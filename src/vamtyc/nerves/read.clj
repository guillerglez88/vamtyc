(ns vamtyc.nerves.read
  (:require [ring.util.response :refer [response not-found]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.fields :as fields]
            [vamtyc.utils.routes :as routes]
            [vamtyc.utils.queryp :as uqueryp]))

(defn handler [req tx _app]
  (let [res-type  (-> req :vamtyc/route :path routes/type)
        id        (-> req :vamtyc/route :path routes/id)
        fields    (-> req :vamtyc/queryp uqueryp/fields fields/flat-expr)
        res       (store/read tx res-type id)]
    (if res
      (-> (fields/select-fields res fields)
          (response))
      (not-found "Not found"))))
