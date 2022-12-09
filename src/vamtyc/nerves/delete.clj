(ns vamtyc.nerves.delete
  (:require [ring.util.response :refer [status not-found]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]))

(defn handler [req tx _app]
  (let [res-type  (-> req :body :resourceType)
        id        (-> req :body :id)
        res       (store/delete tx res-type id)]
    (if res
      (status 204)
      (not-found "Not found"))))
