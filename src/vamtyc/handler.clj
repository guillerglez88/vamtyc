(ns vamtyc.handler
  (:require
   [clojure.string :as str]
   [honey.sql :as hsql]
   [next.jdbc :as jdbc]
   [ring.util.response :refer [created not-found response status]]
   [vamtyc.data.datasource :refer [ds]]
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
     (->> (partial store/create tx)
          (create req route))))
  ([req route db-create]
   (let [route-params (param/route->param route)
         type (param/get-value route-params param/wellknown-type)]
     (->> (:body req)
          (db-create type)
          (#(created (:url %) %))))))

(defn rread [req route]
  (let [route-params (param/route->param route)
        req-params (param/req->param req)
        type (param/get-value route-params param/wellknown-type)
        id (param/get-value route-params param/wellknown-id)
        fields (param/get-value req-params param/wellknown-fields)]
    (jdbc/with-transaction [tx ds]
      (if-let [res (store/fetch tx type id)]
        (-> (fields/select-fields res fields)
            (response))
        (not-found "Not found")))))

(defn upsert [req route]
  (let [route-params (param/route->param route)
        type (param/get-value route-params param/wellknown-type)
        id (param/get-value route-params param/wellknown-id)
        body (:body req)]
    (jdbc/with-transaction [tx ds]
      (if (store/fetch tx type id)
        (-> (store/edit tx type id body)
            (response))
        (-> (store/create tx type id body)
            (#(created (:url %) %)))))))

(defn delete [_req route]
  (let [route-params (param/route->param route)
        type (param/get-value route-params param/wellknown-type)
        id (param/get-value route-params param/wellknown-id)]
    (jdbc/with-transaction [tx ds]
      (if (store/delete tx type id)
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
