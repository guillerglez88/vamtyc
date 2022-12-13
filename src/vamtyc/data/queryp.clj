(ns vamtyc.data.queryp
  (:require [honey.sql :as hsql]
            [vamtyc.data.store :as store]
            [honey.sql :as hsql]
            [honey.sql.helpers :refer [select from where]]))

(defn queryps-sql [types names]
  (-> (select :*)
      (from :QueryParam)
      (where [:and [:in [:jsonb_extract_path_text :resource "of"] types]]
                   [:or [:<> [:jsonb_extract_path_text :resource "default"] nil]
                        [:in [:jsonb_extract_path_text :resource "name"] names]])
        (hsql/format)))

(defn load-queryps [tx res-types param-names]
  (let [types (->> (conj res-types :*)
                   (filter #(not (nil? %)))
                   (map name))
        names (-> param-names (or ["__0f610cba__"]))]
    (->> (queryps-sql types names)
         (store/list tx :QueryParam))))
