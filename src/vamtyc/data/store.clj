(ns vamtyc.data.store
  (:require [clojure.string         :as     str]
            [next.jdbc.sql          :as     sql]
            [next.jdbc.result-set   :as     rs]
            [vamtyc.data.datasource :refer  [ds]]))

(defn process [entity resourceType]
  (when entity
    (let [res-name    (name resourceType)
          res-name-lc (str/lower-case res-name)
          id-key      (keyword res-name-lc "id")
          res-key     (keyword res-name-lc "resource")
          id          (or (id-key entity) (:id entity))
          res         (or (res-key entity) (:resource entity))
          url         (str "/" res-name "/" id)]
      (merge res {:resourceType res-name
                  :id id
                  :url url}))))

(defn create
  ([resourceType id res]
   (let [entity {:id id :resource res}]
    (sql/insert! ds resourceType entity)
    (process  entity resourceType)))
  ([resourceType, res]
   (let [id (str (java.util.UUID/randomUUID))]
     (create resourceType id res))))

(defn read [resourceType id]
  (-> (sql/get-by-id ds resourceType id :id {})
      (process resourceType)))

(defn update [resourceType id res]
  (sql/update! ds resourceType {:resource res} {:id id})
  (-> {:id id :resource res}
      (process resourceType)))

(defn upsert [resourceType id res]
  (let [entity (read resourceType id)]
    (if entity
      (update resourceType id res)
      (create resourceType id res))))

(defn delete [resourceType id]
  (let [entity  (read resourceType id)]
    (sql/delete! ds resourceType {:id id})
    entity))

(defn list [resourceType]
  (let [table (name resourceType)
        limit 128
        sql-query (str "SELECT * FROM " table " LIMIT ?")]
    (->> (sql/query ds [sql-query limit])
         (map (fn [entity] (process entity resourceType ))))))
