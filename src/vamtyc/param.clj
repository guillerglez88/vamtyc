(ns vamtyc.param
  (:require
   [clojure.string :as str]
   [lambdaisland.uri :refer [query-map]]))

(def wkp-type     "/Coding/wellknown-params?code=type")
(def wkp-id       "/Coding/wellknown-params?code=id")
(def wkp-of       "/Coding/wellknown-params?code=of")
(def wkp-fields   "/Coding/wellknown-params?code=fields")
(def wkp-limit    "/Coding/wellknown-params?code=limit")
(def wkp-offset   "/Coding/wellknown-params?code=offset")
(def wkp-sort     "/Coding/wellknown-params?code=sort")
(def wkp-inspect  "/Coding/wellknown-params?code=inspect")

(defn queryp->param [queryp]
  (let [key (-> queryp :name name)
        val (:value queryp)
        code (:code queryp)]
    [(hash-map key val)
     (str code "&name=" key)]))

(defn merge-param
  ([params]
   (reduce merge-param [{}] params))
  ([st-params nd-params]
   (let [[st-param & st-codes] st-params
         [nd-param & nd-codes] nd-params]
     (-> (merge st-param nd-param)
         (vector)
         (concat st-codes nd-codes)
         (vec)))))

(defn queryps->param [queryps]
  (->> (or queryps [])
       (map #(queryp->param %))
       (merge-param)))

(defn route-path-cmp->param [cmp]
  (let [key (-> cmp :name name)
        val (:value cmp)
        code (:code cmp)]
    [(hash-map key val)
     (str code "&name=" key)]))

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
       (into {})
       (vector)))

(defn get-value [params code]
  (let [[param & codes] params]
    (->> (filter #(str/starts-with? % code) codes)
         (map query-map)
         (map #(get param (-> % :name name)))
         (first))))

(defn get-values [param & codes]
  (-> (partial get-value param)
      (map codes)
      (vec)))
