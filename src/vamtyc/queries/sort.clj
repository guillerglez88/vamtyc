(ns vamtyc.queries.sort
  (:require [honey.sql.helpers :refer [order-by]]
            [vamtyc.data.store :as store]))

(defn filter [req query-param sql-map _col]
  (let [name    (:name query-param)
        default (-> query-param :default)]
    sql-map))
