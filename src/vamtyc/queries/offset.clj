(ns vamtyc.queries.offset
  (:require [vamtyc.config.env :refer [env]]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [limit]]
            [vamtyc.data.store :as store]
            [vamtyc.config.env :as env]))

(defn filter [req query-param sql-map _col]
  sql-map)
