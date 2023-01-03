(ns fractals.snowflake
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-centered-equilateral-triangle-canvas-positioning get-centered-snowflake-canvas-positioning get-sentence]]
   [fractals.components :refer [inc-dec render-canvas! switcher]]
   [fractals.turtle :refer [turtle-draw-to-canvas!]]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 90))
(def step-size (atom 20))
(def num-lines (atom 0))

(def active-koch-variation (atom 0))

(def koch-variations
  [{:name "basic"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :- :F :- :- :F]
    :rules {:F [:F :+ :F :- :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :starting-angle 60
    :delta 60
    :step-division 5
    :canvas-inner-square-size #(/ (* % 3) 5)
    :inner-square-padding #(/ % 3)
    :max-iterations 6
    :positioning-fn get-centered-snowflake-canvas-positioning}
   {:name "antisnowflake"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :- :F :- :- :F]
    :rules {:F [:F :- :F :+ :+ :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :starting-angle 60
    :delta 60
    :step-division 5
    :canvas-inner-square-size #(/ (* % 3) 5)
    :inner-square-padding #(/ % 3)
    :max-iterations 6
    :positioning-fn get-centered-equilateral-triangle-canvas-positioning}])

;; create a separate num-iterations atom for each variation
(def koch-iterations [(atom 2) (atom 1)])

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        grammar (get koch-variations @active-koch-variation)
        [starting-x starting-y triangle-length]
        ((:positioning-fn grammar)
         (-> context .-canvas .-clientWidth)
         (-> context .-canvas .-clientHeight))
        sentence (get-sentence grammar @(get koch-iterations @active-koch-variation))]

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! num-lines 0)
    (reset! angle (:starting-angle grammar))
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step-size (nth (iterate #(/ % 3) triangle-length) @(get koch-iterations @active-koch-variation)))

    ;; draw
    (.lineTo context starting-x starting-y)
    (turtle-draw-to-canvas! context x y step-size angle (:delta grammar) num-lines grammar sentence)
    (.stroke context)))

(defn snowflake [window-width]
  (let [num-iterations (get koch-iterations @active-koch-variation)
        max-iterations (:max-iterations (get koch-variations @active-koch-variation))]
    [:<>
     [:div.controls
      [:div
       [inc-dec num-iterations max-iterations]
       [switcher koch-variations active-koch-variation]]]
     [:div.meta [:span "lines drawn: " @num-lines]]
     ;; the concat is to include all the koch-iterations as redraw-atoms
     [apply render-canvas! (concat [draw! window-width active-koch-variation] koch-iterations)]]))
