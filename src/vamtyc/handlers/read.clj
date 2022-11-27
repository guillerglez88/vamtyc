(ns vamtyc.handlers.read
  (:require [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.utils.response :as response]))

(defn handler [route]
  (fn [req]
    (let [url       (:uri req)
          res-type  (-> route :path path/get-res-type keyword)
          id        (-> req :params :id)]
      (-> res-type
          (store/read id)
          (response/ok)))))

(comment
  ((handler {:name "read-route"
             :path [{:name "resourceType" :value "Route"},
                    {:name "id"}]}) {:uri "/Route/9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"
                                     :params {:id "9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"}})
  )
