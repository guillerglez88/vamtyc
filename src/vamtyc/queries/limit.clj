(ns vamtyc.queries.limit
  (:require [vamtyc.config.env :refer [env]]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [limit]]
            [vamtyc.data.store :as store]))

(defn filter [req sql-map]
  (-> req :params
      (get "_limit")
      (Integer/parseInt)
      (->> (limit sql-map))))
