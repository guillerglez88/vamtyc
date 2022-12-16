(ns vamtyc.nerves.search
  (:require [ring.util.response :refer [response]]
            [vamtyc.utils.request :refer [relative-url]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as ufields]
            [vamtyc.queries.core :as queries]
            [clojure.string :as str]
            [vamtyc.utils.routes :as uroutes]
            [vamtyc.utils.queryp :as uqueryp]
            [honey.sql :as hsql]
            [lambdaisland.uri :refer [assoc-query uri-str]]))

(defn result-set [req url total items]
  (let [offset  (-> req :vamtyc/queryp uqueryp/offset)
        limit   (-> req :vamtyc/queryp uqueryp/limit)
        foffset 0
        loffset (- total limit)
        poffset (max foffset (- offset limit))
        noffset (min loffset (+ offset limit))
        first   (-> url (assoc-query :_offset foffset) uri-str)
        prev    (-> url (assoc-query :_offset poffset) uri-str)
        next    (-> url (assoc-query :_offset noffset) uri-str)
        last    (-> url (assoc-query :_offset loffset :_total total) uri-str)]
    {:type    :List
     :url     url
     :items   items
     :nav     {:first first
               :prev  prev
               :next  next
               :last  last}}))

(defn handler [req tx _app]
  (let [url     (relative-url req)
        fields  (-> req :vamtyc/queryp uqueryp/fields (ufields/flat-expr))
        type    (-> req :vamtyc/route :path uroutes/type)
        of      (-> req :vamtyc/queryp uqueryp/of)
        sql-map (queries/search-query req tx)
        total   (store/count tx sql-map)]
    (->> (hsql/format sql-map)
         (store/list tx (or of type))
         (into [])
         (result-set req url total)
         (#(ufields/select-fields % fields))
         (response))))
