(ns vamtyc.data.queryparams
  (:require [honey.sql :as hsql]
            [vamtyc.data.store :as store]
            [honey.sql :as hsql]
            [honey.sql.helpers :refer [select from where]]))

(defn load-queryparams [param-names res-type tx]
  (if (empty? param-names) []
      (-> (select :*)
          (from :QueryParam)
          (where [:= [:jsonb_extract_path_text :resource "type"] (name res-type)])
          (where [:in [:jsonb_extract_path_text :resource "name"] param-names])
          (hsql/format)
          (#(store/list tx :QueryParam %)))))
