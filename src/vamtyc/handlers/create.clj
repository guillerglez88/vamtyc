(ns vamtyc.handlers.create
  (:require [ring.util.response :refer [response]]
            [vamtyc.data.store :as store]))

(defn handler [req tx]
  (let [body        (:body req)
        res-type    (:resourceType body)]
    (-> (store/create tx res-type body)
        (response))))
