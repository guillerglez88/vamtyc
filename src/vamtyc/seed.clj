(ns vamtyc.seed
  (:require
   [clojure.edn :as edn]
   [vamtyc.data.store :as store]
   [vamtyc.data.datasource :refer [ds]]
   [next.jdbc :as jdbc]
   [clojure.java.io :as io]
   [vamtyc.data.ddl :as ddl]))

(def wk-res-res "/Coding/wellknown-resources?code=resource")
(def wk-res-seq "/Coding/wellknown-resources?code=seq")
(def allowed-methods #{:POST :PUT})

(defn hset-code [code]
  (-> (vector? code)
      (if code [code])
      ((partial apply hash-set))))

(defn assoc-by-code [res]
  (->> (:code res)
       (hset-code)
       (map #(vector % res))
       (into {})))

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

(defn collect-ddls [acc curr]
  (-> (:body curr)
      (assoc-by-code)
      (select-keys [ddl/wk-create-storage ddl/wk-create-sequence])
      (merge acc)))

(defn commit-trn-item [tx ddls res]
  (let [res-type (:type res)
        id (:id res)
        of (:of res)
        code-set (-> res :code hset-code)
        storage-ddl (get ddls ddl/wk-create-storage)
        sequence-ddl (get ddls ddl/wk-create-sequence)]
    (create-storage tx res storage-ddl)
    (cond
      (code-set wk-res-seq) (create-sequence tx res sequence-ddl)
      (code-set wk-res-res) (create-storage tx {:type of} storage-ddl)
      (nil? id)             (store/create tx res-type res)
      :else                 (store/create tx res-type id res))))

(defn commit-trn [trn]
  (jdbc/with-transaction [tx ds]
    (loop [[curr & rest] (:items trn)
           acc {}]
      (let [ddls (collect-ddls acc curr)
            method-allowed? (->> curr :method (contains? allowed-methods))
            body (-> curr (or {}) :body)]
        (cond
          (nil? curr)           (hash-map :status :ok)
          (not method-allowed?) (throw (Exception. (str  "Method " (:method curr) " not allowed while booting app")))
          :else                 (do (commit-trn-item tx ddls body)
                                    (recur rest ddls)))))))
        

(defn init []
  (when-not (is-already-init?)
    (-> (io/resource "bootstrap.edn")
        (slurp)
        (edn/read-string)
        (commit-trn))))

(comment
  (init))
