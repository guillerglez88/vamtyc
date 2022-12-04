(ns vamtyc.queries.limit
  (:require [vamtyc.config.env :refer [env]]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [limit]]
            [vamtyc.data.store :as store]))

(defn filter [req]
  (let [env-limit   (:LIMIT env)
        limit-val   (-> req :params (get "_limit") (or env-limit) Integer/parseInt)
        sql-map     (-> req :sql-map (limit limit-val))]
    (merge req {:sql-map sql-map})))
