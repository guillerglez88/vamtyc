(ns vamtyc.req.param
  (:require
    [clojure.string :as str]))

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
       (reduce merge {})))

(defn sanityze-qs-name [qs-name]
  (-> (name qs-name)
      (str/split #":")
      (first)
      (keyword)))

(defn url [req]
  (str (:uri req)
       (when-let [query (:query-string req)]
         (str "?" query))))

(defn req->param [req]
  (->> (or (:params req) {})
       (seq)
       (map (fn [[key val]] (-> (sanityze-qs-name key)
                                (vector val))))
       (into {:__url (url req)})))
