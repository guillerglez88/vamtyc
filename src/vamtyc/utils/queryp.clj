(ns vamtyc.utils.queryp)

(defn resolve-queryp [params queryp]
  (let [qp-name (-> queryp :name name)]
    (->> (:value queryp)
         (or (get params qp-name))
         (hash-map :value)
         (merge queryp))))

(defn resolve-queryps [params queryps]
  (-> (partial resolve-queryp params)
      (map queryps)
      (into [])))
