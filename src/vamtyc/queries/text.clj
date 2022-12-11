(ns vamtyc.queries.text
  (:require [honey.sql.helpers :refer [where]]))

(defn filter [req query-param sql-map col]
  (let [name  (:name query-param)
        val   (-> req :params (get name))]
    (where sql-map [:like [:cast col :text] (str "%" val "%")])))
