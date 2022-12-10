(ns vamtyc.api
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [compojure.route :as route]
            [ring.util.response :refer [content-type response status]]
            [ring.middleware.params :refer [wrap-params]]
            [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.path :as path]
            [vamtyc.utils.requests :as requests]
            [vamtyc.nerves.core :refer [nerves]]))

(defn make-http-response [resp]
  (let [body (-> resp :body json/write-str)]
    (-> (merge resp {:body body})
        (content-type "application/json"))))

(defn handle-req [req route app]
  (let [handler-code    (-> route :code keyword)
        handler         (handler-code nerves)]
    (jdbc/with-transaction [tx ds]
      (-> (requests/build-request req route)
          (handler tx app)
          (make-http-response)))))

(defn make-cpj-route [app route]
  (let [method      (-> route :method)
        method-kw   (when method (-> method str/lower-case keyword))
        path        (:path route)
        path-str    (path/stringify path)
        handler     #(handle-req % route app)]
    (if path
      (make-route method-kw path-str handler)
      handler)))

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
