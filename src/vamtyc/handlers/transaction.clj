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

(def trn-res
  {:type        :Transaction
   :desc        "Represents a Transaction resource"
   :resources   "/Resource"})

(def place-trn-route
  {:code        "/Coding/core-handlers?code=transaction"
   :name        "place-transaction"
   :path        [{:name "resourceType" :value "Transaction"}]
   :method      :POST
   :resource    "/Resource/transaction"})

(defn init []
  (jdbc/with-transaction [tx ds]
    (store/create tx :Resource "transaction" trn-res)
    (store/create tx :Route place-trn-route)
    {:ok "success!"}))

(def handlers
  {:/Coding/core-handlers?code=list   list/handler
   :/Coding/core-handlers?code=read   read/handler
   :/Coding/core-handlers?code=create create/handler
   :/Coding/core-handlers?code=delete delete/handler
   :/Coding/core-handlers?code=upsert upsert/handler})

(defn inspect-req [item]
  (let [method  (-> item :method str/lower-case keyword)
        url     (str (:baseurl item) (:url item))
        body    (-> item :body json/write-str)]
    (client/request {:method        method
                     :url           url
                     :content-type  :json
                     :query-params  { "_inspect" "true" }
                     :body          body})))

(defn commit [tx item]
  (let [req             (-> item inspect-req :body (json/read-str :key-fn keyword))
        route-code      (-> req :route :code keyword)
        route-handler   (route-code handlers)
        brief-req       (select-keys req [:method :url])
        response        (route-handler tx req)
        response-map    (merge response {:body (json/read-str (:body response) :key-fn keyword)})]
    {:request   brief-req
     :response  response-map}))

(defn handler [_ req]
  (let [route       (:route req)
        base-url    (:baseurl req)
        items       (-> req :body :items)]
    (jdbc/with-transaction [tx ds]
      (->> items
           (map #(->> (assoc % :baseurl base-url)
                      (commit tx)))
           (into [])
           (assoc {:resourceType  :List
                   :type          :transaction-result } :items )
           (json/write-str)
           (response)
           (#(content-type % "application/json"))))))

(comment
  (init)
  )
