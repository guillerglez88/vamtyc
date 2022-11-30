(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [ring.util.response :refer [content-type response]]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.requests :as requests]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]
            [vamtyc.handlers.transaction :as transaction]))

(def handlers
  {:/Coding/core-handlers?code=list         list/handler
   :/Coding/core-handlers?code=read         read/handler
   :/Coding/core-handlers?code=create       create/handler
   :/Coding/core-handlers?code=delete       delete/handler
   :/Coding/core-handlers?code=upsert       upsert/handler
   :/Coding/core-handlers?code=transaction  transaction/handler})

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)
        handler (-> route :code keyword (-> handlers))]
    (make-route method path #(->> (requests/build-request % route)
                                  (handler ds)))))

(defn load-routes []
  (->> (store/list ds :Route)
       (map build-cpj-route)
       (apply routes)))
