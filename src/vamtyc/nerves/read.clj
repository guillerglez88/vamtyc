(ns vamtyc.nerves.read
  (:require
   [ring.util.response :refer [not-found response]]
   [vamtyc.data.store :as store]
   [vamtyc.req.param :as param]
   [vamtyc.resp.fields :as fields]))

(defn handler [req tx _app]
  (let [type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        id (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=id"))
        fields (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=fields"))]
    (if-let [res (store/fetch tx type id)]
      (-> (fields/select-fields res fields)
          (response))
      (not-found "Not found"))))
