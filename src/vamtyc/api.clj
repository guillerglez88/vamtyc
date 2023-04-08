(ns vamtyc.api
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
   [vamtyc.req.param :as param]
   [vamtyc.utils.queryp :as uqueryp]
   [vamtyc.utils.routes :as uroutes]))

(defn make-http-response [resp]
  (let [body (-> resp :body json/write-str)]
    (-> (merge resp {:body body})
        (content-type "application/json"))))

(defn hydrate [req route def-queryps req-queryps]
  (let [params      (param/req->param req)
        resv-route  (uroutes/resolve-path route params)
        resv-queryp (uqueryp/resolve-queryps params
                                             def-queryps
                                             req-queryps)]
    (merge req {:params         params
                :vamtyc/route   resv-route
                :vamtyc/queryp  resv-queryp})))

(defn cpj-handler [route queryps app]
  (fn [req]
    (let [route-param (param/route->param route)
          handler   (-> route :code nerves/pick)
          type      (-> route :path uroutes/_type)
          of        (-> req :vamtyc/queryp uqueryp/of)]
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
