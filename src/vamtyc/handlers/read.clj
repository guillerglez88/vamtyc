(ns vamtyc.handlers.read
  (:require [ring.util.response :refer [response not-found]]
            [vamtyc.data.store :as store]))

(defn handler [req tx]
  (let [res-type  (-> req :body :resourceType)
        id        (-> req :body :id)
        res       (store/read tx res-type id)]
    (if res
      (response res)
      (not-found "Not found"))))
