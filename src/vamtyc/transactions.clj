(ns vamtyc.transactions
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.handlers.list :as list]
            [vamtyc.handlers.read :as read]
            [vamtyc.handlers.create :as create]
            [vamtyc.handlers.delete :as delete]
            [vamtyc.handlers.upsert :as upsert]))

(def handlers
  {:/Coding/core-handlers?code=list   list/handler
   :/Coding/core-handlers?code=read   read/handler
   :/Coding/core-handlers?code=create create/handler
   :/Coding/core-handlers?code=delete delete/handler
   :/Coding/core-handlers?code=upsert upsert/handler})

(defn commit [tx req]
  (let [code        (-> req :route :code)
        handler     (code handlers)
        brief-req   (select-keys req [:method :url])
        response    (handler tx req)]
    {:request   brief-req
     :response  response}))

(defn commit [trn]
  (jdbc/with-transaction [tx ds]
    (->> (:items trn)
         (map #(commit tx %))
         (into [])
         #({:resourceType   :List
            :type           :result-set
            :items          %}))))
