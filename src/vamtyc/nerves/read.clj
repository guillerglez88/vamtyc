(ns vamtyc.nerves.read
  (:require [ring.util.response :refer [response not-found]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]))

(defn handler [req tx _app]
  (let [res-type  (-> req :body :resourceType)
        id        (-> req :body :id)
        fields    (-> req :params (get "_fields") (or []))
        res       (store/read tx res-type id)]
    (if res
      (-> (fields/select-fields res fields)
          (response))
      (not-found "Not found"))))
