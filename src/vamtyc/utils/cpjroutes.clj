(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [ring.util.response :refer [content-type response status]]
            [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.requests :as requests]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]
            [vamtyc.handlers.transaction :as transaction]
            [vamtyc.handlers.inspect :as inspect]))

(def handlers
  {:/Coding/core-handlers?code=list         list/handler
   :/Coding/core-handlers?code=read         read/handler
   :/Coding/core-handlers?code=create       create/handler
   :/Coding/core-handlers?code=delete       delete/handler
   :/Coding/core-handlers?code=upsert       upsert/handler
   :/Coding/core-handlers?code=transaction  transaction/handler
   :/Coding/core-handlers?code=inspect      inspect/handler})

(defn resolve-handler-code [req route]
  (let [inspect-code    :/Coding/core-handlers?code=inspect
        is-inspect      (-> req :params (contains? "_inspect"))
        route-code      (-> route :code keyword)]
    (if is-inspect inspect-code route-code)))

(defn make-http-response [resp]
  (-> (json/write-str (:body resp))
      (response)
      (status (:status resp))
      (content-type "application/json")))

(defn handle-req [req route]
  (let [handler-code    (resolve-handler-code req route)
        handler         (handler-code handlers)]
    (jdbc/with-transaction [tx ds]
      (-> (requests/build-request req route)
          (handler tx)
          (make-http-response)))))

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)]
    (make-route method path #(handle-req % route))))

(defn load-routes []
  (->> (store/list ds :Route)
       (map build-cpj-route)
       (apply routes)))
