(defproject csci_web3 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 ;; server-side
                 [ring "1.6.3"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]

                 ;; client-side
                 [org.clojure/clojurescript "1.9.946"]
                 [reagent "0.7.0"]
                 [org.clojure/core.async "0.4.474"]

                 ;; websocket
                 [http-kit "2.2.0"]]

  :plugins [[lein-cljsbuild "1.1.7"]]

  ;; server build config
  :source-paths ["src/server"]
  :main csci-web3.core

  ;; client build config
  :cljsbuild {:builds [{:id "dev" 
                        :source-paths ["src/client"] 
                        :compiler {:main app.core 
                                   :output-to "resources/public/js/main.js"
                                   :output-dir "resources/public/js/out"
                                   :asset-path "/static/js/out"
                                   :source-map true}}
                       {:id "whitespace"
                        :source-paths ["src/client"] 
                        :compiler {:main app.core
                                   :output-to "resources/public/js/main.js"
                                   :optimizations :whitespace}}

                       {:id "prod"
                        :source-paths ["src/client"] 
                        :compiler {:main app.core
                                   :output-to "resources/public/js/main.js"
                                   :optimizations :advanced}}
                       ]})
