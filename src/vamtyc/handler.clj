(ns vamtyc.handler
  (:require
   [clojure.string :as str]
   [honey.sql :as hsql]
   [next.jdbc :as jdbc]
   [ring.util.response :refer [created not-found response status]]
   [vamtyc.data.datasource :refer [ds]]
   [vamtyc.data.queryp :as queryp]
   [vamtyc.data.store :as store]
   [vamtyc.fields :as fields]
   [vamtyc.nav :as nav]
   [vamtyc.param :as param]
   [vamtyc.query :as query]
   [vamtyc.trn :as trn]))

(def handler-create      "/Coding/handlers?code=create")
(def handler-read        "/Coding/handlers?code=read")
(def handler-upsert      "/Coding/handlers?code=upsert")
(def handler-delete      "/Coding/handlers?code=delete")
(def handler-search      "/Coding/handlers?code=search")
(def handler-not-found   "/Coding/handlers?code=not-found")

(defn make-params [req route db-queryps]
  (let [route-params (param/route->param route)
        req-params (param/req->param req)
        type (param/get-value route-params param/wellknown-type)
        param-names (->> req-params keys (filter #(not (#{:vamtyc/url :vamtyc/codes} %))))
        queryps (db-queryps [type] param-names)
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
         type (param/get-value params param/wellknown-type)]
     (->> (:body req)
          (db-create type)
          (#(created (:url %) %))))))

(defn rread
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-fetch (partial store/fetch tx)
           db-queryps (partial queryp/load-queryps tx)]
       (rread req route db-fetch db-queryps))))
  ([req route db-fetch db-queryps]
   (let [params (make-params req route db-queryps)
         type (param/get-value params param/wellknown-type)
         id (param/get-value params param/wellknown-id)
         fields (param/get-value params param/wellknown-fields)]
     (if-let [res (db-fetch type id)]
       (-> (fields/select-fields res fields)
           (response))
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
         type (param/get-value params param/wellknown-type)
         id (param/get-value params param/wellknown-id)
         body (:body req)]
     (if (db-fetch type id)
       (-> (db-edit type id body)
           (response))
       (-> (db-create type id body)
           (#(created (:url %) %)))))))

(defn delete
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-delete (partial store/delete tx)
           db-queryps (partial queryp/load-queryps tx)]
       (delete req route db-delete db-queryps))))
  ([req route db-delete db-queryps]
   (let [params (make-params req route db-queryps)
         type (param/get-value params param/wellknown-type)
         id (param/get-value params param/wellknown-id)]
     (if (db-delete type id)
       (status 204)
       (not-found "Not found")))))

(defn search
  ([req route]
   (jdbc/with-transaction [tx, ds]
     (let [db-search (partial store/search tx)
           db-total (partial store/total tx)
           db-queryps (partial queryp/load-queryps tx)]
       (search req route db-search db-total db-queryps))))
  ([req route db-search db-total db-queryps]
   (let [params (make-params req route db-queryps)
         type (param/get-value params param/wellknown-type)
         of (param/get-value params param/wellknown-of)
         fields (param/get-value params param/wellknown-fields)
         url (param/get-value params :vamtyc/url)
         sql-map (query/search-query req)]
     (->> (hsql/format sql-map)
          (db-search (or of type))
          (into [])
          (nav/result-set req url (db-total sql-map))
          (#(fields/select-fields % fields))
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
  (-> {handler-create    create
       handler-read      rread
       handler-upsert    upsert
       handler-delete    delete
       handler-search    search
       handler-not-found notfound}
      (get code)))
