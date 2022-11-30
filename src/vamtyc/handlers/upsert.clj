(ns vamtyc.handlers.upsert
  (:require [ring.util.response :refer [content-type response]]
            [ring.util.request :refer [body-string]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [clojure.data.json :as json]))

(defn handler [req]
  (let [body        (:body req)
        res-type    (:resourceType body)
        id          (:id body)]
    (-> res-type
        (store/upsert id body)
        (json/write-str)
        (response)
        (content-type "application/json"))))
