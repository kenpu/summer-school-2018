(ns csci-web2.core
  (require [clojure.pprint :refer [pprint]]
           [ring.adapter.jetty :as jetty]
           [ring.middleware.reload :refer [wrap-reload]]
           [ring.middleware.defaults :refer :all]
           [ring.util.response :as response]
           [ring.util.anti-forgery :refer [anti-forgery-field]]
           [compojure.core :refer [GET POST] :as c]
           [compojure.route :as route]
           [hiccup.core :refer [html]]

           ;; database
           [clojure.java.jdbc :as sql]
           ))

(def db-spec {:subprotocol "postgresql" 
              :subname "//localhost:9999/mydb" 
              :user "mary" 
              :password "abc"})

(defn db-get-names []
  (try (sql/with-db-connection 
         [conn db-spec]
         {:result (sql/query conn ["select name from T order by name"])})
       (catch Exception e {:error (str e)})))

(defn db-save-name [name]
  (sql/with-db-transaction
    [tx db-spec]
    (sql/execute! tx ["insert into T values(?)" name])))

(defn db-delete-name [name]
  (sql/with-db-transaction
    [tx db-spec]
    (sql/execute! tx ["delete from T where name = ?" name])))

(defn html-response
  [view-fn & args]
  (-> (apply view-fn args)
      (response/response)
      (response/content-type "text/html")))

(defn format-names [result]
  (if-let [err (:error result)]
    [:div "ERROR:" err]
    [:ul
     (for [person (:result result)]
       [:li 
        (:name person)
        [:form {:action "/delete" :method "get"}
         [:input {:type "hidden" :name "name" :value (:name person)}]
         [:input {:type "submit" :value "X"}]]
        ])]))

(defn page-html
  [body]
  (html [:html
         [:head 
          [:title "Web2"]
          [:link {:rel "stylesheet"
                  :href "/static/css/style.css"}]]
         [:body body]]))

(defn <index>
  [r]
  (let [names (db-get-names)]
    (page-html 
      [:div
       [:h1 "Hello world"]
       (format-names names)
       [:form {:action "/save"
               :method "post"}
        (anti-forgery-field)
        [:label "Name "]
        [:input {:type "text" :name "name"}]
        [:input {:type "submit" :value "Save"}]]
       ])))

(defn <not-found>
  []
  (page-html [:div "Not found"]))

(defn api-save
  [name req]
  (if-let [url (get-in req [:headers "referer"])]
    (do (db-save-name name)
        (response/redirect url))
    (response/redirect "/")))

(defn api-delete
  [name req]
  (if-let [url (get-in req [:headers "referer"])]
    (do (db-delete-name name)
        (response/redirect url))
    (response/redirect "/")))

(def routes
  (c/routes 
    (GET "/" r (html-response <index> r))
    (POST "/save" [name :as r] (api-save name r))
    (GET "/delete" [name :as r] (api-delete name r))
    (route/resources "/static" {:root "static"})
    (route/not-found (html-response <not-found>))))

(def app (-> routes
             (wrap-defaults site-defaults)
             (wrap-reload)))

(defn start-server []
  (jetty/run-jetty app {:port 7654}))
