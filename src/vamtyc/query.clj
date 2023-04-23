(ns vamtyc.query
  (:require
   [clojure.string :as str]
   [honey.sql.helpers :refer [from inner-join limit offset select where]]
   [vamtyc.param :as param]))

(defn make-sql-map [type]
    (-> (select :id :resource :created :modified)
        (from type)))

(defn make-field-alias
  ([base field suffix]
   (-> (name base)
       (str "_" field (when suffix "_") suffix)
       (str/trimr)
       (keyword)))
  ([base field]
   (make-field-alias base field nil)))

(defn extract-prop [sql-map base field alias]
  (inner-join sql-map [[:jsonb_extract_path base field] alias] true))

(defn extract-coll [sql-map base field alias]
  (let [prop-alias (make-field-alias base field)]
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
        curr-alias (if (empty? more) alias (make-field-alias base curr-name suffix))]
    (if (nil? curr)
      sql-map
      (-> (extract-field sql-map base curr curr-alias)
          (extract-path curr-alias more alias)))))

(defn contains-text [sql-map queryp params]
  (let [name (-> queryp :name name)
        db-name (-> name (str/replace #"-" "_") keyword)
        val (get params name)]
    (where sql-map [:like [:cast db-name :text] (str "%" val "%")])))


(defn match-exact [sql-map queryp params]
  (let [name (-> queryp :name name)
        db-name (-> name (str/replace #"-" "_") keyword)
        val (get params name)]
    (where sql-map [:= [:cast db-name :text] (str "\"" val "\"")])))


(defn page-offset [sql-map offset]
  (->> (str offset)
       (Integer/parseInt)
       (offset sql-map)))

(defn page-size [sql-map limit]
  (->> (str limit)
       (Integer/parseInt)
       (limit sql-map)))

(defn order-by [sql-map _params]
  sql-map)

(defn total [sql-map]
  (-> sql-map
      (dissoc :select :offset :limit)
      (select [[:count :*] :count])))

(def filters
  {"/Coding/filters?code=text"             contains-text
   "/Coding/filters?code=keyword"          match-exact})

(defn refine-query [sql-map queryp params]
  (let [path (-> queryp :path (or []))
        db-name (-> queryp :name name (str/replace #"-" "_") keyword)
        refine (-> queryp :code (#(get filters %)) (or (fn [sql-map _ _] sql-map)))]
    (-> (extract-path sql-map :resource path db-name)
        (refine queryp params))))

(defn search-query [queryps params]
  (let [type (param/get-value params "/Coding/wellknown-params?code=type")
        of (param/get-value params "/Coding/wellknown-params?code=of")
        sql-map (make-sql-map (or of type))]
    (reduce #(refine-query %1 %2 params) sql-map queryps)))
