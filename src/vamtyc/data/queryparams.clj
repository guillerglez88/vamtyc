(ns vamtyc.data.queryparams
  (:require [honey.sql :as hsql]
            [vamtyc.data.store :as store]
            [honey.sql :as hsql]
            [honey.sql.helpers :refer [select from where]]))

(defn load-queryparams [param-names res-types tx]
  (let [types (-> res-types (conj :*) (->> (map name)))
        names (-> param-names (or []) (conj "__"))]
    (-> (select :*)
        (from :QueryParam)
        (where
          [:and [:in [:jsonb_extract_path_text :resource "type"] types]]
                [:or [:<> [:jsonb_extract_path_text :resource "default"] nil]
                     [:in [:jsonb_extract_path_text :resource "name"] names]])
        (hsql/format)
        (#(store/list tx :QueryParam %)))))
