(ns csci-web.core
  (require [clojure.pprint :refer [pprint]]
           [ring.adapter.jetty :as jetty]
           [ring.middleware.reload :refer [wrap-reload]]
           [ring.middleware.defaults :refer :all]
           [ring.util.response :refer :all]
           [compojure.core :as c]
           [compojure.route :as route]
           [hiccup.core :as h]
           ))

;; ========= Views using hiccup ==============

(defn index-view [req]
  (let [req-str (with-out-str (clojure.pprint/pprint req))]
    [:div
     [:h1 "index v4:" (:uri req)]
     [:p "Link: " [:a {:href "/nice/user/kenpu/school/uoit"} "nice"]]
     ]))

(defn nice-view [id school language req]
  [:div
   [:h1 "User: " id]
   [:h2 "School: " school]
   [:h3 "Language: " language]
   [:h3 "Session: " (get-in req [:session :counter] "n/a")]
   [:h4 
    [:a {:href "/inc"} "Increment"]
    [:span ","]
    [:a {:href "/dec"} "Decrement"]
    [:span ","]
    [:a {:href "/destroy"} "Destroy"]]
   [:pre (with-out-str (clojure.pprint/pprint req))]])

(defn change-counter [req delta]
  (let [counter (get-in req [:session :counter] 0)
        url (get-in req [:headers "referer"] "/")]
    (-> (redirect url)
        (assoc :session {:counter (+ counter delta)}))))

(defn html-view [view-fn & args]
  (let [data [:html
              [:head
               [:link {:rel "stylesheet" :href "/static/css/style.css"}]]
              [:body (apply view-fn args)]]]
    (-> data
        (h/html)
        (response)
        (content-type "text/html"))))

(def handler
  (c/routes
    (c/GET "/" req (html-view index-view req))

    (c/GET "/nice/user/:id/school/:school" 
           [id school language :as req]
           (html-view nice-view id school language req))

    (c/GET "/inc" req (change-counter req 1))

    (c/GET "/dec" req (change-counter req -1))

    (c/GET "/destroy" req (-> (redirect (get-in req [:headers "referer"] "/"))
                              (assoc :session nil)))

    (route/resources "/static" {:root "static-files"})

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
             (wrap-defaults site-defaults)
             (wrap-reload)))

(defn webserver []
  (jetty/run-jetty app {:port 7654}))

