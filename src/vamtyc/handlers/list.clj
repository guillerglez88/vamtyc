(ns vamtyc.handlers.list
  (:require [clojure.data.json :as json]
            [ring.util.response :refer [content-type response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]))

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

(defn handler [req route]
  (let [url       (:uri req)
        res-type  (-> route :path path/get-res-type keyword)]
    (-> res-type
        (store/list)
        (build-result-set url)
        (json/write-str)
        (response)
        (content-type "application/json"))))

(comment
  ((handler {:name "list-route"
             :path [{:name "resourceType" :value "Route"}]}) {:uri "/Route/9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"})
  )
