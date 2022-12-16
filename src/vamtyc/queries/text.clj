(ns vamtyc.queries.text
  (:require [honey.sql.helpers :refer [where]]
            [vamtyc.utils.queryp :as uqueryp]))

(defn apply-queryp [sql-map req queryp]
  (let [name  (uqueryp/queryp-name queryp)
        val   (:value queryp)]
    (where sql-map [:like [:cast name :text] (str "%" val "%")])))
