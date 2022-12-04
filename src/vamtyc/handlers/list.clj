(ns vamtyc.handlers.list
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]
            [vamtyc.routes :as routes]
            [vamtyc.utils.path :as path]))

(defn make-result-set [items url]
  {:resourceType  :List
   :url           url
   :type          :result-set
   :items         items
   :nav           {:first nil
                   :prev  nil
                   :self  url
                   :next  nil
                   :last  nil}})

(defn handler [req tx]
  (let [url       (:url req)
        res-type  (-> req :body :resourceType)]
    (-> (store/list tx res-type)
        (#(into [] %))
        (make-result-set url)
        (response))))
