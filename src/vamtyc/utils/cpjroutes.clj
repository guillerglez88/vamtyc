(ns vamtyc.utils.cpjroutes
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [compojure.core :refer [make-route routes]]
            [ring.util.response :refer [content-type response status]]
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
            [vamtyc.handlers.transaction :as transaction]
            [vamtyc.handlers.inspect :as inspect]
            [vamtyc.queries.core :as queries]))

(def handlers
  {:/Coding/core-handlers?code=list         list/handler
   :/Coding/core-handlers?code=read         read/handler
   :/Coding/core-handlers?code=create       create/handler
   :/Coding/core-handlers?code=delete       delete/handler
   :/Coding/core-handlers?code=upsert       upsert/handler
   :/Coding/core-handlers?code=transaction  transaction/handler
   :/Coding/core-handlers?code=inspect      inspect/handler})

(defn resolve-handler-code [req route]
  (let [inspect-code    :/Coding/core-handlers?code=inspect
        is-inspect      (-> req :params (contains? "_inspect"))
        route-code      (-> route :code keyword)]
    (if is-inspect inspect-code route-code)))

(defn flat-fields [fields-param]
  (cond
    (string? fields-param)  (->> (str/split fields-param #",")
                                 (map #(str/split % #"\."))
                                 (into []))
    (vector? fields-param)  (->> (mapcat flat-fields fields-param)
                                 (into []))
    :else                   []))

(defn select-path-into [path from to]
  (let [[field-str & rest]  path
        field               (keyword field-str)
        from-prop           (field from)]
    (cond
      (empty? rest)       (assoc to field from-prop)
      (vector? from-prop) (->> (or (field to) (map (fn [_] (hash-map)) from-prop))
                               (map #(select-path-into rest %1 %2) from-prop)
                               (into [])
                               (assoc to field))
      :else               (->> (field to)
                               (or {})
                               (select-path-into rest from-prop)
                               (assoc to field)))))

(defn select-fields [resp params]
  (let [fields (-> params (get "_fields") (or []) (flat-fields))]
    (if (empty? fields) resp
        (->> (reduce #(select-path-into %2 (:body resp) %1) {} fields)
             (hash-map :body)
             (merge resp)))))

(defn make-http-response [resp]
  (-> (json/write-str (:body resp))
      (response)
      (status (:status resp))
      (content-type "application/json")))

(defn handle-req [req route]
  (let [handler-code    (resolve-handler-code req route)
        handler         (handler-code handlers)]
    (jdbc/with-transaction [tx ds]
      (-> (requests/build-request req route)
          (queries/process-query-params tx)
          (handler tx)
          (select-fields (:params req))
          (make-http-response)))))

(defn build-cpj-route [route]
  (let [method  (-> route :method str/lower-case keyword)
        path    (-> route :path path/stringify)]
    (make-route method path #(handle-req % route))))

(defn load-routes []
  (->> (store/list ds :Route)
       (map build-cpj-route)
       (apply routes)))
