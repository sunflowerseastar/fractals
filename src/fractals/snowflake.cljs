(ns fractals.snowflake
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-centered-equilateral-triangle-canvas-positioning get-centered-snowflake-canvas-positioning]]
   [fractals.components :refer [render-canvas! switcher-a]]
   [fractals.l-system :refer [l-system]]
   [fractals.turtle :refer [draw-turtle!]]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 90))
(def step (atom 20))
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
    :positioning-fn get-centered-snowflake-canvas-positioning
    }
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
    :positioning-fn get-centered-equilateral-triangle-canvas-positioning
    }])

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
         (-> context .-canvas .-clientHeight)
         20)
        sentence (l-system grammar @(get koch-iterations @active-koch-variation))]

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! num-lines 0)
    (reset! angle (:starting-angle grammar))
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step (nth (iterate #(/ % 3) triangle-length) @(get koch-iterations @active-koch-variation)))

    ;; draw
    (.lineTo context starting-x starting-y)
    (draw-turtle! context x y step angle (:delta grammar) num-lines grammar sentence)
    (.stroke context)))

(defn snowflake [window-width]
  (let [num-iterations (get koch-iterations @active-koch-variation)
        max-iterations (:max-iterations (get koch-variations @active-koch-variation))]
    [:<>
     [:div.controls
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
     [:div.meta [:span "lines drawn: " @num-lines]]
     ;; the concat is to include all the koch-iterations as redraw-atoms
     [apply render-canvas! (concat [draw! window-width active-koch-variation] koch-iterations)]]))
