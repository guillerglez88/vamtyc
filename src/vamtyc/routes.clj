(ns vamtyc.routes
  (:require [clojure.data.json      :as     json]
            [clojure.string         :as     str]
            [compojure.core         :refer  [make-route routes]]
            [lambdaisland.uri       :refer  [uri query-string->map]]
            [vamtyc.data.store      :as     store]
            [vamtyc.utils.path      :as     path]
            [vamtyc.handlers.list   :as     list]
            [vamtyc.handlers.read   :as     read]))

(defn meta-handler [route]
  (fn [req]
    (let [parsed-req    (select-keys req [:uri :params :form-params :query-params])
          body          (merge parsed-req {:route route})]
      {:status 200
       :headers {"Content-Type" "application/json"}
       :body (json/write-str body)})))

(def handlers
  {:core/list   list/handler
   :core/read   read/handler})

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)
        code    (-> route :code uri :query query-string->map :code)
        hkey    (-> route :type (keyword code))
        handler (-> hkey handlers (or meta-handler) (#(% route)))]
    (make-route method path handler)))

(defn load-cpj-routes []
  (->> (store/list :Route)
       (map build-cpj-route)
       (apply routes)))

(comment
  (load-cpj-routes)
  (uri "/Coding/core-handlers?code=list")
  )
