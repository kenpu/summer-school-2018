(ns app.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(defn Footer [args]
  (let [{:keys [author date company]} args
        style {:style {:margin-right 30
                       :font-size "20pt"}}]
    [:div {:style {:position :fixed
                   :bottom 0
                   :left 0
                   :width "100%"
                   :background :black
                   :text-align :right
                   :color :white}}
     [:span style author]
     [:span style company]
     [:span style "©" date]]))

(defn Counter
  []
  (let [counter-state (r/atom 0)]
    (fn [message]
      [:div
       [:button {:on-click (fn [] (swap! counter-state inc))} "Increment"]
       [:p 
        [:span message]
        [:span {:style {:margin-left 10
                        :font-weight :bold}}
         @counter-state]]])))

(defn LinkedCounter
  [counter-state message & [option]]
  (fn [counter-state message & [option]]
    (let [count-value @counter-state
          color (cond
                  (< count-value 3) "#888"
                  (< count-value 10) "#f88"
                  :else "#000")]
      [:span {:style {:color color
                      :user-select :none}
              :on-click (fn [e]
                          (when-not (= option :readonly)
                            (swap! counter-state inc)))} message " " count-value])))

(defn triangle-layer [w]
  (r/create-class
    {:reagent-render (fn [w] 
                       [:div {:style {:width w
                                      :height 20
                                      :background "#888"
                                      :border "thin solid black"
                                      :margin-top 2}}])
     :component-will-mount (fn [this]
                             (println "another triangle layer will be seen"))
     :component-will-unmount (fn [this]
                               (println "a triangle layer will be destroyed"))
     }))

(defn update-counter [v delta]
  (let [v' (+ v delta)]
    (if (pos? v') v' v)))

(defn triangle
  [counter-state]
  [:div
   {:on-click #(swap! counter-state update-counter -3)}
   (for [i (range @counter-state)]
     (let [w (+ 10 (* i 10))]
       ^{:key i}
       [triangle-layer w]))])

(defn App [] 
  (let [c1-state (r/atom 0)]
    [:div.container
     [:h1 "Global Watch Financials"]
     [:div.jumbotron
      [:h1 "Leading Machine Learning Managed Banking Products"]
      [:h2 "The one source of personal financial freedom"]
      [LinkedCounter c1-state "Number of products:" :readonly]
      ]
     [:div.row
      [:div.col-xs-6
       [:h3 "About us"]
       [:p "We are a team of technos"]]
      [:div.col-xs-6
       [:h3 "Clients"]
       [:p "blah blah"]
       [LinkedCounter c1-state "Number of clients:"]
       [triangle c1-state]
       ]]
     [Counter "Number of visitors:"]
     [Footer {:author "Ken"
              :company "UOIT"
              :date "2018"}]
     ]))

(defn main [] (r/render [App] (.getElementById js/document "main-body")))

(main)
