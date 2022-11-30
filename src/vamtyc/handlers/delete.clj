(ns vamtyc.handlers.delete
  (:require [ring.util.response :refer [content-type response status not-found]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [clojure.data.json :as json]))

(defn handler [tx req]
  (let [res-type  (-> req :body :resourceType)
        id        (-> req :body :id)
        res       (store/delete tx res-type id)]
    (if res
      (-> res
          (json/write-str)
          (response)
          (content-type "application/json"))
      (not-found "Not found"))))
