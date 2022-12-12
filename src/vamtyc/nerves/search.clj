(ns vamtyc.nerves.search
  (:require [ring.util.response :refer [response]]
            [ring.util.request :refer [request-url]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]
            [vamtyc.queries.core :as queries]))

(defn make-result-set [items url]
  {:type    :List
   :url     url
   :items   items
   :nav     {:first nil
             :prev  nil
             :next  nil
             :last  nil}})

(defn handler [req tx _app]
  (let [url         (request-url req)
        fields      (-> req :params (get "_fields") (or []))
        res-type    (-> req :params (get "_of") keyword)]
    (->> (queries/make-search-query req tx)
         (store/list tx res-type)
         (into [])
         (#(make-result-set % url))
         (#(fields/select-fields % fields))
         (response))))
