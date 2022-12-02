(ns vamtyc.handlers.transaction
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]
            [vamtyc.data.store :as store]))

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

(defn commit [tx req]
  (let [code        (-> req :route :code keyword)
        handler     (code handlers)
        brief-req   (select-keys req [:method :url])
        response    (handler tx req)]
    {:request   brief-req
     :response  response}))

(defn handler [_ req]
  (let [route (:route req)
        items (-> req :body :items)]
    (jdbc/with-transaction [tx ds]
      (->> items
           (map #(commit tx (assoc % :route route)))
           (into [])
           (#({:resourceType   :List
               :type           :transaction-result
               :items          %}))))))

(comment
  (init)
  )
