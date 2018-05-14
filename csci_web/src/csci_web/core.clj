(ns csci-web.core
  (require [clojure.pprint :refer [pprint]]
           [ring.adapter.jetty :as jetty]
           [ring.middleware.reload :refer [wrap-reload]]
           [ring.util.response :refer :all]
           [compojure.core :as c]
           [compojure.route :as route]))

(def handler
  (c/routes
    (c/GET "/" req (-> "Hello <b style=color:red>world blah blah</b>."
                       (response)
                       (content-type "text/html")))

    (c/GET "/nice" req (response "<h1>I am nice</h1>"))

    (route/not-found (response "Not found"))))

(defn wrap-logging [handler]
  (fn [req]
    (println "====== req ========")
    (pprint req)
    (let [resp (handler req)]
      (println "====== resp ========")
      (pprint resp)
      resp)))

(def app (-> #'handler
             (wrap-reload)
             (wrap-logging)))

(defn webserver []
  (jetty/run-jetty app {:port 7654}))

