(ns vamtyc.modules
  (:require [next.jdbc :as jdbc]
            [vamtyc.data.datasource :refer [ds]]
            [vamtyc.resources :as resources]
            [vamtyc.routes :as routes]
            [vamtyc.queryparams :as queryparams]
            [vamtyc.history :as history]
            [vamtyc.transactions :as transactions]
            [vamtyc.data.store :as store]))

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
