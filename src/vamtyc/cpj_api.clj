(ns vamtyc.cpj-api
  (:require
   [clojure.data.json :as json]
   [clojure.string :as str]
   [compojure.core :refer [make-route routes]]
   [ring.middleware.json :refer [wrap-json-body]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.util.response :refer [content-type]]
   [vamtyc.data.datasource :refer [ds]]
   [vamtyc.data.store :as store]
   [vamtyc.handler :as handler]))

(def handlers
  {"/Coding/handlers?code=create"       handler/create
   "/Coding/handlers?code=read"         handler/rread
   "/Coding/handlers?code=upsert"       handler/upsert
   "/Coding/handlers?code=delete"       handler/delete
   "/Coding/handlers?code=search"       handler/search
   "/Coding/handlers?code=not-found"    handler/notfound})

(defn make-http-response [resp]
  (let [body (-> resp :body json/write-str)]
    (-> (merge resp {:body body})
        (content-type "application/json"))))

(defn cpj-handler [route]
  (fn [req]
    (let [code (:code route)]
      (-> (get handlers code)
          (apply req)
          (make-http-response)))))

(defn cpj-method [route]
  (when (contains? route :method)
    (-> route :method str/lower-case keyword)))

(defn cpj-path [route]
  (->> (or (:path route) [])
       (map (fn [cmp] (or (:value cmp) (str ":" (:name cmp)))))
       (str/join "/")
       (str "/")))

(defn cpj-route [route]
  (let [method (cpj-method route)
        path   (cpj-path route)
        handler (cpj-handler route)]
    (if method
      (make-route method path handler)
      handler)))

(defn path-value-count [route]
  (->> (:path route)
       (filter :value)
       (count)))

(defn load-cpj-routes []
  (->> (store/search ds :Route)
       (sort-by path-value-count >)
       (map cpj-route)
       (apply routes)))

(defonce app (atom nil))

(defn init []
  (->> (load-cpj-routes)
       (wrap-params)
       (wrap-json-body)
       (reset! app)))
