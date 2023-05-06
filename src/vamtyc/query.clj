(ns vamtyc.query
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [honey.sql :as hsql]
   [honey.sql.helpers :refer [from inner-join limit offset select where]]
   [honey.sql.pg-ops :refer [at>]]
   [lambdaisland.uri :as uri :refer [map->query-string query-string->map uri]]
   [vamtyc.param :as param])
  (:import
   [java.security MessageDigest]))

(def flt-text     "/Coding/filters?code=text")
(def flt-keyword  "/Coding/filters?code=keyword")
(def flt-url      "/Coding/filters?code=url")
(def flt-number   "/Coding/filters?code=number")
(def flt-date     "/Coding/filters?code=date")

(defn make-field [& parts]
  (->> parts
       (filter (complement nil?))
       (map name)
       (filter (complement str/blank?))
       (map #(str/replace % #"-" "_"))
       (str/join "_")
       (str/trimr)
       (keyword)))

(defn all-by-type [type]
    (-> (select :res.*)
        (from [type :res])))

(defn extract-prop [sql-map base path-elem alias]
  (let [field (:name path-elem)]
    (inner-join sql-map [[:jsonb_extract_path base field] alias] true)))

(defn extract-coll [sql-map base path-elem alias]
  (let [field (:name path-elem)
        prop-alias (make-field base field)]
    (-> (extract-prop sql-map base path-elem prop-alias)
        (inner-join [[:jsonb_array_elements prop-alias] alias]
                    (if-let [filter (:filter path-elem)]
                      [[at> alias [:lift filter]]]
                      true)))))

(defn extract-field [sql-map base path-elem alias]
  (cond
    (:meta path-elem)       sql-map ;; TODO: implement meta fields access
    (:collection path-elem) (extract-coll sql-map base path-elem alias)
    :else                   (extract-prop sql-map base path-elem alias)))

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
        val (-> params first (get key) name)
        field (make-field key)]
    (where sql-map [:like [:cast field :text] (str "%" val "%")])))

(defn match-exact [sql-map queryp params]
  (let [name (-> queryp :name name)
        field (make-field name)
        val (-> params first (get name))]
    (where sql-map [:= [:cast field :text] (str "\"" val "\"")])))

(defn paginate [sql-map start count]
  (-> sql-map
      (offset start)
      (limit count)))

(defn total [sql-map]
  (-> sql-map
      (dissoc :select :offset :limit)
      (select [[:count :*] :count])))

(defn not-implemented [sql-map _queryp _params]
  sql-map)

(defn lookup [code]
  (-> {flt-text     contains-text
       flt-keyword  match-exact
       flt-url      not-implemented
       flt-number   not-implemented
       flt-date     not-implemented}
      (get code)))

(defn refine-query [params sql-map queryp]
  (let [path (-> queryp :path (or []))
        field (-> queryp :name (make-field))
        refine (-> queryp :code lookup (or not-implemented))]
    (-> (extract-path sql-map :resource path field)
        (refine queryp params))))

(defn search-query [table queryps params]
  (let [sql-map (all-by-type table)]
    (-> (partial refine-query params)
        (reduce sql-map queryps))))

(defn calc-hash [payload]
  (let [sha256 (MessageDigest/getInstance "SHA-256")]
    (->> (.getBytes payload "UTF-8")
         (.digest sha256)
         (map (partial format "%02x"))
         (apply str))))

(defn clean-url
  ([url]
   (clean-url url #{}))
  ([url keep]
   (let [uri (uri url)
         qs-map (-> uri :query query-string->map)
         keep-map (select-keys qs-map keep)]
     (->> (keys qs-map)
          (map #(vector % ""))
          (into (sorted-map))
          (#(merge % keep-map))
          (map->query-string)
          (str (:path uri) "?")))))

(defn make-url [params]
  (let [type (param/get-value params param/wkp-type)
        param (first params)
        type-prop-name (param/get-name params param/wkp-type)]
    (->> (set/difference (->> param keys (apply hash-set))
                         (hash-set type-prop-name))
         (select-keys param)
         (into (sorted-map))
         (map->query-string)
         (str "/" type "?"))))

(defn make-pg-query [queryps params url]
  (let [[of type] (param/get-values params param/wkp-of param/wkp-type)
        [offset limit] (param/get-values params param/wkp-offset param/wkp-limit)
        table (-> of (or type) keyword)
        start (-> offset str Integer/parseInt)
        count (-> limit str Integer/parseInt)
        query (search-query table queryps params)
        page (paginate query start count)
        total (total query)
        param-url (make-url params)
        of-name (-> params (param/get-name param/wkp-of) keyword)]
    {:type :PgQuery
     :hash (-> param-url (clean-url #{of-name}) calc-hash)
     :origin url
     :expanded param-url
     :from table
     :offset start
     :limit count
     :query (hsql/format query)
     :page (hsql/format page)
     :total (hsql/format total)}))
