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

           ;; oauth
           [ring.middleware.oauth2 :refer [wrap-oauth2]]
           [clj-http.client :as client]
           [clojure.data.json :as json]

           ;; env
           [environ.core :refer [env]]
           ))

(def db-spec {:subprotocol "postgresql" 
              :subname (env :db-url)
              :user (env :db-user)
              :password (env :db-password)})

(def github-spec
  {:github {:authorize-uri    "https://github.com/login/oauth/authorize"
			:access-token-uri "https://github.com/login/oauth/access_token"
			:client-id        (env :client-id)
			:client-secret    (env :client-secret)
			:scopes           ["user:email"]
			:launch-uri       "/oauth2/github"
			:redirect-uri     "/oauth2/github/callback"
			:landing-uri      "/landing"}})

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

(defn get-github-user-info [token]
  (if (not (empty? token))
    (-> (client/get "https://api.github.com/user/emails" 
                    {:headers {:Authorization (str "token " token)}})
        (get :body)
        (json/read-str :key-fn keyword))
    nil))

(defn <index>
  [r]
  (let [names (db-get-names)
        github-token (get-in r [:oauth2/access-tokens :github :token])
        user-info (get-github-user-info github-token)]
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
       [:hr]
       (if (nil? github-token)
         [:p "Do you want to login?"
            " client-id " (env :client-id)
            [:a {:href "/oauth2/github"} " as github user?"]]
         [:div [:pre (with-out-str (pprint user-info))]
          [:a {:href "/logout"} "logout"]])
       ])))

(defn <user>
  [r]
  (let [github-token (get-in r [:oauth2/access-tokens :github :token])
        user-info (get-github-user-info github-token)]
    (page-html [:div
                [:h1 "This is the user"]
                [:pre (with-out-str (pprint user-info))]])))

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

(defn logout [r]
  (-> (response/redirect "/")
      (assoc :session nil)))

(def routes
  (c/routes 
    (GET "/" r (html-response <index> r))
    (POST "/save" [name :as r] (api-save name r))
    (GET "/delete" [name :as r] (api-delete name r))
    ;; (GET "/oauth2/github/callback" r (oauth-callback r))
    (GET "/landing" r (-> (response/response "this is landing")
                          (response/content-type "text/html")))
    (GET "/user" r (html-response <user>))
    (GET "/logout" r (logout r))
    (route/resources "/static" {:root "static"})
    (route/not-found (html-response <not-found>))))

(def my-site-defaults
  (-> site-defaults (assoc-in [:session :cookie-attrs :same-site] :lax)))

(def app (-> routes
             (wrap-oauth2 github-spec)
             (wrap-defaults my-site-defaults)
             (wrap-reload)))

(defn start-server []
  (jetty/run-jetty app {:port 7654}))
