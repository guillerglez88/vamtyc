(ns vamtyc.modules.core
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.data.store :as store]
            [vamtyc.modules.resources :as resources]
            [vamtyc.modules.routes :as routes]
            [vamtyc.modules.queryparams :as queryparams]
            [vamtyc.modules.history :as history]
            [vamtyc.modules.transactions :as transactions]))

(defn is-already-init? []
  (try
    (store/list ds :Resource)
    true
    (catch Exception _ false)))


(defn init []
  (if (not (is-already-init?))
    (do
      (resources/init ds)
      (routes/init ds)
      (queryparams/init ds)
      (history/init ds)
      (transactions/init ds)
      {:ok "successfully initialized!"})
    {:ok "already initialized!"}))
