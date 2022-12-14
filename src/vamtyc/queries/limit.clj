(ns vamtyc.queries.limit
  (:require
   [honey.sql.helpers :refer [limit]]
   [vamtyc.utils.queryp :as uqueryp]))

(defn apply-queryp [sql-map _req queryp]
  (->> (uqueryp/limit [queryp])
       (limit sql-map)))
