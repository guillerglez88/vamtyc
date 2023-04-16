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

(defn create
  ([req route]
   (jdbc/with-transaction [tx ds]
     (let [db-create (partial store/create tx)]
       (create req route db-create))))
  ([req route db-create]
   (let [route-params (param/route->param route)
         type (param/get-value route-params param/wellknown-type)]
     (->> (:body req)
          (db-create type)
          (#(created (:url %) %))))))

(defn rread
  ([req route]
   (jdbc/with-transaction [tx ds]
     (let [db-fetch (partial store/fetch tx)
           db-queryps (partial queryp/load-queryps tx)]
       (rread req route db-fetch db-queryps))))
  ([req route db-fetch db-queryps]
   (let [route-params (param/route->param route)
         req-params (param/req->param req)
         type (param/get-value route-params param/wellknown-type)
         queryps (db-queryps [type] (keys req-params))
         queryp-params (param/queryps->param queryps)
         params (param/merge-param [route-params queryp-params req-params])
         id (param/get-value params param/wellknown-id)
         fields (param/get-value params param/wellknown-fields)]
     (if-let [res (db-fetch type id)]
       (-> (fields/select-fields res fields)
           (response))
       (not-found "Not found")))))

(defn upsert
  ([req route]
   (jdbc/with-transaction [tx ds]
     (let [db-fetch (partial store/fetch tx)
           db-edit (partial store/edit tx)
           db-create (partial store/create tx)]
       (upsert req route db-fetch db-edit db-create))))
  ([req route db-fetch db-edit db-create]
   (let [route-params (param/route->param route)
         req-params (param/req->param req)
         type (param/get-value route-params param/wellknown-type)
         params (param/merge-param [route-params req-params])
         id (param/get-value params param/wellknown-id)
         body (:body req)]
     (if (db-fetch type id)
       (-> (db-edit type id body)
           (response))
       (-> (db-create type id body)
           (#(created (:url %) %)))))))

(defn delete
  ([req route]
   (jdbc/with-transaction [tx ds]
     (let [db-delete (partial store/delete tx)]
       (delete req route db-delete))))
  ([req route db-delete]
   (let [route-params (param/route->param route)
         req-params (param/req->param req)
         type (param/get-value route-params param/wellknown-type)
         params (param/merge-param [route-params req-params])
         id (param/get-value params param/wellknown-id)]
     (if (db-delete type id)
       (status 204)
       (not-found "Not found")))))

(defn search [req route]
  (let [route-params (param/route->param route)
        req-params (param/req->param req)
        type (param/get-value route-params param/wellknown-type)
        of (param/get-value req-params param/wellknown-of)
        fields (param/get-value req-params param/wellknown-fields)
        url (param/get-value req-params :vamtyc/url)
        sql-map (query/search-query req)]
    (jdbc/with-transaction [tx ds]
      (->> (hsql/format sql-map)
           (store/search tx (or of type))
           (into [])
           (nav/result-set req url (store/total tx sql-map))
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
