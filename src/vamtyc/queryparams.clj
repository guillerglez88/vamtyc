(ns vamtyc.queryparams
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]))

(def ddl
  "CREATE TABLE IF NOT EXISTS public.queryparam (
    id          TEXT    NOT NULL,
    resource    JSONB   NULL,
    CONSTRAINT  queryparam_pk PRIMARY KEY (id))")

(defn make-queryparam-resource []
  {:type        "QueryParam"
   :desc        "Represents a REST query-string parameter resource"
   :queryParams "/QueryParam"})

(defn init [tx]
  (let [resource  (make-queryparam-resource)]
    (jdbc/execute! tx [ddl])
    (store/create tx :Resource "queryparam" resource)
    {:ok "success!"}))
