(ns vamtyc.queries.offset
  (:require
   [honey.sql.helpers :refer [offset]]
   [vamtyc.param :as param]))

(defn apply-queryp [sql-map req _queryp]
  (-> (:vamtyc/param req)
      (param/get-value "/Coding/wellknown-params?code=offset")
      (str)
      (Integer/parseInt)
      (#(offset sql-map %))))
