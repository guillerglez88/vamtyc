(ns vamtyc.queries.text
  (:require [honey.sql.helpers :refer [where]]))


(defn filter [req query-param sql-map]
  (let [name  (:name query-param)
        val   (-> req :params (get name) (#(str "%" % "%")))]
    (->> (query-param :path)
         (map #(:name %))
         (into [])
         (concat [:jsonb_extract_path_text :resource])
         (#(where sql-map [:like % val])))))
