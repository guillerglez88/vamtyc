(ns vamtyc.req.param)

(defn queryp->param [queryp]
  (let [key (:name queryp)
        val (:value queryp)]
    (hash-map key val)))

(defn queryps->param [queryps]
  (->> (or queryps [])
       (map #(queryp->param %))
       (reduce merge)))

(defn route-path-cmp->param [cmp]
  (let [key (:name cmp)
        val (:value cmp)]
    (hash-map key val)))

(defn route->param [route]
  (->> (or (:path route) [])
       (map route-path-cmp->param)
       (reduce merge)))
