(ns vamtyc.data.queryp
  (:require
   [honey.sql :as sql]
   [vamtyc.data.store :as store]
   [honey.sql.helpers :refer [select from where]]))

(defn queryps-sql [types names]
  (let [of-list (into [] (filter #(not (nil? %)) types))]
    (-> (select :*)
        (from :Queryp)
        (where [:or [:and [:in [:jsonb_extract_path_text :resource "of"] of-list]
                          [:in [:jsonb_extract_path_text :resource "name"] names]]
                    [:<> [:jsonb_extract_path_text :resource "default"] nil]])
        (sql/format))))

(defn or-something [param-names]
  (if (or (nil? param-names)
          (empty? param-names))
    ["__0f610cba__"]
    param-names))

(defn str-res-types [res-types]
  (->> (conj res-types :*)
       (filter (complement nil?))
       (map name)))

(defn load-queryps [tx res-types param-names]
  (let [types (str-res-types res-types)]
    (->> (or-something param-names)
         (queryps-sql types)
         (store/search tx :Queryp)
         (vec))))
