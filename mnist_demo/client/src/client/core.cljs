(ns client.core
    (:require [reagent.core :as r]))

(enable-console-print!)
(println "This text is printed from src/client/core.cljs.")

(def host "http://db.science.uoit.ca:8000")
(def *num-images-per-digit* 10)
(def *image-size* 104)

(defonce app-state (r/atom nil))
(defn initialize-state! []
  (swap! app-state
         assoc
         :learning nil
         :started nil
         :counter 0
         :images (for [d (range 10)
                       i (range *num-images-per-digit*)]
                   {:digit d
                    :index i
                    :predicted (rand-int 10)
                    :id (+ i (* d *num-images-per-digit*))
                    :url (str host "/image/" d "/" i)})))

(defn predict! [& [callback]]
  (swap! app-state assoc :learning true)
  (.getJSON js/jQuery 
            (str host "/learn")
            (fn [data]
              (swap! app-state
                     (fn [state]
                          (-> state
                              (assoc :images (for [[im y] (map vector (:images state) (js->clj data))]
                                               (assoc im :predicted y)))
                              (assoc :learning nil)
                              (update :counter inc))))
              (when callback (callback)))))

(defn restart! [& [callback]]
  (swap! app-state assoc :learning true)
  (.get js/jQuery
        (str host "/restart")
        (fn [data]
          (swap! app-state
                 (fn [state]
                   (-> state
                       (update :counter inc)
                       (assoc :learning nil))))
          (when callback (callback)))))

(defn organized-bucket
  [images]
  (let [bucket (into [] (repeat 10 []))
        place (fn [bucket im]
                 (let [i (if (:predicted im) (:predicted im) (:digit im))]
                   (update bucket i conj im)))]
    (reduce place bucket images)))

(defn accuracy [images]
  (let [x (reduce (fn [state im] 
                     (if (= (:digit im) (:predicted im))
                        (-> state
                            (update :correct inc)
                            (update :total inc))
                        (-> state
                            (update :total inc))))
                    {:correct 0
                     :total 0}
                    images)]
    (-> (/ (float (:correct x)) (:total x))
        (* 100)
        (Math/round)
        (str "%"))))



(defn do-loop! []
  (let [state @app-state]
    (when (:started state)
      (if (zero? (rem (:counter state) 10))
        (do (println "reset")
            (restart! (fn [] (js/setTimeout do-loop! 2000))))
        (do (println "prediction")
            (predict! (fn [] (js/setTimeout do-loop! 2000))))))))

(defn stop-loop! []
  (println "stopping loop")
  (swap! app-state assoc :started nil :counter 0))

(defn start-loop! []
  (println "starting loop")
  (swap! app-state assoc :started true)
  (restart! do-loop!))

;; ==================================================================

(defn Image
  [style im]
  (let [correct (= (:predicted im) (:digit im))
        imstyle (if correct
                   {:opacity 1.0}
                   {:opacity 0.5
                    :border "2px solid red"})]
    [:div {:style (merge style
                         {:display :inline-block 
                          :transition "all 1s"})}
     [:img {:src (:url im)
            :style imstyle}]]))

(defn ImageGrid
  [bucket]
  [:div {:style {:position :relative}}
   (for [[i slot] (map-indexed vector bucket) 
         :let [top (* i *image-size*)]]
      (for [[j im] (map-indexed vector slot)
            :let [left (* j *image-size*)]]
        ^{:key (:id im)}
        [Image {:position :absolute
                :left left
                :top top} im]))])

(defn App []
  (r/create-class
    {:reagent-render
     (fn []
       [:div {:style {:position :fixed
                      :width "100%"
                      :height "100%"
                      :top 0
                      :left 0
                      :cursor :pointer
                      :background (if (:started @app-state) :black :maroon)} 
              :on-click (fn []
                           (if (:started @app-state)
                             (stop-loop!)
                             (start-loop!)))}
        [ImageGrid (organized-bucket (:images @app-state))]
        ;; learning indicator
        (if (:learning @app-state)
          [:div {:style {:position :fixed
                         :left "50%"
                         :top "50%"
                         :width 300
                         :height 200
                         :margin-left -150
                         :margin-top -100
                         :display :flex
                         :align-items :center
                         :justify-content :center
                         :color :white
                         :font-size 50
                         :font-weight 800
                         :opacity 0.3
                         :font-family "Helvetica"}}
           [:p "Learning"]])
        ;; cycle indicator
        [:div {:style {:position :fixed
                       :top "50%"
                       :right 50
                       :font-size 50
                       :color :teal
                       :font-family "Helvetica"
                       :padding 20
                       :font-weight :bold}
               }
         [:span (accuracy (:images @app-state))]]])
     }))

(defn main []
  (initialize-state!)
  (r/render-component [App] (. js/document (getElementById "app"))))

(main)

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  (main)
)
