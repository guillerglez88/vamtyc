(ns vamtyc.query
  (:require
   [rest-query.core :as rq]))

(def flt-text     "/Coding/filters?code=text")
(def flt-keyword  "/Coding/filters?code=keyword")
(def flt-url      "/Coding/filters?code=url")
(def flt-number   "/Coding/filters?code=number")
(def flt-date     "/Coding/filters?code=date")
(def wkp-limit    "/Coding/wellknown-params?code=limit")
(def wkp-offset   "/Coding/wellknown-params?code=offset")
(def wkp-sort     "/Coding/wellknown-params?code=sort")

(def coding-map
  {flt-text     rq/flt-text
   flt-keyword  rq/flt-keyword
   flt-url      rq/flt-url
   flt-number   rq/flt-number
   flt-date     rq/flt-date
   wkp-offset   rq/pag-offset
   wkp-limit    rq/pag-limit
   wkp-sort     rq/pag-sort})

(defn make-pg-query [table params queryps]
  (let [
        cqueryps (map #(assoc % :code (get coding-map (:code %))) queryps)
        xparams (rq/expand-params params)
        xqueryps (rq/expand-queryps cqueryps)]
    (rq/make-query table xparams xqueryps)))
