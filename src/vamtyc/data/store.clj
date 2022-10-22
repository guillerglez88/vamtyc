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

(comment
  (create :person {:name [{:given "John" :family ["Doe"]}]})
  (create :person "1" {:name [{:given "John" :family ["Doe"]}]})
  (update :person "29e515d7-ddcb-4761-80a9-2bbba7403758" {:name [{:given "John" :family ["Smith"]}]})
  (delete :person "29e515d7-ddcb-4761-80a9-2bbba7403758")
  (read :person "1")
  )
