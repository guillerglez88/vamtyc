(ns vamtyc.api
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [compojure.route :as route]
            [ring.util.response :refer [content-type response status]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-body]]
            [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.utils.routes :as routes]
            [vamtyc.nerves.core :as nerves]
            [vamtyc.utils.params :as params]
            [vamtyc.config.env :refer [sec-env]]
            [vamtyc.data.queryparams :as queryp]))

(defn make-http-response [resp]
  (let [body (-> resp :body json/write-str)]
    (-> (merge resp {:body body})
        (content-type "application/json"))))

(defn hydrate [req route env queryp]
  (->> (params/make-params req env route queryp)
       (hash-map :vamtyc/route route
                 :vamtyc/env sec-env
                 :vamtyc/queryp queryp
                 :params)
       (merge req)))

(defn handler [route app]
  (let [res-type  (-> route :path routes/get-res-type)
        code      (-> route :code keyword)
        handler   (nerves/pick code)]
    (fn [req]
      (let [of-type (-> req :params (get "_of") keyword)]
        (jdbc/with-transaction [tx ds]
          (->> (params/extract-param-names req)
               (queryp/load-queryparams tx [res-type of-type])
               (hydrate req route sec-env)
               (#(handler % tx app ))
               (make-http-response)))))))

(defn compojure-method [route]
  (when (contains? route :method)
    (-> route :method str/lower-case keyword)))

(defn compojure-path [route]
  (-> route :path routes/str-path))

(defn compojure-route [app route]
  (if (contains? route :path)
    (make-route (compojure-method route)
                (compojure-path route)
                (handler route app))
    (handler route app)))

(defn load-compojure-routes [app]
  (->> (store/list ds :Route)
       (sort-by #(-> % :path routes/calc-match-index) >)
       (map #(compojure-route app %))
       (apply routes)))

(defonce app (atom nil))

(defn init []
  (->> {:handle @app
        :reload init}
       (load-compojure-routes)
       (wrap-params)
       (wrap-json-body)
       (reset! app)))
