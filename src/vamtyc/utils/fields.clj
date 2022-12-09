(ns vamtyc.utils.fields
  (:require [clojure.string :as str]))

(defn flat-fields [fields-param]
  (cond
    (= fields-param "")
      []
    (string? fields-param)
      (->> (str/split fields-param #",")
           (map #(str/split % #"\."))
           (into []))
    (vector? fields-param)
      (->> (mapcat flat-fields fields-param)
           (into []))
    :else
      []))

(defn select-path-into [path from to]
  (let [[field-str & rest]  path
        field               (keyword field-str)
        from-prop           (field from)]
    (cond
      (empty? rest)
        (assoc to field from-prop)
      (vector? from-prop)
        (->> (or (field to) (map (fn [_] (hash-map)) from-prop))
             (map #(select-path-into rest %1 %2) from-prop)
             (into [])
             (assoc to field))
      :else
        (->> (field to)
             (or {})
             (select-path-into rest from-prop)
             (assoc to field)))))

(defn select-fields [obj field-expr]
  (let [fields (flat-fields field-expr)]
    (if (empty? fields) obj
        (reduce #(select-path-into %2 obj %1) {} fields))))
