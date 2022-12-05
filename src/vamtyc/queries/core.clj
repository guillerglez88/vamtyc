(ns vamtyc.queries.core
  (:require [vamtyc.data.store :as store]
            [honey.sql :as hsql]
            [honey.sql.helpers :refer [select from where] :as h]
            [vamtyc.queries.limit :as limit]
            [vamtyc.utils.requests :refer [extract-param-names]]
            [vamtyc.data.queryparams :refer [load-queryparams]]))

(def filters
  {:/Coding/core-query-params?code=limit    limit/filter})

(defn refine-query [req sql-map query-param]
  (-> query-param :code keyword filters
      (#(% req sql-map))))

(defn make-sql-map [res-type]
    (-> (select :*)
        (from res-type)))

(defn process-query-params [req tx]
  (let [res-type    (-> req :body :resourceType)
        param-names (extract-param-names req)
        queryparams (load-queryparams param-names res-type tx)
        sql-map     (make-sql-map res-type)]
    (if (empty? queryparams) req
        (->> (reduce #(refine-query req %1 %2) sql-map queryparams)
             (hsql/format)
             (assoc req :sql)))))
