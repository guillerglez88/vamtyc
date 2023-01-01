(ns vamtyc.nerves.search
  (:require
   [ring.util.response :refer [response]]
   [vamtyc.utils.request :refer [relative-url]]
   [vamtyc.data.store :as store]
   [vamtyc.utils.fields :as ufields]
   [vamtyc.queries.core :as queries]
   [vamtyc.utils.routes :as uroutes]
   [vamtyc.utils.queryp :as uqueryp]
   [honey.sql :as hsql]
   [lambdaisland.uri :refer [assoc-query uri-str]]))

(defn nav-uri [url offset]
  (-> url (assoc-query :_offset offset) uri-str))

(defn result-set [req url total items]
  (let [offset  (-> req :vamtyc/queryp uqueryp/offset)
        limit   (-> req :vamtyc/queryp uqueryp/limit)
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
  (let [url     (relative-url req)
        fields  (-> req :vamtyc/queryp uqueryp/fields ufields/flat-expr)
        type    (-> req :vamtyc/route :path uroutes/_type)
        of      (-> req :vamtyc/queryp uqueryp/of)
        sql-map (queries/search-query req tx)
        total   (store/total tx sql-map)]
    (->> (hsql/format sql-map)
         (store/search tx (or of type))
         (into [])
         (result-set req url total)
         (#(ufields/select-fields % fields))
         (response))))
