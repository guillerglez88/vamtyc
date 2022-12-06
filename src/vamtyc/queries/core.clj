(ns vamtyc.queries.core
  (:require [honey.sql :as sql]
            [vamtyc.queries.limit :as limit]
            [vamtyc.utils.requests :refer [extract-param-names]]
            [vamtyc.utils.queries :refer [make-sql-map make-prop-alias
                                          jsonb-extract-prop jsonb-extract-coll]]
            [vamtyc.data.queryparams :refer [load-queryparams]]
            [vamtyc.queries.text :as text]))

(def filters
  {:/Coding/core-query-params?code=limit  limit/filter
   :/Coding/core-query-params?code=text   text/filter})

(defn refine-query [req sql-map query-param]
  (let [filter (-> query-param :code keyword filters)]
    (loop [acc            sql-map
           col            :resource
           [curr & rest]  (:path query-param)]
      (cond
        (nil? curr)         (filter req query-param acc col)
        (:collection curr)  (let [alias (make-prop-alias col curr "_elem")]
                              (-> (jsonb-extract-coll acc col curr alias)
                                  (recur alias rest)))
        :else               (let [alias (make-prop-alias col curr)]
                              (-> (jsonb-extract-prop acc col curr alias)
                                  (recur alias rest)))))))

(defn process-query-params [req tx]
  (let [res-type    (-> req :body :resourceType)
        param-names (extract-param-names req)
        queryparams (load-queryparams param-names res-type tx)
        sql-map     (make-sql-map res-type)]
    (if (empty? queryparams)
      (assoc req :sql (sql/format sql-map {:pretty true}))
      (-> (reduce #(refine-query req %1 %2) sql-map queryparams)
          (sql/format {:pretty true})
          (->> (assoc req :queryparams queryparams :sql))))))
