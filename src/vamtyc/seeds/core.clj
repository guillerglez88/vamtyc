(ns vamtyc.seeds.core
  (:require
   [clojure.edn :as edn]
   [vamtyc.data.store :as store]
   [vamtyc.data.datasource :refer [ds]]
   [next.jdbc :as jdbc]
   [clojure.string :as str]))

(defn is-already-init? []
  (try
    (store/search ds :Resource)
    true
    (catch Exception _ false)))

(defn make-storage-ddl [type]
  (let [type-name (-> type name str/lower-case)]
    [(str "CREATE TABLE IF NOT EXISTS public." type-name " (
       id          TEXT             NOT NULL,
       resource    JSONB            NOT NULL,
       created     timestamptz      NOT NULL,
       modified    timestamptz      NOT NULL,
       CONSTRAINT  " type-name "_pk PRIMARY KEY (id));")]))

(defn allocate-storage [tx type]
  (->> (make-storage-ddl type)
       (jdbc/execute! tx)))

(defn commit-boot-trn-item [tx res]
  (let [res-type    (:type res)
        id          (:id res)
        type        (:of res)]
    (when (= :Resource res-type)
      (allocate-storage tx type))
    (if (nil? id)
      (store/create tx res-type res)
      (store/create tx res-type id res))))

(defn commit-boot-trn [trn]
  (jdbc/with-transaction [tx ds]
    (doseq [item (:items trn)]
      (if (contains? #{:POST :PUT} (:method item))
        (->> (:body item)
             (commit-boot-trn-item tx))
        (throw (Exception. (str  "Method " (:method item) " not allowed while booting app")))))))

(defn init []
  (let [boot-file "./src/vamtyc/seeds/bootstrap.edn"]
    (when-not (is-already-init?)
      (-> (slurp boot-file)
          (edn/read-string)
          (commit-boot-trn)))))
