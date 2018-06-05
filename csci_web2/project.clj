(defproject csci_web2 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ring "1.6.3"]
                 [ring-oauth2 "0.1.4"]
                 [ring/ring-defaults "0.3.1"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [stuarth/clj-oauth2 "0.3.2"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [org.postgresql/postgresql "LATEST"]
                 [clj-http "3.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [environ "1.1.0"]]
  :plugins [[lein-environ "1.1.0"]]
  :main csci-web2.core/start-server
)
