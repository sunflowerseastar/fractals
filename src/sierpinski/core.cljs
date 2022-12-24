(ns ^:figwheel-hooks sierpinski.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [sierpinski.components :refer [switcher-a]]
   [sierpinski.curve :refer [sierpinski-curve]]
   [sierpinski.koch :refer [sierpinski-koch]]
   [sierpinski.carpet :refer [sierpinski-carpet]]
   [sierpinski.triangle :refer [sierpinski-triangle]]))

(def sierpinski-options
  [{:name "koch"
    :component sierpinski-koch}
   {:name "curve"
    :component sierpinski-curve}
   {:name "carpet"
    :component sierpinski-carpet}
   {:name "triangle"
    :component sierpinski-triangle}])
(def current-sierpinski-option (atom 0))

(def window-width (atom nil))

(defn main []
  (let
      [current-sierpinski-component
       (:component (get sierpinski-options @current-sierpinski-option))]
    [:<>
     [:div.pre-canvas
      (into [:div.switcher]
            (map-indexed
             (fn [i option]
               (let [is-active (= @current-sierpinski-option i)]
                 [switcher-a
                  is-active
                  #(when-not is-active (reset! current-sierpinski-option i))
                  (:name option)]))
             sierpinski-options))]
     [current-sierpinski-component window-width]
     [:div.post-canvas]]))

(defn on-window-resize [evt]
  (reset! window-width (.-innerWidth js/window)))

(defn mount [el]
  (rdom/render [main] el)
  (.addEventListener js/window "resize" on-window-resize))

(defn mount-app-element []
  (when-let [el (gdom/getElement "app")]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^:after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
