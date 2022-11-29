(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json      :as     json]
            [clojure.string         :as     str]
            [compojure.core         :refer  [make-route routes]]
            [vamtyc.data.store      :as     store]
            [vamtyc.utils.path      :as     path]
            [vamtyc.handlers.list   :as     list]
            [vamtyc.handlers.read   :as     read]
            [vamtyc.handlers.create :as     create]
            [vamtyc.handlers.delete :as     delete]
            [vamtyc.handlers.upsert :as     upsert]
            [lambdaisland.uri       :refer  [uri query-string->map]]))

(defn meta-handler [req route]
  (let [parsed-req    (select-keys req [:uri :params :form-params :query-params])
        body          (merge parsed-req {:route route})]
    {:status 200
    :headers {"Content-Type" "application/json"}
    :body (json/write-str body)}))

(def handlers
  {:/Coding/core-handlers?code=list   list/handler
   :/Coding/core-handlers?code=read   read/handler
   :/Coding/core-handlers?code=create create/handler
   :/Coding/core-handlers?code=delete delete/handler
   :/Coding/core-handlers?code=upsert upsert/handler})

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)
        code    (-> route :code keyword)
        handler (or (code handlers) meta-handler)]
    (make-route method path #(handler % route))))

(defn load-routes []
  (->> (store/list :Route)
       (map build-cpj-route)
       (apply routes)))
