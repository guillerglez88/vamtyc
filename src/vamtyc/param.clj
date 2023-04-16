(ns vamtyc.param
  (:require
   [clojure.string :as str]
   [lambdaisland.uri :refer [query-map]]))

(def wellknown-type   "/Coding/wellknown-params?code=type")
(def wellknown-id     "/Coding/wellknown-params?code=id")
(def wellknown-of     "/Coding/wellknown-params?code=of")
(def wellknown-fields "/Coding/wellknown-params?code=fields")
(def wellknown-limit  "/Coding/wellknown-params?code=limit")
(def wellknown-offset "/Coding/wellknown-params?code=offset")
(def wellknown-sort   "/Coding/wellknown-params?code=sort")

(defn queryp->param [queryp]
  (let [key (-> queryp :name name)
        val (:value queryp)
        code (:code queryp)]
    (hash-map key val
              :vamtyc/codes [(str code "&name=" key)])))

(defn merge-param [params]
  (reduce #(->> (concat (:vamtyc/codes %1) (:vamtyc/codes %2))
                (vec)
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

(defn get-value [param code]
  (->> (:vamtyc/codes param)
       (filter #(str/starts-with? % code))
       (map query-map)
       (map #(get param (-> % :name name)))
       (first)))
