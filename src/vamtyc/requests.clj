(ns vamtyc.requests
  (:require [clojure.data.json :as json]
            [ring.util.request :refer [body-string]]
            [vamtyc.utils.path :as path]
            [clojure.string :as str]))

(defn build-request [req route]
  (let [method      (:request-method req)
        query-str   (:query-string req)
        req-url     (-> :uri req (str (when query-str (str "?" query-str))))
        res-type    (-> route :path path/get-res-type keyword)
        id          (-> req :params :id)
        body        (-> req
                        body-string
                        (#(if (= % "") "{}" %))
                        (json/read-str :key-fn keyword)
                        (assoc :resourceType    res-type
                               :id              id))
        route-url   (:url route )
        params      (:params req)]
    {:resourceType  :HttpRequest
     :method        method
     :url           req-url
     :body          body
     :route         route-url
     :params        params}))
