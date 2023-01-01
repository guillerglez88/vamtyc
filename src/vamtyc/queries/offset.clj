(ns vamtyc.queries.offset
  (:require
   [vamtyc.utils.queryp :as uqueryp]
   [honey.sql.helpers :refer [offset]]))

(defn apply-queryp [sql-map _req queryp]
  (->> (uqueryp/offset [queryp])
       (offset sql-map)))
