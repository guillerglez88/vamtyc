(ns vamtyc.nerves.core
  (:require [vamtyc.nerves.create :as create]
            [vamtyc.nerves.read :as read]
            [vamtyc.nerves.upsert :as upsert]
            [vamtyc.nerves.delete :as delete]
            [vamtyc.nerves.search :as search]
            [vamtyc.nerves.notfound :as notfound]))

(def nerves
  {:/Coding/nerves?code=create       create/handler
   :/Coding/nerves?code=read         read/handler
   :/Coding/nerves?code=upsert       upsert/handler
   :/Coding/nerves?code=delete       delete/handler
   :/Coding/nerves?code=search       search/handler
   :/Coding/nerves?code=not-found    notfound/handler})
