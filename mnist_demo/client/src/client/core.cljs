(ns client.core
    (:require [reagent.core :as r]))

(enable-console-print!)
(println "This text is printed from src/client/core.cljs.")

(def host "http://db.science.uoit.ca:9999")
(def *num-images-per-digit* 10)


(defn make-image-buckets []
  (into {}
        (for [digit (range 10)]
          [digit
           (for [index (range *num-images-per-digit*)]
             (str host "/image/" digit "/" index))])))

(defonce app-state (r/atom nil))

(defn initialize-state!
  []
  (swap! app-state assoc :image-buckets (make-image-buckets)))

(defn predict!
  []
  (let (


;; ==================================================================

(defn Image
  [src]
  [:img {:src src
         :style {:display :block}}])

(defn ImageBuckets
  [image-buckets]
  [:div
    [:div {:style {:display :flex
                   :flex-direction :column}}
     (for [digit (range 10)]
       ^{:key digit}
       [:div {:style {:display :flex
                      :flex-direction :row}}
        (for [[i im] (map-indexed vector (get image-buckets digit))] 
          ^{:key i}
          [:div [Image im]])])]])


(defn App []
  (r/create-class
    {:reagent-render
     (fn []
       (let [images (get @app-state :image-buckets)]
         [:div {:style {:display :flex
                        :position :fixed
                        :left 0
                        :top 0
                        :background :black
                        :width "100%"
                        :height "100%"}
                :on-click (fn [ev]
                            (println "Ding")
                            (predict!))}
          [ImageBuckets images]]))
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
