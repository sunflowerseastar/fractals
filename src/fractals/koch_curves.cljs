(ns fractals.koch-curves
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-centered-square-canvas-positioning]]
   [fractals.components :refer [render-canvas! switcher-a]]
   [fractals.l-system :refer [l-system]]
   [fractals.turtle :refer [generate-and-center-draw-points turtle-draw-to-canvas! draw-2]]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 90))
(def step (atom 20))
(def num-lines (atom 0))

(def active-koch-variation (atom 0))

(def koch-variations
  [
   {:name "a"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :starting-angle 90
    :delta 90
    :step-division 4
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
    :starting-angle 90
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
    :starting-angle 90
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
    :starting-angle 90
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
    :starting-angle 90
    :delta 90
    :step-division 6
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
    :starting-angle 90
    :delta 90
    :step-division 4
    :canvas-inner-square-size-fn identity
    :inner-square-padding-fn (fn [_] 0)
    :max-iterations 8
    :positioning-fn get-centered-square-canvas-positioning}
   ])

;; create a separate num-iterations atom for each variation
(def koch-iterations [(atom 1) (atom 1) (atom 1) (atom 1) (atom 1) (atom 1)])

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        grammar (get koch-variations @active-koch-variation)
        ;; [starting-x starting-y length]
        ;; ((:positioning-fn grammar)
        ;;  (-> context .-canvas .-clientWidth)
        ;;  (-> context .-canvas .-clientHeight)
        ;;  (:canvas-inner-square-size-fn grammar)
        ;;  (:inner-square-padding-fn grammar))
        starting-x 200
        starting-y 200
        length 20
        sentence (l-system grammar @(get koch-iterations @active-koch-variation))

        canvas-width (-> context .-canvas .-clientWidth)
        canvas-height (-> context .-canvas .-clientHeight)

        dp-2 (generate-and-center-draw-points angle (:delta grammar) grammar sentence canvas-width canvas-height)


        ;; canvas-center-point [(/ canvas-width 2) (/ canvas-height 2)]
        ;; bounds-center-point [(/ (- max-x min-x) 2) (/ (- max-y min-y) 2)]

        ]

    ;; (println "draw-points:" draw-points)
    ;; (println "bounds:" bounds)
    ;; (println min-x max-x min-y max-y)
    ;; (println canvas-center-point bounds-center-point)

    ;; (println "dp-2:" dp-2)

    ;; setup
    (.setAttribute canvas "width" (-> context .-canvas .-clientWidth))
    (.setAttribute canvas "height" (-> context .-canvas .-clientHeight))
    (reset! num-lines 0)
    (reset! angle (:starting-angle grammar))
    (reset! x starting-x)
    (reset! y starting-y)
    (reset! step (nth (iterate #(/ % 3) length) @(get koch-iterations @active-koch-variation))) ;; this works for b & d

    ;; (.lineTo context starting-x starting-y)
    ;; (map #(do (println "hello")
    ;;           (draw-2 context %)) dp-2)
    (doseq [xy dp-2]
      (draw-2 context xy))
    ;; (draw-2 context dp-2)

    ;; draw
    ;; (.lineTo context starting-x starting-y)
    ;; (turtle-draw-to-canvas! context x y step angle (:delta grammar) num-lines grammar sentence)
    (.stroke context)

    ))

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
