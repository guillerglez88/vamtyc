(ns vamtyc.nerves.upsert
  (:require [ring.util.response :refer [response created]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.routes :as routes]))

(defn handler [req tx _app]
  (let [body        (:body req)
        res-type    (-> req :vamtyc/route :path routes/get-res-type)
        id          (-> req :params (get "_id"))]
    (if (store/read tx res-type id)
      (-> (store/update tx res-type id body)
          (response))
      (-> (store/create tx res-type id body)
          (#(created (:url %) %))))))
