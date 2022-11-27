(ns vamtyc.routes
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]))

(defn meta-handler [route]
  (fn [req]
    (let [parsed-req    (select-keys req [:uri :params :form-params :query-params])
          body          (merge parsed-req {:route route})]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str body)})))

;; (def core-handlers
;;   {:core/})

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)
        handler (meta-handler route)]
    (make-route method path handler)))

(defn load-cpj-routes []
  (->> (store/list :Route)
       (map build-cpj-route)
       (apply routes)))
