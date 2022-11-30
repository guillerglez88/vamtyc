(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [ring.util.response :refer [content-type response]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]
            [lambdaisland.uri :refer [uri query-string->map]]
            [vamtyc.requests :as requests]))

(defn meta-handler [req route]
  (-> req
      (requests/build-request route)
      (json/write-str)
      (response)
      (content-type "application/json")))

(def handlers
  {:/Coding/core-handlers?code=list   list/handler
   :/Coding/core-handlers?code=read   read/handler
   :/Coding/core-handlers?code=create create/handler
   :/Coding/core-handlers?code=delete delete/handler
   :/Coding/core-handlers?code=upsert upsert/handler})

(defn handler [req route]
  (let [code    (-> route :code keyword)
        handle  (or (code handlers) meta-handler)]
    (-> req
        (requests/build-request route)
        handle)))

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)]
    (make-route method path #(handler % route))))

(defn load-routes []
  (->> (store/list :Route)
       (map build-cpj-route)
       (apply routes)))
