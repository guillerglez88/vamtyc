(ns vamtyc.nerves.create
  (:require [ring.util.response :refer [created]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.routes :as routes]))

(defn handler [req tx _app]
  (let [res-type (-> req :vamtyc/route :path routes/type)]
    (->> (:body req)
         (store/create tx res-type)
         (#(created (:url %) %)))))
