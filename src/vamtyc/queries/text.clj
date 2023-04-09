(ns vamtyc.queries.text
  (:require
   [clojure.string :as str]
   [honey.sql.helpers :refer [where]]))

(defn apply-queryp [sql-map req queryp]
  (let [name (-> queryp :name name)
        db-name (-> name (str/replace #"-" "_") keyword)
        val (-> req :vamtyc/param (get name))]
    (where sql-map [:like [:cast db-name :text] (str "%" val "%")])))
