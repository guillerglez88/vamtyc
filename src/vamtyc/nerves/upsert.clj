(ns vamtyc.nerves.upsert
  (:require
   [ring.util.response :refer [response created]]
   [vamtyc.data.store :as store]
   [vamtyc.utils.routes :as routes]))

(defn handler [req tx _app]
  (let [body        (:body req)
        res-type    (-> req :vamtyc/route :path routes/_type)
        id          (-> req :vamtyc/route :path routes/_id)]
    (if (store/fetch tx res-type id)
      (-> (store/edit tx res-type id body)
          (response))
      (-> (store/create tx res-type id body)
          (#(created (:url %) %))))))
