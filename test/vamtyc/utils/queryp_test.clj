(ns vamtyc.utils.queryp-test
  (:require [vamtyc.utils.queryp :as sut]
            [clojure.test :as t]))

(defn make-queryp
  ([value]
   {:type      :QueryParam
    :code      "/Coding/wellknown-params?code=limit"
    :desc      "Limit items count in the result"
    :name      :_limit
    :value     (or value 128)
    :of        :List})
  ([] (make-queryp nil)))

(t/deftest resolve
  (t/testing "Can resolve QueryParam value from req-params"
    (t/is (= (make-queryp 5)
             (sut/resolve-queryp {"_limit" 5 "_of" "Resource"}
                                 (make-queryp))))))
