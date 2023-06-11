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
  {flt-text         rq/flt-text
   flt-keyword      rq/flt-keyword
   flt-url          rq/flt-url
   flt-number       rq/flt-number
   flt-date         rq/flt-date
   param/wkp-offset rq/pag-offset
   param/wkp-limit  rq/pag-limit
   param/wkp-sort   rq/pag-sort})

(defn make-pg-query [table params queryps]
  (let [url-map (hash-map :from table :params params)
        rq-queryps (map #(assoc % :code (get coding-map (:code %))) queryps)]
    (rq/make-query url-map rq-queryps)))
