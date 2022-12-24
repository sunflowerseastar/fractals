(ns sierpinski.koch
  (:require
   [reagent.core :as reagent :refer [atom]]
   [sierpinski.components :refer [render-canvas! switcher-a]]
   [sierpinski.l-system :refer [l-system]]
   [sierpinski.turtle :refer [draw-turtle!]]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 90))
(def step (atom 20))
(def num-lines (atom 0))

(def active-koch-variation (atom 0))

(def koch-variations
  [{:name "a"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :- :F :+ :F :+ :F :F :- :F :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :step-division 4
    :iters (atom 1)
    :canvas-inner-square-size #(/ (* % 3) 5)
    :inner-square-padding #(/ % 3)
    :max-iterations 4}
   {:name "b"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :+ :F :F :- :F :F :- :F :- :F :+ :F :+ :F :F :- :F :- :F :+ :F :+ :F :F :+ :F :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :step-division 6
    :iters (atom 1)
    :canvas-inner-square-size #(/ % 2)
    :inner-square-padding #(/ % 2)
    :max-iterations 3}])

;; create a separate num-iterations atom for each variation
(def koch-iterations [(atom 1) (atom 1)])

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        ;; calculate the positioning of the triangle. And by "triangle," I mean
        ;; the resulting triangle-ish Sierpinski curve.
        canvas-padding-px 20
        canvas-width (- (-> context .-canvas .-clientWidth) (* 2 canvas-padding-px))
        canvas-height (- (-> context .-canvas .-clientHeight) (* 2 canvas-padding-px))

        short-edge (if (< canvas-width canvas-height) :width :height)
        short (if (= short-edge :width) canvas-width canvas-height)
        long (if (= short-edge :width) canvas-height canvas-width)

        grammar (get koch-variations @active-koch-variation)

        ;; Since the quadratic koch island goes outside the bounds of the
        ;; original square on each iteration, it needs some extra padding. This
        ;; will give the original square (the drawing of the axiom before any
        ;; rewrites) enough padding to accommodate the iterations.
        canvas-inner-square-size ((:canvas-inner-square-size grammar) short)
        inner-square-padding ((:inner-square-padding grammar) canvas-inner-square-size)

        starting-x (if (= short-edge :height) (+ (/ (- long short) 2) inner-square-padding canvas-padding-px)
                       (+ canvas-padding-px inner-square-padding))
        starting-y (if (= short-edge :width) (- (+ (/ (- long short) 2)
                                                   canvas-width
                                                   canvas-padding-px)
                                                inner-square-padding)
                       (- (+ canvas-height canvas-padding-px) inner-square-padding))

        sentence (l-system grammar @(get koch-iterations @active-koch-variation))]

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! num-lines 0)
    (reset! angle 90)
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step (/ canvas-inner-square-size
                    (reduce * (repeat @(get koch-iterations @active-koch-variation) (:step-division grammar)))))

    ;; draw
    (.lineTo context starting-x starting-y)
    (draw-turtle! context x y step angle num-lines grammar sentence)
    (.stroke context)))


(defn sierpinski-koch [window-width]
  (let [num-iterations (get koch-iterations @active-koch-variation)
        max-iterations (:max-iterations (get koch-variations @active-koch-variation))]
    [:<>
     [:div.controls-post-canvas-left
      [:div
       (into [:div.switcher]
             (map-indexed
              (fn [i type]
                (let [is-active (= @active-koch-variation i)]
                  [switcher-a
                   is-active
                   #(when-not is-active (reset! active-koch-variation i))
                   (:name type)]))
              koch-variations))
       [:span " | "]
       [:div.inc-dec
        [:span "iterations:"]
        [:a.box-button {:class (when (< @num-iterations 1) "inactive")
                        :on-click #(when (pos? @num-iterations) (swap! num-iterations dec))} "-"]
        [:span @num-iterations]
        [:a.box-button {:class (when (>= @num-iterations max-iterations) "inactive")
                        :on-click #(when (< @num-iterations max-iterations) (swap! num-iterations inc))} "+"]]]]
     [:div.controls-post-canvas-right [:span "lines drawn: " @num-lines]]
     ;; the concat is to include all the koch-iterations as redraw-atoms
     [apply render-canvas! (concat [draw! window-width active-koch-variation] koch-iterations)]]))
