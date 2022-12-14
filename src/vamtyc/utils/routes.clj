(ns vamtyc.utils.routes
  (:require [clojure.string :as str]))

(defn res-type? [path-cmp]
  (-> path-cmp :code keyword (= :/Coding/wellknown-params?code=type)))

(defn get-res-type [path]
  (let [path-cmp (filter res-type? path)]
    (-> path-cmp first :value keyword)))

(defn str-path [path]
  (->> path
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (str/join "/")
       (str "/")))

(defn calc-match-index [path]
  (->> (filter #(contains? % :value) path)
       (count)))

(defn resolve [params route]
  (->> (or (:path route) [])
       (map #(if (contains? % :value) %
                 (->> (:name %)
                      (get params)
                      (assoc % :value))))
       (into [])
       (hash-map :path)
       (merge route)))
