(ns vamtyc.seed
  (:require
   [clojure.edn :as edn]
   [vamtyc.data.store :as store]
   [vamtyc.data.datasource :refer [ds]]
   [next.jdbc :as jdbc]
   [clojure.java.io :as io]
   [vamtyc.data.ddl :as ddl]))

(defn is-already-init? []
  (try
    (store/search ds :Resource)
    true
    (catch Exception _ false)))

(defn create-storage [tx res ddl]
  (let [tpl (:template ddl)]
    (->> (ddl/make-res-ddl tpl res)
         (vector)
         (jdbc/execute! tx))))

(defn create-sequence [tx seq ddl]
  (let [tpl (:template ddl)]
    (->> (ddl/make-seq-ddl tpl seq)
         (vector)
         (jdbc/execute! tx))))

(defn commit-boot-trn-item [tx ddls res]
  (let [res-type (:type res)
        id (:id res)
        code (:code res)]
    (when (= "/Coding/wellknown-resources?code=resource" code)
      (->> (get ddls ddl/wk-create-storage)
           (create-storage tx res)))
    (when (and (vector? code)
               (contains? code "/Coding/wellknown-resources?code=seq-etag"))
      (->> (get ddls ddl/wk-create-sequence)
           (create-sequence tx res)))
    (if (nil? id)
      (store/create tx res-type res)
      (store/create tx res-type id res))))

(defn assoc-by-code [res]
  (let [code (:code res)]
    (->> (if (vector? code) code [code])
         (map #(vector % res))
         (into {}))))

(defn map-keep-ddls [items]
  (-> (fn [[ddls & acc], curr]
        (-> (:body curr)
            (assoc-by-code)
            (merge ddls)
            (select-keys [ddl/wk-create-storage ddl/wk-create-sequence])
            (vector)
            (concat acc [curr])))
      (reduce [] items)))

(defn commit-boot-trn [trn]
  (jdbc/with-transaction [tx ds]
    (doseq [[ddls & item] (-> trn :items map-keep-ddls)]
      (if (contains? #{:POST :PUT} (:method item))
        (->> (:body item)
             (commit-boot-trn-item tx ddls))
        (throw (Exception. (str  "Method " (:method item) " not allowed while booting app")))))))

(defn init []
  (when-not (is-already-init?)
    (-> (io/resource "bootstrap.edn")
        (slurp)
        (edn/read-string)
        (commit-boot-trn))))
