(ns vamtyc.handlers.list
  (:require [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [clojure.data.json :as json]))

(defn build-result-set [items url]
  {:resourceType  :List
   :url           url
   :type          :result-set
   :items         items
   :nav           {:first nil
                   :prev  nil
                   :self  url
                   :next  nil
                   :last  nil}})

(defn handler [route]
  (fn [req]
    (let [url       (:uri req)
          res-type  (-> route :path path/get-res-type keyword)]
      (-> res-type
          (store/list)
          (build-result-set url)
          (json/write-str)))))

(comment
  ((handler {:name "read-route"
             :path [{:name "resourceType" :value "Route"},
                    {:name "id"}]}) {:uri "/Route/9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"})
  )
