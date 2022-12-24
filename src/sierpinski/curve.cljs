(ns sierpinski.curve
  (:require
   [reagent.core :as reagent :refer [atom]]
   [sierpinski.components :refer [render-canvas!]]
   [sierpinski.l-system :refer [l-system]]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 0))
(def step (atom 20))
(def num-iterations (atom 4))
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
        new-x ((if is-flipped + -) @x (* @step (Math/cos theta)))
        new-y (- @y (* @step (Math/sin theta)))]
    (reset! x new-x)
    (reset! y new-y)
    (.lineTo context new-x new-y)
    (swap! num-lines inc)))

(defn draw-turtle! [context grammar sentence]
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
(defn eth [len] (/ (* (Math/sqrt 3) len) 2))

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        ;; calculate the positioning of the triangle. And by "triangle," I mean
        ;; the resulting triangle-ish Sierpinski curve.
        canvas-padding-px 20
        canvas-width (- (-> context .-canvas .-clientWidth) (* 2 canvas-padding-px))
        canvas-height (- (-> context .-canvas .-clientHeight) (* 2 canvas-padding-px))

        ;; get the height of the resulting triangle if it was based on the
        ;; canvas width/height
        canvas-width-eth (eth canvas-width)
        canvas-height-eth (eth canvas-height)

        ;; if we drew the triangle based on the canvas width, would it be taller
        ;; than the canvas?...
        is-triangle-taller-than-canvas-height (> canvas-width-eth canvas-height)
        ;; ...and then determine whether the triangle size will be based on the
        ;; width or the height
        triangle-length (if is-triangle-taller-than-canvas-height canvas-height-eth canvas-width)

        ;; from there, figure out the [bottom left] starting x/y position in
        ;; order to center the triangle in the canvas
        starting-x (if is-triangle-taller-than-canvas-height
                     (+ (- (/ canvas-width 2) (/ canvas-height-eth 2)) canvas-padding-px)
                     canvas-padding-px)
        starting-y (if is-triangle-taller-than-canvas-height
                     canvas-height
                     (- canvas-height (/ (- canvas-height canvas-width-eth) 2)))

        sentence (drop 1 (l-system sierpinski-curve-grammar @num-iterations))]

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! angle 0)
    ;; (.clearRect context 0 0 (.-width canvas) (.-height canvas))
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step (/ triangle-length (reduce * (repeat @num-iterations 2))))

    ;; draw
    (.lineTo context starting-x starting-y)
    (draw-turtle! context sierpinski-curve-grammar sentence)
    (draw-forward! context)
    (.stroke context)))

(defn sierpinski-curve [window-width]
  [:<>
   [:div.controls-post-canvas-left
    [:div.inc-dec
     [:span "iterations:"]
     [:a.box-button {:class (when (< @num-iterations 1) "inactive")
                     :on-click #(when (pos? @num-iterations) (swap! num-iterations dec))} "-"]
     [:span @num-iterations]
     [:a.box-button {:class (when (>= @num-iterations 9) "inactive")
                     :on-click #(when (< @num-iterations 9) (swap! num-iterations inc))} "+"]]]
   [:div.controls-post-canvas-right [:span "lines drawn: " @num-lines]]
   [render-canvas! draw! window-width num-iterations]])
