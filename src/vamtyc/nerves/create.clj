(ns vamtyc.nerves.create
  (:require [ring.util.response :refer [created]]
            [vamtyc.data.store :as store]))

(defn handler [req tx _app]
  (let [body        (:body req)
        res-type    (:resourceType body)]
    (-> (store/create tx res-type body)
        (#(created (:url %) %)))))
