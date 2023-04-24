(ns vamtyc.query
  (:require
   [clojure.string :as str]
   [honey.sql.helpers :refer [from inner-join limit offset select where]]
   [vamtyc.param :as param]))

(def filters-text     "/Coding/filters?code=text")
(def filters-keyword  "/Coding/filters?code=keyword")
(def filters-url      "/Coding/filters?code=url")
(def filters-number   "/Coding/filters?code=number")
(def filters-date     "/Coding/filters?code=date")

(defn make-field [& parts]
  (->> parts
       (filter (complement nil?))
       (map name)
       (filter (complement str/blank?))
       (map #(str/replace % #"-" "_"))
       (str/join "_")
       (str/trimr)
       (keyword)))

(defn make-sql-map [type]
    (-> (select :id :resource :created :modified)
        (from type)))

(defn extract-prop [sql-map base field alias]
  (inner-join sql-map [[:jsonb_extract_path base field] alias] true))

(defn extract-coll [sql-map base field alias]
  (let [prop-alias (make-field base field)]
    (-> (extract-prop sql-map base field prop-alias)
        (inner-join [[:jsonb_array_elements prop-alias] alias] true))))

(defn extract-field [sql-map base path-elem alias]
  (let [curr-name (:name path-elem)]
    (cond
      (:meta path-elem)       sql-map ;; TODO: implement meta fields access
      (:collection path-elem) (extract-coll sql-map base curr-name alias)
      :else                   (extract-prop sql-map base curr-name alias))))

(defn extract-path [sql-map base path alias]
  (let [[curr & more] path
        curr-name (:name curr)
        suffix (when (:collection curr) "elem")
        curr-alias (if (empty? more) alias (make-field base curr-name suffix))]
    (if (nil? curr)
      sql-map
      (-> (extract-field sql-map base curr curr-alias)
          (extract-path curr-alias more alias)))))

(defn contains-text [sql-map queryp params]
  (let [key (-> queryp :name name)
        val (-> params (get key) name)
        field (make-field key)]
    (where sql-map [:like [:cast field :text] (str "%" val "%")])))

(defn match-exact [sql-map queryp params]
  (let [name (-> queryp :name name)
        field (make-field name)
        val (get params name)]
    (where sql-map [:= [:cast field :text] (str "\"" val "\"")])))

(defn page-offset [sql-map value]
  (->> (str value)
       (Integer/parseInt)
       (offset sql-map)))

(defn page-size [sql-map value]
  (->> (str value)
       (Integer/parseInt)
       (limit sql-map)))

(defn total [sql-map]
  (-> sql-map
      (dissoc :select :offset :limit)
      (select [[:count :*] :count])))

(defn not-implemented [sql-map _queryp _params]
  (sql-map))

(defn lookup [code]
  (-> {filters-text     contains-text
       filters-keyword  match-exact
       filters-url      not-implemented
       filters-number   not-implemented
       filters-date     not-implemented}
      (get code)))

(defn refine-query [sql-map queryp params]
  (let [path (-> queryp :path (or []))
        field (-> queryp :name (make-field))
        refine (-> queryp :code lookup (or not-implemented))]
    (-> (extract-path sql-map :resource path field)
        (refine queryp params))))

(defn search-query [queryps params]
  (let [[of type] (param/get-values params
                                    param/wellknown-of
                                    param/wellknown-type)
        sql-map (make-sql-map (or of type))]
    (reduce #(refine-query %1 %2 params) sql-map queryps)))
