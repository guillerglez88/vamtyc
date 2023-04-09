(ns vamtyc.nerves.create
  (:require
   [ring.util.response :refer [created]]
   [vamtyc.data.store :as store]
   [vamtyc.param :as param]))

(defn handler [req tx _app]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))]
    (->> (:body req)
         (store/create tx type)
         (#(created (:url %) %)))))
