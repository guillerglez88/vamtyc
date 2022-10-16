(ns vamtyc.store
  (:require [next.jdbc :as jdbc]
            [next.jdbc.sql :as sql]
            [next.jdbc.prepare :as prepare]
            [next.jdbc.result-set :as rs]
            [clojure.data.json :as json]))

(import '(org.postgresql.util PGobject))
(import '(java.sql PreparedStatement))

(set! *warn-on-reflection* true)

(defn to-jsonb
  [m]
  (let [type (or (:pgtype (meta m)) "jsonb")
        value (json/write-str m)]
    (doto (PGobject.)
      (.setType type)
      (.setValue value))))

(defn from-jsonb
  [^org.postgresql.util.PGobject v]
  (let [type (.getType v)
        value (.getValue v)]
    (if (#{"json" "jsonb"} type)
      (when value
        (with-meta (json/read-str value :key-fn keyword) {:pgtype type})))))

(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [m ^PreparedStatement s i]
    (.setObject s i (to-jsonb m)))

  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (to-jsonb v))))

(extend-protocol rs/ReadableColumn
  org.postgresql.util.PGobject
  (read-column-by-label [^org.postgresql.util.PGobject v _]
    (from-jsonb v))
  (read-column-by-index [^org.postgresql.util.PGobject v _2 _3]
    (from-jsonb v)))

(def db {:dbtype "postgresql"
         :host "localhost"
         :port 5432
         :user "postgres"
         :password "postgres"
         :dbname "vamtyc"})

(def ds (jdbc/get-datasource db))

;; TODO: implement CURD operations

(defn create [entity, res]
  (let [id (java.util.UUID/randomUUID)]
    (sql/insert! ds entity {:id id :resource res})))

(defn update [entity, id, res]
  (let [uuid (parse-uuid id)]
    (sql/update! ds entity {:resource res} {:id uuid})))

(comment
  ;; pgsql
  (jdbc/execute! ds ["CREATE TABLE public.person (
                        id uuid NOT NULL,
                        resource jsonb NULL,
                        CONSTRAINT person_pk PRIMARY KEY (id)
                    );"])
  (jdbc/execute! ds ["SELECT * FROM public.person"])
  ;; CRUD
  (create :person {:name [{:given "John" :family ["Doe"]}]})
  (update :person "ab165937-f667-43f1-933a-f491e4cbddbc" {:name [{:given "John" :family ["Smith"]}]})
  ;; utils
  (java.util.UUID/randomUUID)
  ;; jsonb
  (def pgobjct
    (to-jsonb {:name [{:given "John" :family ["Doe"]}]}))
  (from-jsonb pgobjct)
  )
