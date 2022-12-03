(ns vamtyc.transactions
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.store :as store]
            [vamtyc.data.datasource :refer [ds]]))

(defn make-trn-resource []
  {:type        :Transaction
   :desc        "Represents a Transaction resource"
   :resources   "/Resource"})

(defn place-trn-route []
  {:code        "/Coding/core-handlers?code=transaction"
   :name        "place-transaction"
   :path        [{:name "resourceType" :value "Transaction"}]
   :method      :POST
   :resource    "/Resource/transaction"})

(defn init []
  (jdbc/with-transaction [tx ds]
    (store/create tx :Resource "transaction" (make-trn-resource))
    (store/create tx :Route (place-trn-route))
    {:ok "success!"}))
