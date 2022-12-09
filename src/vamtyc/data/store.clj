(ns vamtyc.data.store
  (:require [clojure.string :as str]
            [next.jdbc.sql :as sql]
            [honey.sql :as hsql]
            [honey.sql.helpers :refer [select from where limit] :as h]
            [vamtyc.config.env :refer [env]]))

(defn process [entity res-type]
  (when entity
    (let [res-name    (name res-type)
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
  ([tx res-type id res]
   (let [entity {:id id :resource res}]
    (sql/insert! tx res-type entity)
    (process entity res-type)))
  ([tx res-type, res]
   (let [id (str (java.util.UUID/randomUUID))]
     (create tx res-type id res))))

(defn read [tx res-type id]
  (-> (sql/get-by-id tx res-type id :id {})
      (process res-type)))

(defn update [tx res-type id res]
  (sql/update! tx res-type {:resource res} {:id id})
  (-> {:id id :resource res}
      (process res-type)))

(defn upsert [tx res-type id res]
  (let [entity (read tx res-type id)]
    (if entity
      (update tx res-type id res)
      (create tx res-type id res))))

(defn delete [tx res-type id]
  (let [entity  (read tx res-type id)]
    (sql/delete! tx res-type {:id id})
    entity))

(defn list
  ([tx res-type sql]
   (->> (sql/query tx sql)
        (map #(process % res-type))))
  ([tx res-type]
   (-> (select :*)
       (from res-type)
       (limit (-> env :LIMIT Integer/parseInt))
       (hsql/format)
       (#(list tx res-type %)))))

(defn count [tx sql-map]
  (-> sql-map
      (dissoc :select)
      (select [[:count :*] :count])
      (hsql/format)
      (->> (sql/query tx))))
