(ns csci-web3.websocket
  (:require [org.httpkit.server :as httpkit]))

(defn handler [r]
  (httpkit/with-channel r ch
    (println (str "connection from:" (:uri r)))
    (httpkit/on-close ch (fn [status] (println "closed:" status)))
    (httpkit/on-receive ch (fn [data] (println "received:" data)))))

(defn -main []
  (let [port 7655]
    (println "Websocket server running at port: " port)
    (httpkit/run-server handler {:port port})))

