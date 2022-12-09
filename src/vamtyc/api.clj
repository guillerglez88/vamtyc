(ns vamtyc.api
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [ring.util.response :refer [content-type response status]]
            [ring.middleware.params :refer [wrap-params]]
            [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.utils.requests :as requests]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]
            [vamtyc.handlers.inspect :as inspect]
            [vamtyc.queries.core :as queries]))

(def handlers
  {:/Coding/nerves?code=search       list/handler
   :/Coding/nerves?code=read         read/handler
   :/Coding/nerves?code=create       create/handler
   :/Coding/nerves?code=delete       delete/handler
   :/Coding/nerves?code=upsert       upsert/handler
   :/Coding/nerves?code=inspect      inspect/handler})

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

(defn handle-req [req route app]
  (let [handler-code    (resolve-handler-code req route)
        handler         (handler-code handlers)]
    (jdbc/with-transaction [tx ds]
      (-> (requests/build-request req route)
          (queries/process-query-params tx)
          (handler tx app)
          (make-http-response)))))

(defn make-cpj-route [app route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)]
    (make-route method path #(handle-req % route app))))

(defn load-routes [app]
  (->> (store/list ds :Route)
       (sort-by #(-> % :path path/calc-match-index) >)
       (map #(make-cpj-route app %))
       (apply routes)))

(defonce app (atom nil))

(defn init []
  (->> {:handle @app
        :reload init}
       (load-routes)
       (wrap-params)
       (reset! app)))

(comment
  (init)
  )
