(ns vamtyc.handlers.read
  (:require [ring.util.response :refer  [content-type response not-found]]
            [vamtyc.data.store  :as     store]
            [vamtyc.utils.path  :as     path]
            [clojure.data.json  :as     json]))

(defn handler [req route]
  (let [res-type  (-> route :path path/get-res-type keyword)
        id        (-> req :params :id)
        res       (store/read res-type id)]
    (if res
      (-> res
          (json/write-str)
          (response)
          (content-type "application/json"))
      (not-found "Not found"))))

(comment
  ((handler {:name "read-route"
             :path [{:name "resourceType" :value "Route"},
                    {:name "id"}]}) {:uri "/Route/9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"
                                     :params {:id "9ddf6cbd-1def-4306-8b19-a664bbbdf2ae"}})
  )
