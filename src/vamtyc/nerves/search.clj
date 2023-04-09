(ns vamtyc.nerves.search
  (:require
   [honey.sql :as hsql]
   [lambdaisland.uri :refer [assoc-query uri-str]]
   [ring.util.response :refer [response]]
   [vamtyc.data.store :as store]
   [vamtyc.queries.core :as queries]
   [vamtyc.req.param :as param]
   [vamtyc.resp.fields :as fields]))

(defn nav-uri [url offset]
  (-> url (assoc-query :_offset offset) uri-str))

(defn result-set [req url total items]
  (let [offset  (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=offset"))
        limit   (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=limit"))
        first   0
        last    (max first (- total limit))
        prev    (max first (- offset limit))
        next    (min last (+ offset limit))]
    {:type  :List
     :url   url
     :items items
     :total total
     :nav   {:first (nav-uri url first)
             :prev  (nav-uri url prev)
             :next  (nav-uri url next)
             :last  (nav-uri url last)}}))

(defn handler [req tx _app]
  (let [url     (:vamtyc/url req)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        of (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=of"))
        fields (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=fields"))
        sql-map (queries/search-query req tx)
        total   (store/total tx sql-map)]
    (->> (hsql/format sql-map)
         (store/search tx (or of type))
         (into [])
         (result-set req url total)
         (#(fields/select-fields % fields))
         (response))))
