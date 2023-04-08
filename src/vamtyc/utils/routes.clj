(ns vamtyc.utils.routes)

(defn match-code? [path-cmp code]
  (-> path-cmp :code (= code)))

(defn component [path code]
  (-> (filter #(match-code? % code) path)
      (first)
      (get :value)))

(defn _type [path]
  (-> path (component "/Coding/wellknown-params?code=type") keyword))

(defn _id [path]
  (component path "/Coding/wellknown-params?code=id"))

(defn resolve-path [route params]
  (->> (or (:path route) [])
       (map #(if (contains? % :value) %
                 (->> (:name %)
                      (get params)
                      (assoc % :value))))
       (into [])
       (hash-map :path)
       (merge route)))
