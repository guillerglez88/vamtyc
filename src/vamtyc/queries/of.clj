(ns vamtyc.queries.of
  (:require [vamtyc.config.env :refer [env]]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [from limit]]
            [vamtyc.data.store :as store]
            [vamtyc.queries.utils :as qutils]))

(defn filter [req query-param sql-map _col]
  (let [name        (:name query-param)
        default     (:default query-param)
        res-type    (-> req :params (get name) (or default) keyword)]
    (-> (qutils/make-sql-map res-type)
        (merge (dissoc sql-map :select :from)))))
