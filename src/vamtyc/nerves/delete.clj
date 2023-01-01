(ns vamtyc.nerves.delete
  (:require
   [ring.util.response :refer [status not-found]]
   [vamtyc.data.store :as store]
   [vamtyc.utils.routes :as routes]))

(defn handler [req tx _app]
  (let [res-type  (-> req :vamtyc/route :path routes/_type)
        id        (-> req :vamtyc/route :path routes/_id)]
    (if (store/delete tx res-type id)
      (status 204)
      (not-found "Not found"))))
