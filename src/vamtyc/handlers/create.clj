(ns vamtyc.handlers.create
  (:require [ring.util.response :refer [content-type response]]
            [ring.util.request :refer [body-string]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [clojure.data.json :as json]))

(defn handler [tx req]
  (let [body        (:body req)
        res-type    (:resourceType body)]
    (-> (store/create tx res-type body)
        (json/write-str)
        (response)
        (content-type "application/json"))))
