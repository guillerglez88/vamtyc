(ns vamtyc.utils.queryp
  (:require [clojure.string :as str]))

(defn queryp-name [queryp]
  (-> (:name queryp)
      (name)
      (str/replace #"-" "_")
      keyword))

(defn resolve-queryp [params queryp]
  (let [qp-name (-> queryp :name name)]
    (->> (:value queryp)
         (or (get params qp-name))
         (hash-map :value)
         (merge queryp))))

(defn resolve-queryps [params def-queryps req-queryps]
  (-> (partial resolve-queryp params)
      (map req-queryps)
      (concat def-queryps)
      (->> (group-by #(:name %)))
      (vals)
      (->> (map first))
      (into [])))

(defn find-by-code [queryps code]
  (->> (map #(hash-map :code (-> % :code keyword)
                       :value (:value %)) queryps)
       (filter #(= code (:code %)))
       (first)
       (:value)))


(defn of [queryps]
  (-> (identity queryps)
      (find-by-code :/Coding/wellknown-params?code=of)
      (keyword)))

(defn fields [queryps]
  (-> (identity queryps)
      (find-by-code :/Coding/wellknown-params?code=fields)))
