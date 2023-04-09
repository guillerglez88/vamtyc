(ns vamtyc.queries.limit
  (:require
   [honey.sql.helpers :refer [limit]]
   [vamtyc.param :as param]))

(defn apply-queryp [sql-map req _queryp]
  (->> (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=limit"))
       (limit sql-map)))
