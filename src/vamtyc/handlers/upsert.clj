(ns vamtyc.handlers.upsert
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]))

(defn handler [req tx]
  (let [body        (:body req)
        res-type    (:resourceType body)
        id          (:id body)]
    (-> (store/upsert tx res-type id body)
        (response))))
