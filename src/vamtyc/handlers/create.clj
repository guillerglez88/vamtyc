(ns vamtyc.handlers.create
  (:require [ring.util.response :refer [content-type response]]
            [ring.util.request :refer [body-string]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [clojure.data.json :as json]))

(defn handler [req]
  (let [body        (:body req)
        res-type    (:resourceType body)]
    (-> res-type
        (store/create body)
        (json/write-str)
        (response)
        (content-type "application/json"))))
