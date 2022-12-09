(ns vamtyc.nerves.search
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.utils.fields :as fields]
            [vamtyc.queries.core :as queries]))

(defn make-result-set [items url]
  {:resourceType  :List
   :url           url
   :items         items
   :nav           {:first nil
                   :prev  nil
                   :next  nil
                   :last  nil}})

(defn handler [req tx _app]
  (let [url         (-> req :url)
        res-type    (-> req :params (get "_of") (or "List") keyword)
        fields      (-> req :params (get "_fields") (or []))]
    (->> (queries/make-search-query req tx)
         (store/list tx res-type)
         (map #(fields/select-fields % fields))
         (into [])
         (#(make-result-set % url))
         (response))))
