(ns sierpinski.components
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(defn switcher-a
  "'a' as in 'an <a> tag anchor"
  [is-active on-click-fn & children]
  [:a {:on-click on-click-fn :class (when is-active "is-active")} children])

(defn render-canvas!
  [draw-fn & redraw-atoms]
  (println redraw-atoms)
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn []
        (let [canvas (.-firstChild (.-firstChild @dom-node))]
          (println "!! redraw")
          (draw-fn canvas)))

      :component-did-mount
      (fn [this]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn []
        ;; trigger a redraw when any of these atoms change
        (doseq [a redraw-atoms] @a)
        [:div.canvas-container
         [:div.canvas-inner-container
          [:canvas (if-let [node @dom-node]
                     {:width (.-clientWidth node) :height (.-clientHeight node)})]]])})))
