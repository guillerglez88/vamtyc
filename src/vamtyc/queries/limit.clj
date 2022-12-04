(ns vamtyc.queries.limit
  (:require [vamtyc.config.env :refer [env]]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [where] :as h]
            [vamtyc.data.store :as store]))

(defn filter [req]
  (let [env-limit   (:LIMIT env)
        limit       (-> req :params :_limit (or env-limit))
        sql-map     (-> req :sql-map (where [:= :limit limit]))]
    (merge req {:sql-map sql-map})))
