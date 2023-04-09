(ns vamtyc.query
  (:require
   [clojure.string :as str]
   [honey.sql.helpers :refer [from inner-join limit offset select where]]
   [vamtyc.param :as param]))

(defn make-sql-map [type]
    (-> (select :id :resource :created :modified)
        (from type)))

(defn make-prop-alias
  ([base path-elem suffix]
   (let [prop-name  (:name path-elem)]
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
  (let [prop-alias  (make-prop-alias base path-elem)]
    (-> (identity sql-map)
        (extract-prop base path-elem prop-alias)
        (inner-join [[:jsonb_array_elements prop-alias] alias] true))))

(defn extract-path [sql-map base path alias]
  (let [[curr & more]   path
        suffix          (when (:collection curr) "elem")
        curr-alias      (if (empty? more) alias (make-prop-alias base curr suffix))]
    (cond
      (nil? curr)           sql-map
      (:meta curr)          sql-map ;; TODO: implement meta fields access
      (:collection curr)    (-> (identity sql-map)
                                (extract-coll base curr curr-alias)
                                (extract-path curr-alias more alias))
      :else                 (-> (identity sql-map)
                                (extract-prop base curr curr-alias)
                                (extract-path curr-alias more alias)))))

(defn contains-text [sql-map req queryp]
  (let [name (-> queryp :name name)
        db-name (-> name (str/replace #"-" "_") keyword)
        val (-> req :vamtyc/param (get name))]
    (where sql-map [:like [:cast db-name :text] (str "%" val "%")])))


(defn match-exact [sql-map req queryp]
  (let [name (-> queryp :name name)
        db-name (-> name (str/replace #"-" "_") keyword)
        val (-> req :vamtyc/param (get name))]
    (where sql-map [:= [:cast db-name :text] (str "\"" val "\"")])))


(defn page-offset [sql-map req _queryp]
  (-> (:vamtyc/param req)
      (param/get-value "/Coding/wellknown-params?code=offset")
      (str)
      (Integer/parseInt)
      (#(offset sql-map %))))

(defn page-size [sql-map req _queryp]
  (->> (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=limit"))
       (limit sql-map)))

(defn order-by [sql-map _req _queryp]
  sql-map)

(def filters
  {"/Coding/wellknown-params?code=limit"   page-size
   "/Coding/wellknown-params?code=offset"  page-offset
   "/Coding/wellknown-params?code=sort"    order-by
   "/Coding/filters?code=text"             contains-text
   "/Coding/filters?code=keyword"          match-exact})

(defn refine-query [req sql-map queryp]
  (let [path (-> queryp :path (or []))
        db-name (-> queryp :name name (str/replace #"-" "_") keyword)
        refine (-> queryp :code (#(get filters %)) (or (fn [sql-map _ _] sql-map)))]
    (-> (identity sql-map)
        (extract-path :resource path db-name)
        (refine req queryp))))

(defn search-query [req _tx]
  (let [queryp (-> req :vamtyc/queryp)
        type (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=type"))
        of (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=of"))
        sql-map (make-sql-map (or of type))]
    (reduce #(refine-query req %1 %2) sql-map queryp)))
