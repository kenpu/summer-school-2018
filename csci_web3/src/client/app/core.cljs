(ns app.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.string :as s]
            [reagent.core :as r]
            [clojure.core.async :refer [chan <! >! timeout close!]]
            ))
(enable-console-print!)

(defn body
  [ch box]
  [:div 
   [:button {:on-click (fn [e]
                         (go (>! ch "click")))} "Click"]
   [:h1 "hello world"]
   [:h2 "planet"]
   [:h3 "earth"]
   [:h4 "from " @box]])

(defn view
  [ch]
  (let [data-box (r/atom "Mars")]
    (r/render [body ch data-box] (.getElementById js/document "main-body"))
    data-box))

(defn add-ticks
  [ch t]
  (go-loop []
      (<! (timeout t))
      (>! ch "tick")
      (recur)))

(defn start-animation
  [signals-chan data-box]
  (go
    (loop []
      (println "waiting...")
      (let [x (<! signals-chan)]
        (when x
          (println "got chan")
          (swap! data-box #(str % "." x))
          (recur))))))

(let [ch (chan)]
  (add-ticks ch 2000)
  (start-animation ch (view ch)))
