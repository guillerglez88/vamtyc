(ns vamtyc.queries.utils
  (:require [honey.sql.helpers :refer [select from inner-join]]))

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
