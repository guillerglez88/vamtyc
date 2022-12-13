(ns vamtyc.utils.request)

(defn relative-url [req]
  (str (:uri req)
       (if-let [query (:query-string req)]
         (str "?" query))))
