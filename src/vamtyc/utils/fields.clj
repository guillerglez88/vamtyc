(ns vamtyc.utils.fields
  (:require [clojure.string :as str]))

(defn parse-expr [expr]
  (->> (str/split expr #",")
       (map #(str/split % #"\."))
       (map #(into [] (filter (fn [cmp] (not (str/blank? cmp))) %)))
       (filter #(seq %))
       (into [])))

(defn flat-expr [expr]
  (cond
    (vector? expr)  (str/join "," expr)
    (nil? expr)     ""
    :else           expr))

(defn select-path-into [path from to]
  (let [[field-str & rest]  path
        field               (keyword field-str)
        from-prop           (field from)]
    (cond
      (nil? from-prop)      to
      (empty? rest)         (assoc to field from-prop)
      (vector? from-prop)   (->> (or (field to) (repeat (count from-prop) {}))
                                 (map #(select-path-into rest %1 %2) from-prop)
                                 (into [])
                                 (assoc to field))
      :else                 (->> (or (field to) {})
                                 (select-path-into rest from-prop)
                                 (assoc to field)))))

(defn select-fields [map expr]
  (let [fields (parse-expr expr)]
    (if (empty? fields) map
        (reduce #(select-path-into %2 map %1) {} fields))))
