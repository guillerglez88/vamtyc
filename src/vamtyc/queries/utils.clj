(ns vamtyc.queries.utils
  (:require [honey.sql.helpers :refer [select from inner-join]]
            [compojure.route :as route]
            [clojure.string :as str]))

(defn make-sql-map [type]
    (-> (select :id :resource :created :modified)
        (from type)))

(defn make-prop-alias
  ([base path-elem suffix]
   (let [prop-name  (:name path-elem)
         suffix?     (str/blank? suffix)]
     (-> (name base)
         (str "_" prop-name (when suffix "_") suffix)
         (str/trimr)
         (keyword))))
  ([base path-elem]
   (make-prop-alias base path-elem nil)))

(defn extract-prop [sql-map base path-elem alias]
  (let [prop (:name path-elem)]
    (inner-join sql-map [[:jsonb_extract_path base prop] alias] true)))

(defn extract-coll [sql-map base path-elem alias]
  (let [prop        (:name path-elem)
        prop-alias  (make-prop-alias base path-elem)]
    (-> (identity sql-map)
        (extract-prop base path-elem prop-alias)
        (inner-join [[:jsonb_array_elements prop-alias] alias] true))))

(defn extract-path [sql-map base path alias]
  (let [[curr & rest]   path
        suffix          (when (:collection curr) "elem")
        curr-alias      (if (empty? rest) alias (make-prop-alias base curr suffix))]
    (cond
      (nil? curr)           sql-map
      (:meta curr)          sql-map ;; TODO: implement meta fields access
      (:collection curr)    (-> (identity sql-map)
                                (extract-coll base curr curr-alias)
                                (extract-path curr-alias rest alias))
      :else                 (-> (identity sql-map)
                                (extract-prop base curr curr-alias)
                                (extract-path curr-alias rest alias)))))
