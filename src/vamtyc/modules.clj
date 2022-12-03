(ns vamtyc.modules
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.resources :as resources]
            [vamtyc.routes :as routes]
            [vamtyc.queryparams :as queryparams]
            [vamtyc.history :as history]
            [vamtyc.transactions :as transactions]))

(defn init []
  (resources/init ds)
  (routes/init ds)
  (queryparams/init ds)
  (history/init ds)
  (transactions/init ds))

(comment
  (init))
