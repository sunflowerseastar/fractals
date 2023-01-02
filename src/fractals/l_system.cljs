(ns fractals.l-system
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.utility :refer [get-sentence]]
   [fractals.components :refer [render-canvas! switcher-a]]
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
       [:div.inc-dec
        [:a.box-button
         {:class (when (< num-iterations 1) "inactive")
          :on-click #(when (pos? num-iterations)
                       (swap! koch-iterations update @active-koch-variation dec))}
         "-"]
        [:span num-iterations]
        [:a.box-button
         {:class (when (>= num-iterations max-iterations) "inactive")
          :on-click #(when (< num-iterations max-iterations)
                       (swap! koch-iterations update @active-koch-variation inc))}
         "+"]]
       (when (> (count koch-variations) 1) [:span " | "])
       (when (> (count koch-variations) 1)
         (into [:div.switcher]
               (map-indexed
                (fn [i type]
                  (let [is-active (= @active-koch-variation i)]
                    [switcher-a
                     is-active
                     #(when-not is-active (reset! active-koch-variation i))
                     (:name type)]))
                koch-variations)))]]
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
