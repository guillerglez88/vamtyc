(ns user
  (:require [portal.api :as p]))


(def p (p/open))

(add-tap #'p/submit)

(p/eval-str (str
            '(portal.ui.state/dispatch! portal.ui.state/state
                                        portal.ui.state/set-theme!
                                        :portal.colors/zerodark)))

(comment
  (p/clear)
  (remove-tap #'p/submit)
  (p/close)
  )
