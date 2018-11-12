(ns client.core
    (:require [reagent.core :as r]))

(enable-console-print!)
(println "This text is printed from src/client/core.cljs.")

(def host "http://db.science.uoit.ca:8000")
(def *num-images-per-digit* 10)

(defonce app-state (r/atom nil))
(defn initialize-state! []
  (swap! app-state
         assoc
         :images (for [d (range 10)
                       i (range *num-images-per-digit*)]
                   {:digit d
                    :index i
                    :predicted nil
                    :id (+ i (* d *num-images-per-digit*))
                    :url (str host "/image/" d "/" i)})))

(defn predict! []
  (.getJSON js/jQuery 
            (str host "/learn")
            (fn [data]
              (swap! app-state
                     update
                     :images
                     (fn [images]
                       (for [[im y] (map vector images (js->clj data))]
                         (assoc im :predicted y)))))))

(defn organized-bucket
  [images]
  (let [bucket (into [] (repeat 10 []))
        place (fn [bucket im]
                 (let [i (if (:predicted im) (:predicted im) (:digit im))]
                   (update bucket i conj im)))]
    (reduce place bucket images)))

;; ==================================================================

(defn Image
  [im]
  (let [correct (= (:predicted im) (:digit im))]
    [:img {:src (:url im)
           :style {:display :inline-block
                   :opacity (if correct 0.5 1.0)}}]))

(defn ImageGrid
  [bucket]
  [:div
   (for [[i slot] (map-indexed vector bucket)]
     ^{:key i}
     [:div {:style {:display :flex}}
      (for [im slot]
        ^{:key (:id im)}
        [Image im])])])

(defn App []
  (r/create-class
    {:reagent-render
     (fn []
       [:div {:style {:position :fixed
                      :width "100%"
                      :height "100%"
                      :top 0
                      :left 0
                      :background :black}
              :on-click (fn [e] (predict!))}
        [ImageGrid (organized-bucket (:images @app-state))]])
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
