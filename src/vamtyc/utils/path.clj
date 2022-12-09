(ns vamtyc.utils.path
  (:require [clojure.string :as str]))

(defn is-res-type? [path-cmp]
  (-> path-cmp :name (= "resourceType")))

(defn get-res-type [path]
  (let [path-cmp (filter is-res-type? path)]
    (-> path-cmp first :value)))

(defn stringify [path]
  (->> path
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (str/join "/")
       (str "/")))

(defn res-type [resourceType]
  {:name "resourceType" :value resourceType})

(defn id []
  {:name "id"})

(defn calc-match-index [path]
  (->> (filter #(contains? % :value) path)
       (count)))
