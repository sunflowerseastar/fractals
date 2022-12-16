(ns sierpinski.carpet
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

;; canvas
(defn draw!
  [canvas]
  (println "draw!")
  (let [context (.getContext canvas "2d")]
    (.beginPath context)
    ;; draw the circle
    (.arc context 20 20 5 0 (* 2 (.-PI js/Math)) false)
    (set! (.-fillStyle context) "orange")
    (.fill context)
    ;; fill it in
    (set! (.-lineWidth context) 5)
    (set! (.-strokeStyle context) "#003300")
    (.stroke context)))

(def window-width (atom nil))

(defn render-canvas!
  []
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn []
        (let [canvas (.-firstChild @dom-node)]
          (draw! canvas)))

      :component-did-mount
      (fn [this]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn []
        @window-width ;; trigger re-render
        [:div.canvas-container
         [:canvas (if-let [node @dom-node]
                    {:width (.-clientWidth node) :height (.-clientHeight node)})]])})))

(defn sierpinski-carpet []
  [:<>
   [:div.pre-canvas
    [:div.meta [:span "meta"]]]
   [render-canvas!]])
