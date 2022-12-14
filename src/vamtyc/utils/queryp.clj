(ns vamtyc.utils.queryp)

(defn resolve [params queryp]
  (let [qp-name (-> queryp :name name)]
    (->> (:value queryp)
         (or (get params qp-name))
         (hash-map :value)
         (merge queryp))))
