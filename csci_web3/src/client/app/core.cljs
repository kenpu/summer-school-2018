(ns app.core
  (:require [clojure.string :as s]))

(enable-console-print!)

(dotimes [i 10]
  (println "hello world"
           (s/join "" (repeat i "*"))))
