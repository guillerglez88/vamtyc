(ns vamtyc.handler
  (:require
   [clojure.string :as str]
   [honey.sql :as hsql]
   [ring.util.response :refer [created not-found response status]]
   [vamtyc.data.store :as store]
   [vamtyc.fields :as fields]
   [vamtyc.nav :as nav]
   [vamtyc.param :as param]
   [vamtyc.queries.core :as queries]
   [vamtyc.trn :as trn]))

(defn read [req tx _app]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))
        fields (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=fields"))]
    (if-let [res (store/fetch tx type id)]
      (-> (fields/select-fields res fields)
          (response))
      (not-found "Not found"))))

(defn create [req tx _app]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))]
    (->> (:body req)
         (store/create tx type)
         (#(created (:url %) %)))))

(defn upsert [req tx _app]
  (let [body (:body req)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))]
    (if (store/fetch tx type id)
      (-> (store/edit tx type id body)
          (response))
      (-> (store/create tx type id body)
          (#(created (:url %) %))))))

(defn delete [req tx _app]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))]
    (if (store/delete tx type id)
      (status 204)
      (not-found "Not found"))))

(defn search [req tx _app]
  (let [url     (:vamtyc/url req)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        of (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=of"))
        fields (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=fields"))
        sql-map (queries/search-query req tx)
        total   (store/total tx sql-map)]
    (->> (hsql/format sql-map)
         (store/search tx (or of type))
         (into [])
         (nav/result-set req url total)
         (#(fields/select-fields % fields))
         (response))))

(defn transaction [req tx _app]
  (->> (-> req :body :items)
       (map #(trn/commit % req tx))
       (into [])
       (trn/make-trn-result)
       (response)))

(defn notfound [req _tx _app]
  (let [method  (-> req :request-method name str/upper-case)]
    (-> (str "Not found, "
             "explore available routes at: "
             "/List?_of=Route&method=" method)
        (not-found))))
