(ns vamtyc.resources
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]))

(defn ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name "(
            id          TEXT    NOT NULL,
            resource    JSONB   NULL,
            CONSTRAINT  " name "_pk PRIMARY KEY (id));"))

(defn init []
  (let [resource  {:type      "Resource"
                   :desc      "Represents a REST resource"
                   :resources "/Resource"}
        id        "resource"
        ddl       (ddl "Resource")]
    (jdbc/execute! ds [ddl])
    (store/create :Resource id resource)))

(comment
  (init)
  )
