(ns vamtyc.queries.text
  (:require [honey.sql.helpers :refer [where]]
            [vamtyc.utils.queries :refer [make-prop-alias
                                          jsonb-extract-coll
                                          jsonb-extract-prop]]))

(defn filter [req query-param sql-map]
  (let [name  (:name query-param)
        val   (-> req :params (get name))]
    (loop [acc              sql-map
           jsonb            :resource
           [curr & rest]    (:path query-param)]
      (cond
        (nil? curr)         (where acc [:like [:cast jsonb :text] (str "%" val "%")])
        (:collection curr)  (let [alias (make-prop-alias jsonb curr "_elem")]
                              (-> (jsonb-extract-coll acc jsonb curr alias)
                                  (recur alias rest)))
        :else               (let [alias (make-prop-alias jsonb curr)]
                              (-> (jsonb-extract-prop acc jsonb curr alias)
                                  (recur alias rest)))))))
