(ns vamtyc.nerves.upsert
  (:require
   [ring.util.response :refer [created response]]
   [vamtyc.data.store :as store]
   [vamtyc.param :as param]))

(defn handler [req tx _app]
  (let [body (:body req)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))]
    (if (store/fetch tx type id)
      (-> (store/edit tx type id body)
          (response))
      (-> (store/create tx type id body)
          (#(created (:url %) %))))))
