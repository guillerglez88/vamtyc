(ns vamtyc.nerves.create
  (:require [ring.util.response :refer [created]]
            [vamtyc.data.store :as store]))

(defn handler [req tx _app]
  (let [res-type    (-> req :params (get "_type") keyword)]
    (->> (:body req)
         (store/create tx res-type)
         (#(created (:url %) %)))))
