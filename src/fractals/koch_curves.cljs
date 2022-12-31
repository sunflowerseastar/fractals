(ns fractals.koch-curves
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-centered-square-canvas-positioning get-sentence]]
   [fractals.components :refer [render-canvas! switcher-a]]
   [fractals.turtle :refer [generate-center-and-plot-points]]))

(def num-lines (atom 0))

(def active-koch-variation (atom 0))

(def koch-variations
  [{:name "a"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}
   {:name "b"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :step-division 6
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}
   {:name "c"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :+ :F :- :F :- :F :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}
   {:name "d"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}
   {:name "e"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :- :F :F :- :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}
   {:name "f"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :- :F :+ :F :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}])

;; create a separate num-iterations atom for each variation
(def koch-iterations [(atom 1) (atom 1) (atom 1) (atom 1) (atom 1) (atom 1)])

;; canvas
(defn draw!
  [canvas]
  (let [grammar (get koch-variations @active-koch-variation)
        sentence (get-sentence grammar @(get koch-iterations @active-koch-variation))]
    (generate-center-and-plot-points canvas
                                     (if (:initial-heading grammar) (:initial-heading grammar) (:delta grammar))
                                     (:delta grammar)
                                     grammar
                                     sentence)))

(defn koch-curves [window-width]
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
