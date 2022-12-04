(ns vamtyc.queries.core
  (:require [vamtyc.data.store :as store]
            [honey.sql :as hsql]
            [honey.sql.helpers :refer [select from where] :as h]
            [vamtyc.queries.limit :as limit]))

(def filters
  {:/Coding/core-query-params?code=limit    limit/filter})

(defn load-queryparams [params res-type tx]
  (let [param-names (->> (or (keys params) '())
                         (map name)
                         (into []))]
    (if (empty? param-names) []
        (-> (select :*)
            (from :QueryParam)
            (where [:= [:jsonb_extract_path_text :resource "type"] (name res-type)])
            (where [:in [:jsonb_extract_path_text :resource "name"] param-names])
            (hsql/format)
            (#(store/list tx :QueryParam %))))))

(defn make-sql-map [res-type]
    (-> (select :*)
        (from res-type)))

(defn process-query-params [req tx]
  (let [res-type    (-> req :body :resourceType)
        params      (:params req)
        sql-map     (make-sql-map res-type)
        seed        (assoc req :sql-map sql-map)]
    (->> (load-queryparams params res-type tx)
         (reduce (fn [acc curr]
                   (->> (keyword (:code curr))
                        (get filters)
                        (#(%2 %1) acc))) seed)
         (#(merge % {:sql (-> % :sql-map hsql/format)})))))
