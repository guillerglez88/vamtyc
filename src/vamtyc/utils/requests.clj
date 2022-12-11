(ns vamtyc.utils.requests
  (:require [clojure.data.json :as json]
            [ring.util.request :refer [body-string]]
            [vamtyc.utils.routes :as routes]
            [clojure.string :as str]))

(defn extract-base-url [req]
  (let [port    (:server-port req)
        scheme  (-> req :scheme name)
        server  (:server-name req)]
    (->> (if port (str ":" port) "")
         (str scheme "://" server))))

(defn extract-body [req]
  (-> (body-string req)
      (#(if (= % "") "{}" %))
      (json/read-str :key-fn keyword)))

(defn extract-param-names [req]
  (->> (-> req :params keys (or []))
       (map name)
       (into [])))

(defn build-request [req route]
  (let [method      (:request-method req)
        query-str   (:query-string req)
        req-url     (-> :uri req (str (when query-str (str "?" query-str))))
        req-port    (:server-port req)
        base-url    (extract-base-url req)
        res-type    (-> route :path routes/get-res-type keyword)
        id          (-> req :params :id)
        body        (-> req extract-body (assoc :resourceType    res-type
                                                :id              id))
        params      (:params req)]
    {:resourceType  :HttpRequest
     :method        method
     :url           req-url
     :baseurl       base-url
     :body          body
     :route         route
     :params        params}))
