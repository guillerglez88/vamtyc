(ns vamtyc.handlers.delete
  (:require [ring.util.response :refer  [content-type response status]]
            [vamtyc.data.store  :as     store]
            [vamtyc.utils.path  :as     path]
            [clojure.data.json  :as     json]))

(defn handler [req route]
  (let [res-type  (-> route :path path/get-res-type keyword)
        id        (-> req :params :id)]
    (-> res-type
        (store/delete id)
        (json/write-str)
        (response)
        (content-type "application/json"))))
