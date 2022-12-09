(ns vamtyc.nerves.upsert
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]))

(defn handler [req tx _app]
  (let [body        (:body req)
        res-type    (:resourceType body)
        id          (:id body)
        fields      (-> req :params (get "_fields") (or []))]
    (-> (store/upsert tx res-type id body)
        (fields/select-fields fields)
        (response))))
