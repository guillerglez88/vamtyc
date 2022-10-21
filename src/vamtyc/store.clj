(ns vamtyc.store
  (:require [next.jdbc.sql :as sql]
            [next.jdbc.result-set :as rs]

            [vamtyc.datasource :refer [ds]]))

(defn create [resourceType, res]
  (let [id (java.util.UUID/randomUUID)]
    (sql/insert! ds resourceType {:id id :resource res})))

(defn update [resourceType, id, res]
  (let [uuid (parse-uuid id)]
    (sql/update! ds resourceType {:resource res} {:id uuid})))

(defn delete [resourceType, id]
  (let [uuid (parse-uuid id)
        entity (sql/get-by-id ds resourceType uuid)
        resKey (keyword (name resourceType) "resource")]
    (sql/delete! ds resourceType {:id uuid})
    (resKey entity)))

(comment
  (create :person {:name [{:given "John" :family ["Doe"]}]})
  (update :person "91894c4d-5b2c-4ab7-b952-059504712aab" {:name [{:given "John" :family ["Smith"]}]})
  (delete :person "91894c4d-5b2c-4ab7-b952-059504712aab")
  )
