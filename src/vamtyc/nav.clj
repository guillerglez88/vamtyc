(ns vamtyc.nav
  (:require
   [lambdaisland.uri :refer [assoc-query uri-str]]))

(defn nav-uri [url offset]
  (-> url (assoc-query :_offset offset) uri-str))

(defn result-set [items total pg-query]
  (let [first 0
        url (:origin pg-query)
        offset (:offset pg-query)
        limit (:limit pg-query)
        last (max first (- total limit))
        prev (max first (- offset limit))
        next (min last (+ offset limit))]
    {:type  :List
     :url   url
     :items (vec items)
     :total total
     :nav   {:first (nav-uri url first)
             :prev  (nav-uri url prev)
             :next  (nav-uri url next)
             :last  (nav-uri url last)}
     :pgquery (:url pg-query)}))
