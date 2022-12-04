(ns vamtyc.modules.resources
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.store :as store]))

(def ddl
  "CREATE TABLE IF NOT EXISTS public.resource (
       id          TEXT    NOT NULL,
       resource    JSONB   NULL,
       CONSTRAINT  resource_pk PRIMARY KEY (id));")

(defn make-resource []
  {:type      "Resource"
   :desc      "Represents a REST resource"
   :resources "/Resource"})

(defn init [tx]
  (let [resource  (make-resource)]
    (jdbc/execute! tx [ddl])
    (store/create tx :Resource "resource" resource)
    {:ok "success!"}))
