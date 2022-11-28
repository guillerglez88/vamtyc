(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json      :as     json]
            [clojure.string         :as     str]
            [compojure.core         :refer  [make-route routes]]
            [vamtyc.data.store      :as     store]
            [vamtyc.utils.path      :as     path]
            [vamtyc.handlers.list   :as     list]
            [vamtyc.handlers.read   :as     read]
            [vamtyc.handlers.create :as     create]
            [vamtyc.handlers.delete :as     delete]
            [vamtyc.handlers.upsert :as     upsert]
            [lambdaisland.uri       :refer  [uri query-string->map]]))

(defn meta-handler [req route]
  (let [parsed-req    (select-keys req [:uri :params :form-params :query-params])
        body          (merge parsed-req {:route route})]
    {:status 200
    :headers {"Content-Type" "application/json"}
    :body (json/write-str body)}))

(def handlers
  {:core/list   list/handler
   :core/read   read/handler
   :core/create create/handler
   :core/delete delete/handler
   :core/upsert upsert/handler})

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)
        code    (-> route :code uri :query query-string->map :code)
        hkey    (-> route :type (keyword code))
        handler (-> hkey handlers (or meta-handler) ((fn [h] (fn [req] (h req route)))))]
    (make-route method path handler)))

(defn load-routes []
  (->> (store/list :Route)
       (map build-cpj-route)
       (apply routes)))
