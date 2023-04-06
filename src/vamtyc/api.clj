(ns vamtyc.api
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [compojure.core :refer [make-route routes]]
   [ring.util.response :refer [content-type]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.json :refer [wrap-json-body]]
   [next.jdbc :as jdbc]
   [vamtyc.data.datasource :refer [ds]]
   [vamtyc.data.store :as store]
   [vamtyc.utils.routes :as uroutes]
   [vamtyc.nerves.core :as nerves]
   [vamtyc.utils.params :as params]
   [vamtyc.data.queryp :as dqueryp]
   [vamtyc.utils.queryp :as uqueryp]))

(defn make-http-response [resp]
  (let [body (-> resp :body json/write-str)]
    (-> (merge resp {:body body})
        (content-type "application/json"))))

(defn hydrate [req route def-queryps req-queryps]
  (let [params      (params/req-to-params req)
        resv-route  (uroutes/resolve-path route params)
        resv-queryp (uqueryp/resolve-queryps params
                                             def-queryps
                                             req-queryps)]
    (merge req {:params         params
                :vamtyc/route   resv-route
                :vamtyc/queryp  resv-queryp})))

(defn compojure-handler [route queryps app]
  (fn [req]
    (let [handler   (-> route :code nerves/pick)
          type      (-> route :path uroutes/_type)
          of        (-> req :vamtyc/queryp uqueryp/of)]
      (jdbc/with-transaction [tx ds]
        (->> (params/extract-param-names req)
             (dqueryp/load-queryps tx [type of])
             (hydrate req route queryps)
             (#(handler % tx app))
             (make-http-response))))))

(defn compojure-method [route]
  (when (contains? route :method)
    (-> route :method str/lower-case keyword)))

(defn compojure-path [route]
  (-> route :path uroutes/str-path))

(defn compojure-route [app route queryp]
  (if (contains? route :path)
    (make-route (compojure-method route)
                (compojure-path route)
                (compojure-handler route queryp app))
    (compojure-handler route queryp app)))

(defn load-compojure-routes [app]
  (let [queryps (dqueryp/load-default-queryps ds)]
    (->> (store/search ds :Route)
         (sort-by #(-> % :path uroutes/calc-match-index) >)
         (map #(compojure-route app % queryps))
         (apply routes))))

(defonce app (atom nil))

(defn init []
  (->> {:handle @app :reload init}
       (load-compojure-routes)
       (wrap-params)
       (wrap-json-body)
       (reset! app)))
