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

(comment
  (is-res-type? {:name "resourceType"})
  (is-res-type? {:name "id"})
  ;;
  (get-res-type [{:name "resourceType" :value "Resource"}
                 {:name "id"}])
  (get-res-type [{:name "tenant"        :value "instance1"}
                 {:name "resourceType"  :value "Resource"}
                 {:name "id"}])
  ;;
  (stringify [{:name "resourceType"  :value "Resource"}])
  (stringify [{:name "tenant"        :value "instance1"}
              {:name "resourceType"  :value "Resource"}
              {:name "id"}])
  )
