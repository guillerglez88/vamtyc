(ns vamtyc.handlers.read
  (:require [ring.util.response :refer  [content-type response]]
            [vamtyc.data.store  :as     store]
            [vamtyc.utils.path  :as     path]
            [clojure.data.json :as json]))

(defn handler [route]
  (fn [req]
    (let [url       (:uri req)
          res-type  (-> route :path path/get-res-type keyword)
          id        (-> req :params :id)]
      (-> res-type
          (store/read id)
          (json/write-str)
          (response)
          (content-type "application/json")))))

(comment
  ((handler {:name "read-route"
             :path [{:name "resourceType" :value "Route"},
                    {:name "id"}]}) {:uri "/Route/9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"
                                     :params {:id "9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"}})
  )
