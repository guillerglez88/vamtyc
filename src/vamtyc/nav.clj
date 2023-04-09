(ns vamtyc.nav
  (:require
   [lambdaisland.uri :refer [assoc-query uri-str]]
   [vamtyc.param :as param]))

(defn nav-uri [url offset]
  (-> url (assoc-query :_offset offset) uri-str))

(defn result-set [req url total items]
  (let [offset  (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=offset"))
        limit   (-> req :vamtyc/param (param/get-value "/Coding/wellknown-params?code=limit"))
        first   0
        last    (max first (- total limit))
        prev    (max first (- offset limit))
        next    (min last (+ offset limit))]
    {:type  :List
     :url   url
     :items items
     :total total
     :nav   {:first (nav-uri url first)
             :prev  (nav-uri url prev)
             :next  (nav-uri url next)
             :last  (nav-uri url last)}}))
