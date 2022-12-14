(ns vamtyc.queries.core
  (:require
   [vamtyc.queries.limit :as limit]
   [vamtyc.queries.of :as of]
   [vamtyc.queries.utils :as utils]
   [vamtyc.queries.text :as text]
   [vamtyc.queries.offset :as offset]
   [vamtyc.queries.fields :as fields]
   [vamtyc.queries.keyword :as keyw]
   [vamtyc.queries.sort :as sort]
   [vamtyc.utils.routes :as uroutes]
   [vamtyc.utils.queryp :as uqueryp]))

(def filters
  {"/Coding/wellknown-params?code=limit"   limit/apply-queryp
   "/Coding/wellknown-params?code=offset"  offset/apply-queryp
   "/Coding/wellknown-params?code=of"      of/apply-queryp
   "/Coding/wellknown-params?code=fields"  fields/apply-queryp
   "/Coding/wellknown-params?code=sort"    sort/apply-queryp
   "/Coding/filters?code=text"             text/apply-queryp
   "/Coding/filters?code=keyword"          keyw/apply-queryp})

(defn refine-query [req sql-map queryp]
  (let [path    (-> queryp :path (or []))
        name    (uqueryp/queryp-name queryp)
        refine  (get filters (:code queryp))]
    (-> (identity sql-map)
        (utils/extract-path :resource path name)
        (refine req queryp))))

(defn search-query [req _tx]
  (let [queryp    (-> req :vamtyc/queryp)
        type      (-> req :vamtyc/route :path uroutes/_type)
        of        (-> req :vamtyc/queryp uqueryp/of)
        sql-map   (utils/make-sql-map (or of type))]
    (reduce #(refine-query req %1 %2) sql-map queryp)))
