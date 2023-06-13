(ns vamtyc.handler
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [next.jdbc :as jdbc]
   [next.jdbc.sql :as sql]
   [rest-query.core :as rq]
   [ring.util.response :refer [created header not-found response status]]
   [vamtyc.data.datasource :refer [ds]]
   [vamtyc.data.queryp :as queryp]
   [vamtyc.data.store :as store]
   [vamtyc.fields :as fields]
   [vamtyc.nav :as nav]
   [vamtyc.param :as param]
   [vamtyc.trn :as trn]))

(def hdl-create      "/Coding/handlers?code=create")
(def hdl-read        "/Coding/handlers?code=read")
(def hdl-upsert      "/Coding/handlers?code=upsert")
(def hdl-delete      "/Coding/handlers?code=delete")
(def hdl-search      "/Coding/handlers?code=search")
(def hdl-not-found   "/Coding/handlers?code=not-found")

(def flt-text     "/Coding/filters?code=text")
(def flt-keyword  "/Coding/filters?code=keyword")
(def flt-url      "/Coding/filters?code=url")
(def flt-number   "/Coding/filters?code=number")
(def flt-date     "/Coding/filters?code=date")
(def wkp-limit    "/Coding/wellknown-params?code=limit")
(def wkp-offset   "/Coding/wellknown-params?code=offset")
(def wkp-sort     "/Coding/wellknown-params?code=sort")
(def wkp-type     "/Coding/wellknown-params?code=type")
(def wkp-of       "/Coding/wellknown-params?code=of")
(def wkp-fields   "/Coding/wellknown-params?code=fields")

(def coding-map
  {flt-text     rq/flt-text
   flt-keyword  rq/flt-keyword
   flt-url      rq/flt-url
   flt-number   rq/flt-number
   flt-date     rq/flt-date
   wkp-offset   rq/pag-offset
   wkp-limit    rq/pag-limit
   wkp-sort     rq/pag-sort})

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
   (let [param-names (-> req :params keys)
         route-type (->> (:path route) (filter #(-> % :code (= wkp-type))) (map :value) (first))
         queryps (-> route-type vector (db-queryps param-names))

         of-queryp (->> queryps (filter #(-> % :code (= wkp-of))) (first))
         fields-queryp (->> queryps (filter #(-> % :code (= wkp-fields))) (first))
         offset-queryp (->> queryps (filter #(-> % :code (= wkp-offset))) (first))
         limit-queryp (->> queryps (filter #(-> % :code (= wkp-limit))) (first))

         of (-> req :params (get (:name of-queryp)))
         fields (-> req :params (get (:name fields-queryp)))
         offset (-> req :params (get (:name offset-queryp)) (or (:default offset-queryp)) str (Integer/parseInt))
         limit (-> req :params (get (:name limit-queryp)) (or (:default limit-queryp)) str (Integer/parseInt))

         keep-params (set/difference (apply hash-set (keys (:params req))) (hash-set (:name of-queryp)))
         req-params (select-keys (:params req) keep-params)

         table (-> of (or route-type) keyword)
         xparams (rq/expand-params req-params)
         cqueryps (map #(assoc % :code (get coding-map (:code %))) queryps)
         xqueryps (rq/expand-queryps cqueryps)

         query (rq/make-query table xparams xqueryps)
         total (db-total (:total query))
         pgquery (db-upsert :PgQuery (:hash query) query)]
     (-> (:from query)
         (db-search (:page query))
         (nav/result-set (param/url req) offset limit total (:url pgquery))
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
