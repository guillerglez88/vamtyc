(ns vamtyc.schema
  (:require [next.jdbc :as jdbc]

            [vamtyc.datasource :refer [ds]]))

(defn storage-ddl [name]
  (str "CREATE TABLE public." name " (
            id uuid NOT NULL,
            resource jsonb NULL,
            CONSTRAINT " name "_pk PRIMARY KEY (id));"))

(defn history-ddl [name]
  (str "CREATE TABLE public." name "_history (
            id uuid NOT NULL,
            resource jsonb NULL,
            CONSTRAINT " name "_history_pk PRIMARY KEY (id));"))

(defn provision [resourceType]
  (let [tableName (name resourceType)]
    (jdbc/execute! ds [(str (storage-ddl name) "\n"
                            (history-ddl name))])))

(comment
  (provision "person")
  (provision "country")
  )
