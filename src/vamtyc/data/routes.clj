(ns vamtyc.data.routes
  (:require [honey.sql.helpers :refer [select from inner-join where]]
            [honey.sql :as hsql]
            [next.jdbc.sql :as sql]))

(defn get-all [tx]
  (-> (select [:route.id "route-id"]
              [:route.resource "route-resource"]
              [:res.id "res-id"]
              [:res.resource "res-resource"])
      (from [:Route :route])
      (inner-join [[:jsonb_extract_path_text :resource "resource"] :res_ref] true)
      (inner-join [:Resource :res] true)
      (where [:= [:concat "/Resource/" :res.id] :res_ref])
      (hsql/format)
      (->> (sql/query tx))))
