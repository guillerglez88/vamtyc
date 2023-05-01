(ns vamtyc.data.store
  (:require
   [clojure.string :as str]
   [next.jdbc.sql :as sql]
   [honey.sql :as hsql]
   [honey.sql.helpers :refer [select from limit]]
   [vamtyc.env :refer [env]])
  (:import [java.time Instant]))

(defn- process [entity res-type]
  (when entity
    (let [res-name      (name res-type)
          res-name-lc   (str/lower-case res-name)
          id-key        (keyword res-name-lc "id")
          res-key       (keyword res-name-lc "resource")
          created-key   (keyword res-name-lc "created")
          modified-key  (keyword res-name-lc "modified")
          id            (or (id-key entity) (:id entity))
          res           (or (res-key entity) (:resource entity))
          created       (or (created-key entity) (:created entity))
          modified      (or (modified-key entity) (:modified entity))
          url           (str "/" res-name "/" id)]
      (merge res {:type     res-name
                  :id       id
                  :url      url
                  :created  (.toString created)
                  :modified (.toString modified)}))))

(defn create
  ([tx res-type id res]
   (let [now    (Instant/now)
         entity {:id id :resource res :created now :modified now}]
    (sql/insert! tx res-type entity)
    (process entity res-type)))
  ([tx res-type, res]
   (let [id (str (java.util.UUID/randomUUID))]
     (create tx res-type id res))))

(defn fetch [tx res-type id]
  (-> (sql/get-by-id tx res-type id :id {})
      (process res-type)))

(defn edit [tx res-type id res]
  (let [stored  (fetch tx res-type id)
        created (-> stored :created)
        now     (Instant/now)]
    (sql/update! tx res-type {:resource res :modified now} {:id id})
    (-> {:id id :resource res :modified now :created created}
        (process res-type))))

(defn upsert [tx res-type id res]
  (if (fetch tx res-type id)
    (edit tx res-type id res)
    (create tx res-type id res)))

(defn delete [tx res-type id]
  (let [entity  (fetch tx res-type id)]
    (sql/delete! tx res-type {:id id})
    entity))

(defn search
  ([tx res-type sql]
   (->> (sql/query tx sql)
        (map #(process % res-type))))
  ([tx res-type]
   (-> (select :*)
       (from res-type)
       (limit (-> env :LIMIT Integer/parseInt))
       (hsql/format)
       (#(search tx res-type %)))))
