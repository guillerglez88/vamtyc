(ns vamtyc.modules
  (:require [vamtyc.resources :as resources]
            [vamtyc.routes :as routes]
            [vamtyc.queryparams :as queryparams]
            [vamtyc.history :as history]
            [vamtyc.transactions :as transactions]))

(defn init []
  (resources/init)
  (routes/init)
  (transactions/init)
  (queryparams/init)
  (history/init))
