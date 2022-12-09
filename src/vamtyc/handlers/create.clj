(ns vamtyc.handlers.create
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]))

(defn handler [req tx _app]
  (let [body        (:body req)
        res-type    (:resourceType body)
        fields      (-> req :params (get "_fields") (or []))]
    (-> (store/create tx res-type body)
        (fields/select-fields fields)
        (response))))
