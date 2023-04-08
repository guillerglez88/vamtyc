(ns vamtyc.req.param
  (:require
    [clojure.string :as str]))

(defn queryp->param [queryp]
  (let [key (-> queryp :name name)
        val (:value queryp)
        code (:code queryp)]
    (hash-map key val
              :vamtyc/codes [(str code "&name=" key)])))

(defn merge-param [params]
  (reduce #(->> (concat (:vamtyc/codes %1) (:vamtyc/codes %2))
                (hash-map :vamtyc/codes)
                (merge %1 %2)) {} params))

(defn queryps->param [queryps]
  (->> (or queryps [])
       (map #(queryp->param %))
       (merge-param)))

(defn route-path-cmp->param [cmp]
  (let [key (-> cmp :name name)
        val (:value cmp)
        code (:code cmp)]
    (hash-map key val
              :vamtyc/codes [(str code "&name=" key)])))

(defn route->param [route]
  (->> (or (:path route) [])
       (map route-path-cmp->param)
       (merge-param)))

(defn sanityze-qs-name [qs-name]
  (-> (name qs-name)
      (str/split #":")
      (first)))

(defn url [req]
  (str (:uri req)
       (when-let [query (:query-string req)]
         (str "?" query))))

(defn req->param [req]
  (->> (or (:params req) {})
       (seq)
       (map (fn [[key val]] (-> (sanityze-qs-name key)
                                (vector val))))
       (into {:vamtyc/url (url req)})))