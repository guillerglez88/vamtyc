(ns vamtyc.nerves.delete
  (:require
   [ring.util.response :refer [not-found status]]
   [vamtyc.data.store :as store]
   [vamtyc.param :as param]))

(defn handler [req tx _app]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))]
    (if (store/delete tx type id)
      (status 204)
      (not-found "Not found"))))
