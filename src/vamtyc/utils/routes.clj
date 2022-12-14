(ns vamtyc.utils.routes
  (:require [clojure.string :as str]))

(defn match-code? [path-cmp code]
  (-> path-cmp :code keyword (= code)))

(defn component [path code]
  (let [path-cmp (filter #(match-code? % code) path)]
    (-> path-cmp first :value)))

(defn type [path]
  (-> path (component :/Coding/wellknown-params?code=type) keyword))

(defn id [path]
  (component path :/Coding/wellknown-params?code=id))

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
