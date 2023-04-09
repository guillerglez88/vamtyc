(ns vamtyc.queries.core
  (:require
   [clojure.string :as str]
   [vamtyc.queries.fields :as fields]
   [vamtyc.queries.keyword :as keyw]
   [vamtyc.queries.limit :as limit]
   [vamtyc.queries.of :as of]
   [vamtyc.queries.offset :as offset]
   [vamtyc.queries.sort :as sort]
   [vamtyc.queries.text :as text]
   [vamtyc.queries.utils :as utils]
   [vamtyc.req.param :as param]))

(def filters
  {"/Coding/wellknown-params?code=limit"   limit/apply-queryp
   "/Coding/wellknown-params?code=offset"  offset/apply-queryp
   "/Coding/wellknown-params?code=of"      of/apply-queryp
   "/Coding/wellknown-params?code=fields"  fields/apply-queryp
   "/Coding/wellknown-params?code=sort"    sort/apply-queryp
   "/Coding/filters?code=text"             text/apply-queryp
   "/Coding/filters?code=keyword"          keyw/apply-queryp})

(defn refine-query [req sql-map queryp]
  (let [path (-> queryp :path (or []))
        db-name (-> queryp :name name (str/replace #"-" "_") keyword)
        refine (get filters (:code queryp))]
    (-> (identity sql-map)
        (utils/extract-path :resource path db-name)
        (refine req queryp))))

(defn search-query [req _tx]
  (let [queryp (-> req :vamtyc/queryp)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        of (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=of"))
        sql-map (utils/make-sql-map (or of type))]
    (reduce #(refine-query req %1 %2) sql-map queryp)))
