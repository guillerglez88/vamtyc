(ns vamtyc.data.store
  (:require [clojure.string :as str]
            [next.jdbc.sql :as sql]))

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
  ([tx resourceType id res]
   (let [entity {:id id :resource res}]
    (sql/insert! tx resourceType entity)
    (process entity resourceType)))
  ([tx resourceType, res]
   (let [id (str (java.util.UUID/randomUUID))]
     (create tx resourceType id res))))

(defn read [tx resourceType id]
  (-> (sql/get-by-id tx resourceType id :id {})
      (process resourceType)))

(defn update [tx resourceType id res]
  (sql/update! tx resourceType {:resource res} {:id id})
  (-> {:id id :resource res}
      (process resourceType)))

(defn upsert [tx resourceType id res]
  (let [entity (read tx resourceType id)]
    (if entity
      (update tx resourceType id res)
      (create tx resourceType id res))))

(defn delete [tx resourceType id]
  (let [entity  (read resourceType id)]
    (sql/delete! tx resourceType {:id id})
    entity))

(defn list
  ([tx resourceType]
   (let [table (name resourceType)
         limit 128
         sql-query (str "SELECT * FROM " table " LIMIT ?")]
     (->> (sql/query tx [sql-query limit])
          (map (fn [entity] (process entity resourceType )))))))
