(ns vamtyc.queries.keyword
  (:require [honey.sql.helpers :refer [where]]
            [vamtyc.utils.queries :refer [make-prop-alias
                                          jsonb-extract-coll
                                          jsonb-extract-prop]]))

(defn filter [req query-param sql-map col]
  (let [name  (:name query-param)
        val   (-> req :params (get name))]
    (where sql-map [:= [:cast col :text] (str "\"" val "\"")])))
