(ns vamtyc.utils.params)

(defn extract-param-names [req]
  (->> (-> req :params keys (or []))
       (map name)
       (into [])))

(defn queryp-val-str [queryp]
  (let [default (:value queryp)]
    (cond
      (keyword? default) (name default)
      (nil? default) ""
      :else (str default))))

(defn queryp-to-params [query-params]
  (->> (map #(vector (-> % :name name)
                     (queryp-val-str %)) query-params)
       (into {})))

(defn route-to-params [route]
  (let [path (:path route)]
    (->> (map #(vector (:name %) (:value %)) path)
         (into {}))))

(defn req-to-params [req]
  (->> (:params req)
       (seq)
       (map #(vector (-> % first name) (second %)))
       (into {})))
