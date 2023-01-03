(ns fractals.sierpinski-curve
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-centered-equilateral-triangle-canvas-positioning get-sentence]]
   [fractals.components :refer [inc-dec render-canvas!]]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 0))
(def step-size (atom 20))
(def num-iterations (atom 5))
(def num-lines (atom 0))

;; Alphabet: X, Y
;; Constants: F, +, −
;; Axiom: XF
;; Production rules:
;; X → YF + XF + Y
;; Y → XF − YF − X

(def sierpinski-curve-grammar
  {:variables #{:X :Y}
   :constants #{:F :+ :-}
   :start [:F :X]
   :rules {:X [:Y :F :+ :X :F :+ :Y]
           :Y [:X :F :- :Y :F :- :X]}
   :actions {:F :forward :+ :left :- :right}})

(defn draw-forward! [context]
  (let [is-flipped (even? @num-iterations)
        angle (if is-flipped @angle (mod (+ 120 @angle) 360))
        theta (/ (* Math/PI angle) 180.0)
        new-x ((if is-flipped + -) @x (* @step-size (Math/cos theta)))
        new-y (- @y (* @step-size (Math/sin theta)))]
    (reset! x new-x)
    (reset! y new-y)
    (.lineTo context new-x new-y)
    (swap! num-lines inc)))

(defn turtle-draw-to-canvas! [context grammar sentence]
  (let [is-flipped (even? @num-iterations)]
    (doseq [letter (filter identity sentence)]
      (let [action (letter (:actions grammar))]
        ;; (println "letter:" letter "action:" action "@angle:" @angle)
        (cond
          (= action :forward)
          (draw-forward! context)
          (= action :left)
          (swap! angle #(mod ((if is-flipped - +) % 60) 360))
          (= action :right)
          (swap! angle #(mod ((if is-flipped + -) % 60) 360)))))))

;; equilateral triangle height
;; (defn eth [len] (/ (* (Math/sqrt 3) len) 2))

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        [starting-x starting-y triangle-length]
        (get-centered-equilateral-triangle-canvas-positioning
         (-> context .-canvas .-clientWidth)
         (-> context .-canvas .-clientHeight))
        sentence (drop 1 (get-sentence sierpinski-curve-grammar @num-iterations))]

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! angle 0)
    ;; (.clearRect context 0 0 (.-width canvas) (.-height canvas))
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step-size (/ triangle-length (reduce * (repeat @num-iterations 2))))

    ;; draw
    (.lineTo context starting-x starting-y)
    (turtle-draw-to-canvas! context sierpinski-curve-grammar sentence)
    (draw-forward! context)
    (.stroke context)))

(defn sierpinski-curve [window-width]
  [:<>
   [:div.controls [inc-dec num-iterations 9]]
   [:div.meta [:span "lines drawn: " @num-lines]]
   [render-canvas! draw! window-width num-iterations]])
