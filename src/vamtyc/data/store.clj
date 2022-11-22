(ns vamtyc.data.store
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]

            [vamtyc.data.datasource :refer [ds]]))

(defn create
  ([resourceType, id, res]
   (sql/insert! ds resourceType {:id id :resource res}))
  ([resourceType, res]
   (let [id (str (java.util.UUID/randomUUID))]
     (create resourceType id res))))

(defn read [resourceType, id]
  (sql/get-by-id ds resourceType id :id {}))

(defn update [resourceType, id, res]
  (sql/update! ds resourceType {:resource res} {:id id}))

(defn delete [resourceType, id]
  (let [entity (read resourceType id)
        resKey (keyword (name resourceType) "resource")]
    (sql/delete! ds resourceType {:id id})
    (resKey entity)))

(defn list [resourceType]
  (let [table (name resourceType)
        limit 128
        sql-query (str "select * from " table " limit ?")]
    (sql/query ds [sql-query limit])))
