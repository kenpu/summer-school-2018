(ns csci-web3.core
  (require [ring.adapter.jetty :as jetty]
           [ring.middleware.reload :refer [wrap-reload]]
           [ring.util.response :refer [response]]
           [compojure.core :as c]
           [compojure.route :as route]
           [hiccup.core :as h]))

(defn <index> [r]
  (h/html
    [:html
     [:head
      [:title "CSCI Web3"]
      [:link {:rel "stylesheet"
              :href "/static/style.css"}]]
     [:body
      [:h1 "Hello Web 3"]
      [:script {:src "/static/js/main.js"}]]]))


(def app (c/routes
           (c/GET "/" r (<index> r))
           (route/resources "/static" {:root "public"})
           (route/not-found (response "Not found"))))

(defn -main []
  (jetty/run-jetty (-> #'app
                      (wrap-reload)) {:port 7654}))
