(ns vamtyc.handlers.delete
  (:require [ring.util.response :refer  [content-type response status not-found]]
            [vamtyc.data.store  :as     store]
            [vamtyc.utils.path  :as     path]
            [clojure.data.json  :as     json]))

(defn handler [req route]
  (let [res-type  (-> route :path path/get-res-type keyword)
        id        (-> req :params :id)
        res       (store/delete res-type id)]
    (if res
      (-> res
          (json/write-str)
          (response)
          (content-type "application/json"))
      (not-found "Not found"))))
