(ns vamtyc.query
  (:require
   [rest-query.core :as rq]
   [vamtyc.param :as param]))

(def flt-text     "/Coding/filters?code=text")
(def flt-keyword  "/Coding/filters?code=keyword")
(def flt-url      "/Coding/filters?code=url")
(def flt-number   "/Coding/filters?code=number")
(def flt-date     "/Coding/filters?code=date")

(def coding-map
  {flt-text     rq/flt-text
   flt-keyword  rq/flt-keyword
   flt-url      rq/flt-url
   flt-number   rq/flt-number
   flt-date     rq/flt-date})

(defn search-query [table queryps params]
  (let [url-map (hash-map :from table :params params)
        rq-queryps (map #(assoc % :code (get coding-map (:code %))) queryps)]
    (rq/make-query url-map rq-queryps)))

(defn make-pg-query [queryps params]
  (let [[of type] (param/get-values params param/wkp-of param/wkp-type)
        table (-> of (or type) keyword)]
    (-> (search-query table queryps params)
        (assoc :type :PgQuery))))
