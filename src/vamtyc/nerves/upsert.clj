(ns vamtyc.nerves.upsert
  (:require [ring.util.response :refer [response created]]
            [vamtyc.data.store :as store]))

(defn handler [req tx _app]
  (let [body        (:body req)
        res-type    (:resourceType body)
        id          (:id body)]
    (if (store/read tx res-type id)
      (-> (store/update tx res-type id body)
          (response))
      (-> (store/create tx res-type id body)
          (#(created (:url %) %))))))
