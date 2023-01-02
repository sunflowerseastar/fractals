;; This file has two different turtles. One of the turtles draws while it goes.
;; This turtle has to have its starting coordinates and step size pre-calculated
;; in order to be positioned correctly on the canvas.
;;
;; The other turtle does not draw while it goes, but rather returns
;; would-be "draw-points" with arbitrary starting-x/y and step-size. These
;; draw-points are now positioned with respect to the canvas size--and then
;; plotted--subsequently.
(ns fractals.turtle
  (:require [fractals.utility :refer [center-draw-points]]))

(def step-size 20)

;; 1 - 'draw as you go' turtle

(defn draw-forward! [context x y step-size angle num-lines]
  (let [theta (/ (* Math/PI @angle) 180.0)
        new-x (+ @x (* @step-size (Math/cos theta)))
        new-y (- @y (* @step-size (Math/sin theta)))]
    (reset! x new-x)
    (reset! y new-y)
    (.lineTo context new-x new-y)
    (swap! num-lines inc)))

(defn turtle-draw-to-canvas! [context x y step-size angle delta num-lines grammar sentence]
  (doseq [letter (filter identity sentence)]
    (let [action (letter (:actions grammar))]
      (cond
        (= action :forward)
        (draw-forward! context x y step-size angle num-lines)
        (= action :left)
        (swap! angle #(mod (+ % delta) 360))
        (= action :right)
        (swap! angle #(mod (- % delta) 360))))))


;; 2 - 'just return draw points' turtle; positioning and plotting is separate

(defn plot-xy [context [x y]]
  (.lineTo context x y))

(defn point-forward [x y heading]
  (let [theta (/ (* Math/PI heading) 180.0)
        new-x (+ x (* step-size (Math/cos theta)))
        new-y (- y (* step-size (Math/sin theta)))]
    [new-x new-y]))

(defn turtle-return-draw-points [heading delta grammar sentence]
  (let [starting-point [300 300]]
    (loop [letters (filter identity sentence)
           heading heading
           x (first starting-point)
           y (second starting-point)
           acc [starting-point]]
      (if (empty? letters) acc
          (let [action ((first letters) (:actions grammar))]
            (cond (= action :forward)
                  (let [[new-x new-y] (point-forward x y heading)]
                    (recur (rest letters)
                           heading
                           new-x
                           new-y
                           (conj acc [new-x new-y])))
                  (= action :left)
                  (recur (rest letters)
                         (mod (+ heading delta) 360)
                         x y
                         acc)
                  (= action :right)
                  (recur (rest letters)
                         (mod (- heading delta) 360)
                         x y
                         acc)
                  :else
                  (recur (rest letters)
                         heading
                         x y
                         acc)))))))

(defn generate-and-center-draw-points [heading delta grammar sentence canvas-width canvas-height]
  (let [draw-points (turtle-return-draw-points heading delta grammar sentence)]
    (center-draw-points canvas-width canvas-height draw-points)))

(defn generate-center-and-plot-points [canvas heading delta grammar sentence num-lines]
  (let [context (.getContext canvas "2d")
        canvas-width (-> context .-canvas .-clientWidth)
        canvas-height (-> context .-canvas .-clientHeight)
        draw-points (turtle-return-draw-points heading delta grammar sentence)
        centered-draw-points (center-draw-points canvas-width canvas-height draw-points)]

    ;; (println draw-points)

    (.setAttribute canvas "height" canvas-height)
    (.setAttribute canvas "width" canvas-width)

    (reset! num-lines (count draw-points))

    (doseq [xy centered-draw-points]
      (plot-xy context xy))
    (.stroke context)))
