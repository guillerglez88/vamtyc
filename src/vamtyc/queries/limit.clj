(ns vamtyc.queries.limit
  (:require [honey.sql.helpers :refer [limit]]))

(defn apply-queryp [sql-map req queryp]
   (-> (:value queryp)
       (str)
       (Integer/parseInt)
       (->> (limit sql-map))))
