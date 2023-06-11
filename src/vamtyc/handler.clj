(ns vamtyc.handler
  (:require
   [clojure.string :as str]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [ring.util.response :refer [created header not-found response status]]
   [vamtyc.data.datasource :refer [ds]]
   [vamtyc.data.queryp :as queryp]
   [vamtyc.data.store :as store]
   [vamtyc.fields :as fields]
   [vamtyc.nav :as nav]
   [vamtyc.param :as param]
   [vamtyc.query :as query]
   [vamtyc.trn :as trn]
   [clojure.set :as set]))

(def hdl-create      "/Coding/handlers?code=create")
(def hdl-read        "/Coding/handlers?code=read")
(def hdl-upsert      "/Coding/handlers?code=upsert")
(def hdl-delete      "/Coding/handlers?code=delete")
(def hdl-search      "/Coding/handlers?code=search")
(def hdl-not-found   "/Coding/handlers?code=not-found")

(defn make-params [req route db-queryps]
  (let [route-params (param/route->param route)
        req-params (param/req->param req)
        of (param/get-value req-params param/wkp-of)
        type (param/get-value route-params param/wkp-type)
        param-names (->> req-params first keys)
        queryps (db-queryps [of type] param-names)
        queryp-params (param/queryps->param queryps)]
    (param/merge-param [route-params queryp-params req-params])))

(defn create
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-create (partial store/create tx)
           db-queryps (partial queryp/load-queryps tx)]
       (create req route db-create db-queryps))))
  ([req route db-create db-queryps]
   (let [params (make-params req route db-queryps)
         type (param/get-value params param/wkp-type)
         body (:body req)
         res (db-create type body)]
     (-> (created (:url res) res)
         (header "ETag" (:etag res))))))

(defn rread
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-fetch (partial store/fetch tx)
           db-queryps (partial queryp/load-queryps tx)]
       (rread req route db-fetch db-queryps))))
  ([req route db-fetch db-queryps]
   (let [params (make-params req route db-queryps)
         [type, id, fields] (param/get-values params
                                              param/wkp-type
                                              param/wkp-id
                                              param/wkp-fields)]
     (if-let [res (db-fetch type id)]
       (-> (fields/select-fields res fields)
           (response)
           (header "ETag" (:etag res)))
       (not-found "Not found")))))

(defn upsert
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-fetch (partial store/fetch tx)
           db-edit (partial store/edit tx)
           db-create (partial store/create tx)
           db-queryps (partial queryp/load-queryps tx)]
       (upsert req route db-fetch db-edit db-create db-queryps))))
  ([req route db-fetch db-edit db-create db-queryps]
   (let [params (make-params req route db-queryps)
         [type id] (param/get-values params param/wkp-type param/wkp-id)
         body (:body req)]
     (if (db-fetch type id)
       (let [res (db-edit type id body)]
         (-> (response res)
             (header "ETag" (:etag res))))
       (let [res (db-create type id body)]
         (-> (created (:url res) res)
             (header "ETag" (:etag res))))))))

(defn delete
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-delete (partial store/delete tx)
           db-queryps (partial queryp/load-queryps tx)]
       (delete req route db-delete db-queryps))))
  ([req route db-delete db-queryps]
   (let [params (make-params req route db-queryps)
         [type id] (param/get-values params
                                     param/wkp-type
                                     param/wkp-id)]
     (if (db-delete type id)
       (status 204)
       (not-found "Not found")))))

(defn search
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-search (partial store/search tx)
           db-queryps (partial queryp/load-queryps tx)
           db-upsert (partial store/upsert tx)
           db-total (fn [sql] (-> (sql/query tx sql) (first) (:count)))]
       (search req route db-search db-total db-queryps db-upsert))))
  ([req route db-search db-total db-queryps db-upsert]
   (let [params (make-params req route db-queryps)
         [of type] (param/get-values params param/wkp-of param/wkp-type)
         [offset limit] (param/get-values params param/wkp-offset param/wkp-limit)
         start (-> offset str Integer/parseInt)
         count (-> limit str Integer/parseInt)
         param-names (->> params first keys)
         queryps (db-queryps (vector of type) param-names)
         fields (param/get-value params param/wkp-fields)
         ignore-params (->> queryps (filter #(#{param/wkp-type param/wkp-of} (:code %))) (map #(-> % :name name)) (into #{}))
         req-params (-> (keys (:params req)) hash-set (set/difference ignore-params) (#(select-keys (:params req) %)))
         table (-> of (or type) keyword)
         query (query/make-pg-query table req-params queryps)
         total (db-total (:total query))
         pgquery (db-upsert :PgQuery (:hash query) query)]
     (-> (:from query)
         (db-search (:page query))
         (nav/result-set (param/url req) start count total (:url pgquery))
         (fields/select-fields fields)
         (response)))))

(defn transaction [req _route]
  (jdbc/with-transaction [tx ds]
    (->> (-> req :body :items)
         (map #(trn/commit % req tx))
         (into [])
         (trn/make-trn-result)
         (response))))

(defn notfound [req _route]
  (let [method  (-> req :request-method name str/upper-case)]
    (-> (str "Not found, "
             "explore available routes at: "
             "/List?_of=Route&method=" method)
        (not-found))))

(defn lookup [code]
  (-> {hdl-create    create
       hdl-read      rread
       hdl-upsert    upsert
       hdl-delete    delete
       hdl-search    search
       hdl-not-found notfound}
      (get code)))
