(ns vamtyc.req.param)

(defn queryp->param [queryp]
  (let [key (:name queryp)
        val (:value queryp)]
    (hash-map key val)))

(defn queryps->param [queryps]
  (->> (or queryps [])
       (map #(queryp->param %))
       (reduce merge)))
