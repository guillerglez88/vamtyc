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

(defn create [req]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))]
    (jdbc/with-transaction [tx ds]
      (->> (:body req)
           (store/create tx type)
           (#(created (:url %) %))))))

(defn rread [req]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))
        fields (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=fields"))]
    (jdbc/with-transaction [tx ds]
      (if-let [res (store/fetch tx type id)]
        (-> (fields/select-fields res fields)
            (response))
        (not-found "Not found")))))

(defn upsert [req]
  (let [body (:body req)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))]
    (jdbc/with-transaction [tx ds]
      (if (store/fetch tx type id)
        (-> (store/edit tx type id body)
            (response))
        (-> (store/create tx type id body)
            (#(created (:url %) %)))))))

(defn delete [req]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))]
    (jdbc/with-transaction [tx ds]
      (if (store/delete tx type id)
        (status 204)
        (not-found "Not found")))))

(defn search [req]
  (let [url     (:vamtyc/url req)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        of (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=of"))
        fields (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=fields"))
        sql-map (query/search-query req)]
    (jdbc/with-transaction [tx ds]
      (->> (hsql/format sql-map)
           (store/search tx (or of type))
           (into [])
           (nav/result-set req url (store/total tx sql-map))
           (#(fields/select-fields % fields))
           (response)))))

(defn transaction [req]
  (jdbc/with-transaction [tx ds]
    (->> (-> req :body :items)
         (map #(trn/commit % req tx))
         (into [])
         (trn/make-trn-result)
         (response))))

(defn notfound [req]
  (let [method  (-> req :request-method name str/upper-case)]
    (-> (str "Not found, "
             "explore available routes at: "
             "/List?_of=Route&method=" method)
        (not-found))))
