(ns sierpinski.carpet
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(def max-iterations 4)

(defn draw-the-center-square
  [context x y size]
  (let [inner-square-size (/ size 3)]
    (.fillRect context (+ x inner-square-size) (+ y inner-square-size)
               inner-square-size inner-square-size)))

(defn recursively-draw-the-other-eight-squares
  [iteration-count context x y inner-square-size]
  (doseq [m [0 1 2] n [0 1 2] :when (not (and (= m 1) (= n 1)))]
    (let [inner-square-x (+ x (* inner-square-size m))
          inner-square-y (+ y (* inner-square-size n))]
      (draw-the-center-square context inner-square-x inner-square-y inner-square-size)
      (when (< iteration-count max-iterations)
        (recursively-draw-the-other-eight-squares (inc iteration-count) context inner-square-x inner-square-y (/ inner-square-size 3))))))

;; canvas
(defn draw!
  [canvas]
  (println "draw carpet!")
  (let [context (.getContext canvas "2d")
        ;; calculate the max-area centered square of the canvas
        canvas-width (-> context .-canvas .-clientWidth)
        canvas-height (-> context .-canvas .-clientHeight)
        short-edge (if (< canvas-width canvas-height) :width :height)
        short (if (= short-edge :width) canvas-width canvas-height)
        long (if (= short-edge :width) canvas-height canvas-width)
        canvas-square-size short
        inner-square-size (/ canvas-square-size 3)
        x-offset (if (= short-edge :height) (/ (- long short) 2) 0)
        y-offset (if (= short-edge :width) (/ (- long short) 2) 0)]
    ;; first, draw the largest square in the middle:
    ;; . . .
    ;; . X .
    ;; . . .
    (draw-the-center-square context x-offset y-offset canvas-square-size)
    ;; then, start recursively drawing the other 8 squares
    ;; X X X
    ;; X . X
    ;; X X X
    (recursively-draw-the-other-eight-squares 0 context x-offset y-offset inner-square-size)))

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
