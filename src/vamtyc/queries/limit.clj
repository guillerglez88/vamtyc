(ns vamtyc.queries.limit
  (:require [vamtyc.config.env :refer [env]]
            [honey.sql :as sql]
            [honey.sql.helpers :refer [limit]]
            [vamtyc.data.store :as store]
            [vamtyc.config.env :as env]))

(defn filter
  ([req query-param sql-map _col]
   (filter sql-map (:params req)))
  ([sql params]
   (-> (get params "_limit")
       (or (get params "env/LIMIT"))
       (Integer/parseInt)
       (->> (limit sql)))))
