(ns vamtyc.utils.routes
  (:require [clojure.string :as str]))

(defn is-res-type? [path-cmp]
  (-> path-cmp :name (= "_type")))

(defn get-res-type [path]
  (let [path-cmp (filter is-res-type? path)]
    (-> path-cmp first :value)))

(defn str-path [path]
  (->> path
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (str/join "/")
       (str "/")))

(defn calc-match-index [path]
  (->> (filter #(contains? % :value) path)
       (count)))
