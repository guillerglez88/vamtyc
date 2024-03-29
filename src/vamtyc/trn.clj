(ns vamtyc.trn
  (:require
   [clj-http.client :as client]
   [clojure.string :as str]
   [clojure.data.json :as json]))

(defn make-trn-result [items]
  {:type    :Transaction
   :status "/Coding/transaction-statuses?code=completed"
   :items   items})

(defn make-inspect-http-req [req]
  (let [method  (-> req :method str/lower-case keyword)
        url     (str (:baseurl req) (:url req))
        body    (-> req :body json/write-str)]
    {:method        method
     :url           url
     :content-type  :json
     :queryps  { "_inspect" "true"}
     :body          body}))

(defn make-trn-item-result [req resp]
  (let [brief-req   (select-keys req [:method :url])]
    {:request   brief-req
     :response  resp}))

(def handlers {})

(defn inspect-req [req]
  (-> req
      (make-inspect-http-req)
      (client/request)
      (:body)
      (json/read-str :key-fn keyword)))

(defn handle-req [req tx]
  (let [code        (-> req :route :code keyword)
        req-handler (-> handlers code)]
    (req-handler req tx)))

(defn commit [item req tx]
  (-> (assoc item :baseurl (:baseurl req))
      (inspect-req)
      (handle-req tx)
      (->> (make-trn-item-result item))))
