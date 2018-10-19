(ns progressive_web_client.core
    (:require [reagent.core :as r]))

(enable-console-print!)

(println "This text is printed from src/progressive_web_client/core.cljs. 
         Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (r/atom {:x 0
                            :y 0}))

(defn bouncing-ball
  [w h x y]
  [:div {:style {:width w
                 :height h
                 :background "black"}}
   [:svg {:width w 
          :height h}
    [:circle {:cx x
              :cy y
              :r 40
              :stroke "white"
              :stroke-width 10}]]
   ]
  )

(defn hello-world 
  []
  (let [{:keys [x y]} @app-state]
    [:div
     [:h1 "Demo"]
     [:h3 "By Andrei, Michael and Ken"]
     [bouncing-ball 500 500 x y]]))

(defn increase [x dx maxx]
  (cond
    (> x maxx) 0
    :else (+ x dx)))

(def ^:const dx 1)
(def ^:const dt 50)

(defn start-animation
  [app-state]
  (js/setTimeout
    (fn []
      (swap! app-state (fn [state] (-> state
                                       (update :x increase dx 500)
                                       (update :y increase dx 500))))
      (start-animation app-state)) dt))

(defn main 
  []
  (start-animation app-state)
  (r/render-component 
    [hello-world] 
    (. js/document (getElementById "app"))))

(main)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
