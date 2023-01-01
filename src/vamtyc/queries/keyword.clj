(ns vamtyc.queries.keyword
  (:require
   [honey.sql.helpers :refer [where]]
   [vamtyc.utils.queryp :as uqueryp]))

(defn apply-queryp [sql-map _req queryp]
  (let [name  (uqueryp/queryp-name queryp)
        val   (:value queryp)]
    (where sql-map [:= [:cast name :text] (str "\"" val "\"")])))
