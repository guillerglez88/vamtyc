(ns vamtyc.api
  (:require [vamtyc.data.store :as store]
            [clojure.data.json :as json]))

(defn load-endpoints []
  (for [endpoint (store/list :endpoint)]
    (core-handler endpoint)))

;; (defroutes app-routes
;;   (GET "/"                      []  home-page)
;;   (GET "/request"               []  request-example)
;;   (GET "/hello"                 []  hello-name)
;;   (route/not-found "Error, page not found!"))

(def core-functions
  {:list store/list
   :read store/read
   :create store/create
   :upsert (fn [p] p)
   :delete store/delete})

(defn core-handler [endpoint]
  (fn [req]
    (let [parsed-req (select-keys req [:uri :params :form-params :query-params])]
        {:status 200
        :headers {"Content-Type" "application/json"}
        :body (json/write-str parsed-req)})))
