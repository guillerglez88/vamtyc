(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [ring.util.response :refer [content-type response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.requests :as requests]
            [vamtyc.transactions :as transactions]))

(defn exec-req-as-trn [req]
  (let [trn {:resourceType  :List
             :type          :transaction
             :items         [req]}]
    (-> trn transactions/commit :items first)))

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)]
    (make-route method path #(-> %
                                 (requests/build-request route)
                                 (exec-req-as-trn)))))

(defn load-routes []
  (->> (exec-req-as-trn {:resourceType  :HttpRequest
                         :url           "/Route"
                         :body          {:resourceType :Route}})
       (map build-cpj-route)
       (apply routes)))
