(ns fractals.l-system
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-sentence]]
   [fractals.components :refer [inc-dec-with-vec-atom render-canvas! switcher]]
   [fractals.turtle :refer [generate-center-and-plot-points]]))

(def num-lines (atom 0))

(def active-koch-variation (atom 0))

(def default-initial-num-iterations 3)
(def koch-iterations (atom nil))

;; canvas
(defn draw! [koch-variations canvas]
  (let [grammar (get koch-variations @active-koch-variation)
        sentence (get-sentence grammar (get @koch-iterations @active-koch-variation))]
    (generate-center-and-plot-points canvas
                                     (if (:initial-heading grammar) (:initial-heading grammar) (:delta grammar))
                                     (:delta grammar)
                                     grammar
                                     sentence
                                     num-lines)))

(defn l-system-with-iterations-atom [window-width koch-variations]
  (let [num-iterations (get @koch-iterations @active-koch-variation)
        max-iterations (:max-iterations (get koch-variations @active-koch-variation))]
    [:<>
     [:div.controls
      [:div
       [inc-dec-with-vec-atom num-iterations koch-iterations max-iterations active-koch-variation]
       (when (> (count koch-variations) 1)
         [switcher koch-variations active-koch-variation])]]
     [:div.meta [:span "lines drawn: " @num-lines]]
     [render-canvas! (partial draw! koch-variations) window-width active-koch-variation]]))

(defn l-system [window-width koch-variations]
  (reagent/create-class
   {:component-will-unmount
    (fn [] (reset! koch-iterations nil)
      (reset! active-koch-variation 0))

    :reagent-render
    (fn []
      (when (nil? @koch-iterations)
        (let [initial-koch-iterations (map #(if (:initial-num-iterations %)
                                              (:initial-num-iterations %)
                                              default-initial-num-iterations)
                                           koch-variations)]
          (reset! koch-iterations (vec initial-koch-iterations))))
      (l-system-with-iterations-atom window-width koch-variations))}))
