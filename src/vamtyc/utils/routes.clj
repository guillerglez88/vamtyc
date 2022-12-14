(ns vamtyc.utils.routes
  (:require [clojure.string :as str]))

(defn res-type? [path-cmp]
  (-> path-cmp :code keyword (= :/Coding/wellknown-params?code=type)))

(defn get-res-type [path]
  (let [path-cmp (filter res-type? path)]
    (-> path-cmp first :value)))

(defn str-path [path]
  (->> path
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (str/join "/")
       (str "/")))

(defn calc-match-index [path]
  (->> (filter #(contains? % :value) path)
       (count)))
