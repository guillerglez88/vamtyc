(ns vamtyc.queries.core
  (:require [honey.sql :as sql]
            [vamtyc.queries.limit :as limit]
            [vamtyc.queries.of :as of]
            [vamtyc.queries.utils :as utils]
            [vamtyc.queries.text :as text]
            [vamtyc.queries.offset :as offset]
            [vamtyc.queries.fields :as fields]
            [vamtyc.queries.keyword :as keyw]
            [vamtyc.queries.sort :as sort]))

(def filters
  {:_limit                        limit/filter
   :_offset                       offset/filter
   :_of                           of/filter
   :_fields                       fields/filter
   :_sort                         sort/filter
   :/Coding/filters?code=text     text/filter
   :/Coding/filters?code=keyword  keyw/filter})

(defn refine-query [req sql-map query-param]
  (let [code    (-> query-param :code keyword)
        path    (-> query-param :path (or []))
        name    (-> query-param :name keyword)
        filter  (or (name filters) (code filters))]
    (loop [acc            sql-map
           col            :resource
           [curr & rest]  path]
      (cond
        (nil? curr)
          (filter req query-param acc col)
        (:meta curr)
          sql-map
        (:collection curr)
          (let [alias (utils/make-prop-alias col curr "_elem")]
            (-> (utils/jsonb-extract-coll acc col curr alias)
                (recur alias rest)))
        :else
          (let [alias (utils/make-prop-alias col curr)]
            (-> (utils/jsonb-extract-prop acc col curr alias)
                (recur alias rest)))))))

(defn make-search-query [req tx]
  (let [queryp        (-> req :vamtyc/queryp)
        res-type      (-> req :params (get "_of") keyword)
        sql-map       (utils/make-sql-map res-type)]
    (-> (reduce #(refine-query req %1 %2) sql-map queryp)
        (sql/format {:pretty true}))))
