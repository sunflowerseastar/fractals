(ns fractals.barnsley
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [center-draw-points]]
   [fractals.components :refer [render-canvas!]]))

;; https://martin.varela.fi/2017/10/02/fractals-complexity-from-simplicity-part-2/
(def transforms {:f1 [0 0 0 0.16 0 0]
                 :f2 [0.85 0.04 -0.04 0.85 0 1.6]
                 :f3 [0.2 -0.26 0.23 0.22 0 1.6]
                 :f4 [-0.15 0.28 0.26 0.24 0 0.44]})

(defn- apply-transform [point coeffs]
  (if (empty? coeffs)
    [0 0]
    (let [[x y] point
          [a b c d e f] coeffs
          xx  (+ (* a x) (* b y) e)
          yy  (+ (* c x) (* d y) f)]
      [xx yy])))

(defn- transform-probs [i]
  (cond
    (<= i 0.01) :f1
    (<= i (+ 0.01 0.85)) :f2
    (<= i (+ 0.01 0.85 0.07)) :f3
    :else :f4))

(defn barnsley-fern [num-points]
  (let [probs (repeatedly num-points rand)
        coeflist (map (comp transforms transform-probs) probs)]
    (reductions apply-transform [0 0] coeflist)))

(def num-iterations (atom 2))
(def num-plots (atom 0))

(defn transpose [m]
  (apply mapv vector m))


;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        canvas-width (-> context .-canvas .-clientWidth)
        canvas-height (-> context .-canvas .-clientHeight)
        draw-points (barnsley-fern 32768)
        centered-draw-points (center-draw-points canvas-width canvas-height draw-points)]

    (.setAttribute canvas "height" canvas-height)
    (.setAttribute canvas "width" canvas-width)

    (reset! num-plots 0)

    (doseq [xy centered-draw-points]
      (swap! num-plots inc)
      (.moveTo context (first xy) (second xy))
      (.beginPath context)
      (.arc context (first xy) (second xy) 1 0 (* 2 js/Math.PI))
      (.fill context))
    ))

(defn barnsley [window-width]
  [:<>
   ;; [:div.controls [inc-dec num-iterations 5]]
   [:div.meta [:span "plots: " @num-plots]]
   [render-canvas! draw! window-width num-iterations]])
