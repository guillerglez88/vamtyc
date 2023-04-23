(ns vamtyc.seed
  (:require
   [clojure.edn :as edn]
   [vamtyc.data.store :as store]
   [vamtyc.data.datasource :refer [ds]]
   [next.jdbc :as jdbc]
   [clojure.string :as str]
   [clojure.java.io :as io]))

(defn is-already-init? []
  (try
    (store/search ds :Resource)
    true
    (catch Exception _ false)))

(defn allocate-storage [tx res]
  (->> (:sql res)
       (str/join "\n")
       (vector)
       (jdbc/execute! tx)))

(defn commit-boot-trn-item [tx res]
  (let [res-type (:type res)
        id (:id res)
        code (:code res)]
    (when (= "/Coding/wellknown-resources?code=resource-storage" code)
      (allocate-storage tx res))
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
  (when-not (is-already-init?)
    (-> (io/resource "bootstrap.edn")
        (slurp)
        (edn/read-string)
        (commit-boot-trn))))

(comment
  (init))
