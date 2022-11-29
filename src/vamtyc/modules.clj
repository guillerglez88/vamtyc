(ns vamtyc.modules
  (:require [vamtyc.resources :as resources]
            [vamtyc.routes :as routes]
            [vamtyc.queryparams :as queryparams]
            [vamtyc.history :as history]))

(defn init []
  (resources/init)
  (queryparams/init)
  (routes/init)
  (history/init))
