(ns vamtyc.data.datasource
  (:require [clojure.data.json :as json]
            [next.jdbc :as jdbc]
            [next.jdbc.prepare :as prepare]
            [next.jdbc.result-set :as rs]
            [next.jdbc.date-time] ;; import-only, side effect!
            [vamtyc.config.env :refer [env]]))

(import '(org.postgresql.util PGobject))
(import '(java.sql PreparedStatement))

(set! *warn-on-reflection* true)

(defn to-jsonb
  [m]
  (let [type  (-> m meta :pgtype (or "jsonb"))
        value (json/write-str m)]
    (doto (PGobject.)
      (.setType type)
      (.setValue value))))

(defn from-jsonb
  [^org.postgresql.util.PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"json" "jsonb"} type)
      (when value
        (-> value
            (json/read-str :key-fn keyword)
            (with-meta {:pgtype type}))))))

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

(def ds (jdbc/get-datasource (:DB_CNX_STR env)))
