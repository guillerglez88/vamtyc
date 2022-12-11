(ns vamtyc.queries.utils
  (:require [honey.sql.helpers :refer [select from inner-join]]
            [vamtyc.utils.routes :as routes]
            [compojure.route :as route]))

(defn make-sql-map [res-type]
    (-> (select :id :resource :created :modified)
        (from res-type)))

(defn make-prop-alias
  ([jsonb path-elem suffix]
   (let [prop-name (:name path-elem)]
     (-> (name jsonb)
         (str "_" prop-name suffix)
         (keyword))))
  ([jsonb path-elem]
   (make-prop-alias jsonb path-elem "")))

(defn jsonb-extract-prop [sql-map jsonb path-elem alias]
  (let [prop            (:name path-elem)]
    (-> sql-map
        (inner-join [[:jsonb_extract_path jsonb prop] alias] (= 1 1)))))

(defn jsonb-extract-coll [sql-map jsonb path-elem alias]
  (let [prop            (:name path-elem)
        prop-alias      (make-prop-alias jsonb path-elem)]
    (-> sql-map
        (jsonb-extract-prop jsonb path-elem prop-alias)
        (inner-join [[:jsonb_array_elements prop-alias] alias] (= 1 1)))))

(defn make-env-params [env]
  (reduce #(assoc %1 (str "env/" (name %2)) (%2 env)) {} (keys env)))

(defn get-queryp-default-str [queryp]
  (let [default (:default queryp)]
    (cond
      (keyword? default) (name default)
      (nil? default) ""
      :else (str default))))

(defn make-queryp-params [query-params]
  (->> (map #(vector (-> % :name name)
                     (get-queryp-default-str %)) query-params)
       (into {})))

(defn make-params [req route query-params env]
  (let [req-params    (:params req)
        route-params  (-> route :path routes/make-params)
        queryp-params (make-queryp-params query-params)]
    (-> (make-env-params env)
        (merge queryp-params)
        (merge route-params)
        (merge req-params))))
