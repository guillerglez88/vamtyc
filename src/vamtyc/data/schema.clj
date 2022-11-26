(ns vamtyc.data.schema
  (:require [next.jdbc :as jdbc]

            [vamtyc.data.datasource :refer [ds]]))

(defn storage-ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name " (
            id TEXT NOT NULL,
            resource JSONB NULL,
            CONSTRAINT " name "_pk PRIMARY KEY (id));"))

(defn history-ddl [name]
  (str "CREATE TABLE IF NOT EXISTS public." name "_history (
            id TEXT NOT NULL,
            resource JSONB NULL,
            CONSTRAINT " name "_history_pk PRIMARY KEY (id));"))

(defn provision [resourceType]
  (let [table-name (name resourceType)]
    (jdbc/execute! ds [(str (storage-ddl table-name) "\n"
                            (history-ddl table-name))])))

;;
(comment
  (provision :HttpRoute))
