(ns vamtyc.handlers.upsert
  (:require [ring.util.response :refer [content-type response]]
            [ring.util.request :refer [body-string]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [clojure.data.json :as json]))

(defn handler [req route]
  (let [res-type  (-> route :path path/get-res-type keyword)
        id        (-> req :params :id)
        body      (-> req body-string (json/read-str :key-fn keyword))]
    (-> res-type
        (store/upsert id body)
        (json/write-str)
        (response)
        (content-type "application/json"))))
