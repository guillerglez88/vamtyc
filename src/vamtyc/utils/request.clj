(ns vamtyc.utils.request)

(defn relative-url [req]
  (str (:uri req)
       (when-let [query (:query-string req)]
         (str "?" query))))
