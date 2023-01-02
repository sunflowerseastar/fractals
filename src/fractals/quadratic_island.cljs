(ns fractals.quadratic-island
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-centered-square-canvas-positioning get-sentence]]
   [fractals.components :refer [render-canvas! switcher-a]]
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
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :- :F :+ :F :+ :F :F :- :F :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :starting-angle 90
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn #(/ (* % 3) 5)
    :inner-square-padding-fn #(/ % 3)
    :max-iterations 4
    :positioning-fn get-centered-square-canvas-positioning}
   {:name "alternate"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :+ :F :F :- :F :F :- :F :- :F :+ :F :+ :F :F :- :F :- :F :+ :F :+ :F :F :+ :F :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :starting-angle 90
    :delta 90
    :step-division 6
    :canvas-inner-square-size-fn #(/ % 2)
    :inner-square-padding-fn #(/ % 2)
    :max-iterations 3
    :positioning-fn get-centered-square-canvas-positioning}])

;; create a separate num-iterations atom for each variation
(def koch-iterations [(atom 1) (atom 1)])

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        grammar (get koch-variations @active-koch-variation)
        [starting-x starting-y canvas-inner-square-size]
        ((:positioning-fn grammar)
         (-> context .-canvas .-clientWidth)
         (-> context .-canvas .-clientHeight)
         (:canvas-inner-square-size-fn grammar)
         (:inner-square-padding-fn grammar))

        sentence (get-sentence grammar @(get koch-iterations @active-koch-variation))]

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! num-lines 0)
    (reset! angle (:starting-angle grammar))
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step-size (/ canvas-inner-square-size
                         (reduce * (repeat @(get koch-iterations @active-koch-variation) (:step-division grammar)))))

    ;; draw
    (.lineTo context starting-x starting-y)
    (turtle-draw-to-canvas! context x y step-size angle (:delta grammar) num-lines grammar sentence)
    (.stroke context)))


(defn quadratic-island [window-width]
  (let [num-iterations (get koch-iterations @active-koch-variation)
        max-iterations (:max-iterations (get koch-variations @active-koch-variation))]
    [:<>
     [:div.controls
      [:div
       [:div.inc-dec
        [:a.box-button {:class (when (< @num-iterations 1) "inactive")
                        :on-click #(when (pos? @num-iterations) (swap! num-iterations dec))} "-"]
        [:span @num-iterations]
        [:a.box-button {:class (when (>= @num-iterations max-iterations) "inactive")
                        :on-click #(when (< @num-iterations max-iterations) (swap! num-iterations inc))} "+"]]
       [:span " | "]
       (into [:div.switcher]
             (map-indexed
              (fn [i type]
                (let [is-active (= @active-koch-variation i)]
                  [switcher-a
                   is-active
                   #(when-not is-active (reset! active-koch-variation i))
                   (:name type)]))
              koch-variations))]]
     [:div.meta [:span "lines drawn: " @num-lines]]
     ;; the concat is to include all the koch-iterations as redraw-atoms
     [apply render-canvas! (concat [draw! window-width active-koch-variation] koch-iterations)]]))
