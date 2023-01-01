(ns vamtyc.nerves.read
  (:require
   [ring.util.response :refer [response not-found]]
   [vamtyc.data.store :as store]
   [vamtyc.utils.fields :as fields]
   [vamtyc.utils.routes :as routes]
   [vamtyc.utils.queryp :as uqueryp]))

(defn handler [req tx _app]
  (let [res-type  (-> req :vamtyc/route :path routes/_type)
        id        (-> req :vamtyc/route :path routes/_id)
        fields    (-> req :vamtyc/queryp uqueryp/fields fields/flat-expr)
        res       (store/fetch tx res-type id)]
    (if res
      (-> (fields/select-fields res fields)
          (response))
      (not-found "Not found"))))
