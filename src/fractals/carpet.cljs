(ns fractals.carpet
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.components :refer [render-canvas!]]))

(def num-iterations (atom 2))
(def num-squares (atom 0))

(defn draw-the-center-square
  [context x y size]
  (let [inner-square-size (/ size 3)]
    (.fillRect context (+ x inner-square-size) (+ y inner-square-size)
               inner-square-size inner-square-size)
    (swap! num-squares inc)))

(defn recursively-draw-the-other-eight-squares
  [iteration-count context x y inner-square-size]
  (doseq [m [0 1 2] n [0 1 2] :when (not (and (= m 1) (= n 1)))]
    (let [inner-square-x (+ x (* inner-square-size m))
          inner-square-y (+ y (* inner-square-size n))]
      (draw-the-center-square context inner-square-x inner-square-y inner-square-size)
      (when (< iteration-count @num-iterations)
        (recursively-draw-the-other-eight-squares (inc iteration-count) context inner-square-x inner-square-y (/ inner-square-size 3))))))

;; canvas
(defn draw!
  [canvas]
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
    (.setAttribute canvas "height" canvas-height)
    (.setAttribute canvas "width" canvas-width)

    ;; first, draw the largest square in the middle:
    ;; . . .
    ;; . X .
    ;; . . .
    (draw-the-center-square context x-offset y-offset canvas-square-size)
    ;; then, start recursively drawing the other 8 squares
    ;; X X X
    ;; X . X
    ;; X X X
    (when (> @num-iterations 0)
      (recursively-draw-the-other-eight-squares 1 context x-offset y-offset inner-square-size))))

(defn sierpinski-carpet [window-width]
  [:<>
   [:div.controls
    [:div.inc-dec
     [:span "iterations:"]
     [:a.box-button {:class (when (< @num-iterations 1) "inactive")
                     :on-click #(when (pos? @num-iterations) (swap! num-iterations dec))} "-"]
     [:span @num-iterations]
     [:a.box-button {:class (when (> @num-iterations 4) "inactive")
                     :on-click #(when (<= @num-iterations 4) (swap! num-iterations inc))} "+"]]]
   [:div.meta [:span "squares drawn: " @num-squares]]
   [render-canvas! draw! window-width num-iterations]])
