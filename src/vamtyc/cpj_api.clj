(ns vamtyc.cpj-api
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [compojure.core :refer [make-route routes]]
   [next.jdbc :as jdbc]
   [ring.middleware.json :refer [wrap-json-body]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :refer [content-type]]
   [vamtyc.data.datasource :refer [ds]]
   [vamtyc.data.queryp :as dqueryp]
   [vamtyc.data.store :as store]
   [vamtyc.nerves.core :as nerves]
   [vamtyc.param :as param]))

(defn make-http-response [resp]
  (let [body (-> resp :body json/write-str)]
    (-> (merge resp {:body body})
        (content-type "application/json"))))

(defn hydrate [req route def-queryps req-queryps]
  (let [route-param (param/route->param route)
        def-queryp-param (param/queryps->param def-queryps)
        req-queryp-param (param/queryps->param req-queryps)
        req-param (param/req->param req)]
    (->> (param/merge-param [route-param def-queryp-param req-queryp-param req-param])
         (merge req {:vamtyc/param req-param
                     :vamtyc/route route
                     :vamtyc/queryp (concat def-queryps req-queryps)}))))

(defn cpj-handler [route queryps app]
  (fn [req]
    (let [route-param (param/route->param route)
          req-param (param/req->param req)
          handler (-> route :code nerves/pick)
          type (param/get-value route-param "/Coding/wellknown-params?code=type")
          of (param/get-value req-param "/Coding/wellknown-params?code=of")]
      (jdbc/with-transaction [tx ds]
        (->> (keys route-param)
             (map #(-> % (or "") name))
             (vec)
             (dqueryp/load-queryps tx [type of])
             (hydrate req route queryps)
             (#(handler % tx app))
             (make-http-response))))))

(defn cpj-method [route]
  (when (contains? route :method)
    (-> route :method str/lower-case keyword)))

(defn cpj-path [route]
  (->> (:path route)
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (str/join "/")
       (str "/")))

(defn cpj-route [app route queryps]
  (if (contains? route :path)
    (make-route (cpj-method route)
                (cpj-path route)
                (cpj-handler route queryps app))
    (cpj-handler route queryps app)))

(defn calc-index [path]
  (->> (filter #(contains? % :value) path)
       (count)))

(defn load-cpj-routes [app]
  (let [queryps (dqueryp/load-default-queryps ds)]
    (->> (store/search ds :Route)
         (sort-by #(-> % :path calc-index) >)
         (map #(cpj-route app % queryps))
         (apply routes))))

(defonce app (atom nil))

(defn init []
  (->> {:handle @app :reload init}
       (load-cpj-routes)
       (wrap-params)
       (wrap-json-body)
       (reset! app)))
