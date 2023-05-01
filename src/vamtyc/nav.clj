(ns vamtyc.nav
  (:require
   [lambdaisland.uri :refer [assoc-query uri-str]]))

(defn nav-uri [url offset]
  (-> url (assoc-query :_offset offset) uri-str))

(defn result-set [items url total offset limit pg-query]
  (let [first 0
        start (-> offset str Integer/parseInt)
        count (-> limit str Integer/parseInt)
        last (max first (- total count))
        prev (max first (- start count))
        next (min last (+ start count))]
    {:type  :List
     :url   url
     :items (vec items)
     :total total
     :nav   {:first (nav-uri url first)
             :prev  (nav-uri url prev)
             :next  (nav-uri url next)
             :last  (nav-uri url last)}
     :pgquery (:url pg-query)}))
