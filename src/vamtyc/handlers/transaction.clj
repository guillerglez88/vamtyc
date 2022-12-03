(ns vamtyc.handlers.transaction
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]
            [vamtyc.data.store :as store]
            [clj-http.client :as client]
            [lambdaisland.uri :refer [uri]]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [ring.util.response :refer [response content-type]]))

(defn make-trn-resource []
  {:type        :Transaction
   :desc        "Represents a Transaction resource"
   :resources   "/Resource"})

(defn make-trn-result [items]
  {:resourceType    :List
   :type            :transaction-result
   :items           items})

(defn place-trn-route []
  {:code        "/Coding/core-handlers?code=transaction"
   :name        "place-transaction"
   :path        [{:name "resourceType" :value "Transaction"}]
   :method      :POST
   :resource    "/Resource/transaction"})

(defn make-inspect-http-req [req]
  (let [method  (-> req :method str/lower-case keyword)
        url     (str (:baseurl req) (:url req))
        body    (-> req :body json/write-str)]
    {:method        method
     :url           url
     :content-type  :json
     :query-params  { "_inspect" "true" }
     :body          body}))

(defn make-trn-item-result [req resp]
  (let [brief-req   (select-keys req [:method :url])]
    {:request   brief-req
     :response  resp}))

(defn init []
  (jdbc/with-transaction [tx ds]
    (store/create tx :Resource "transaction" (make-trn-resource))
    (store/create tx :Route (place-trn-route))
    {:ok "success!"}))

(def handlers
  {:/Coding/core-handlers?code=list   list/handler
   :/Coding/core-handlers?code=read   read/handler
   :/Coding/core-handlers?code=create create/handler
   :/Coding/core-handlers?code=delete delete/handler
   :/Coding/core-handlers?code=upsert upsert/handler})

(defn inspect-req [req]
  (-> req
      (make-inspect-http-req)
      (client/request)
      (:body)
      (json/read-str :key-fn keyword)))

(defn handle-req [req tx]
  (let [code        (-> req :route :code keyword)
        req-handler (-> handlers code)]
    (req-handler req tx)))

(defn commit [item req tx]
  (-> (assoc item :baseurl (:baseurl req))
      (inspect-req)
      (handle-req tx)
      (->> (make-trn-item-result item))))

(defn handler [req tx]
  (->> (-> req :body :items)
       (map #(commit % req tx))
       (into [])
       (make-trn-result)
       (response)))
