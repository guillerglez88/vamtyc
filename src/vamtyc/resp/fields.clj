(ns vamtyc.resp.fields
  (:require
   [clojure.string :as str]))

(defn parse-expr [expr]
  (->> (str/split (or expr "") #",")
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
  (let [[current & rest] path
        prop             (keyword current)
        value            (prop from)]
    (cond
      (nil? value)    to
      (empty? rest)   (assoc to prop value)
      (vector? value) (->> (or (prop to) (repeat (count value) {}))
                           (map #(select-path-into rest %1 %2) value)
                           (into [])
                           (assoc to prop))
      :else           (->> (or (prop to) {})
                           (select-path-into rest value)
                           (assoc to prop)))))

(defn select-fields [map expr]
  (let [fields (parse-expr expr)]
    (if (empty? fields) map
        (reduce #(select-path-into %2 map %1) {} fields))))
