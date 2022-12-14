(ns vamtyc.utils.params)

(defn extract-param-names [req]
  (->> (-> req :params keys (or []))
       (map name)
       (into [])))

(defn env-params [env]
  (reduce #(assoc %1 (str "env/" (name %2)) (%2 env)) {} (keys env)))

(defn queryp-val-str [queryp]
  (let [default (:value queryp)]
    (cond
      (keyword? default) (name default)
      (nil? default) ""
      :else (str default))))

(defn queryp-params [query-params]
  (->> (map #(vector (-> % :name name)
                     (queryp-val-str %)) query-params)
       (into {})))

(defn route-params [route]
  (let [path (:path route)]
    (->> (map #(vector (:name %) (:value %)) path)
         (into {}))))

(defn req-params [req]
  (->> (:params req)
       (seq)
       (map #(vector (-> % first name) (second %)))
       (into {})))

(defn make-params [req env route queryp]
  (-> (env-params env)
      (merge (queryp-params queryp))
      (merge (route-params route))
      (merge (req-params req))))
