(ns vamtyc.modules.queryparams
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.modules.routes :as routes]))

(def ddl
  "CREATE TABLE IF NOT EXISTS public.queryparam (
    id          TEXT    NOT NULL,
    resource    JSONB   NULL,
    CONSTRAINT  queryparam_pk PRIMARY KEY (id))")

(defn make-queryparam-resource []
  {:type        "QueryParam"
   :desc        "Represents a REST query-string parameter resource"
   :queryParams "/QueryParam"})

(defn make-queryparam [resourceType]
  {:name          "_limit"
   :desc          "_limit=128 query-string, used for limiting result items count"
   :type          resourceType
   :code          "/Coding/core-query-params?code=limit"
   :queryparams   "/QueryParam"})

(defn init [tx]
  (let [resource  (make-queryparam-resource)]
    (jdbc/execute! tx [ddl])
    (store/create tx :Resource "queryparam" resource)
    (routes/provision resource tx)
    (doseq [res (store/list tx :Resource)]
      (-> (:type res)
          (make-queryparam)
          (#(store/create tx :QueryParam %))))
    {:ok "success!"}))
