(ns ^:figwheel-hooks fractals.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [fractals.components :refer [switcher-a]]
   [fractals.curve :refer [sierpinski-curve]]
   [fractals.snowflake :refer [snowflake]]
   [fractals.quadratic-island :refer [quadratic-island]]
   [fractals.carpet :refer [sierpinski-carpet]]
   [fractals.triangle :refer [sierpinski-triangle]]))

(def fractals-options
  [{:name "snowflake"
    :component snowflake}
   {:name "quadratic-island"
    :component quadratic-island}
   {:name "curve"
    :component sierpinski-curve}
   {:name "carpet"
    :component sierpinski-carpet}
   {:name "triangle"
    :component sierpinski-triangle}])
(def current-fractals-option (atom 0))

(def window-width (atom nil))

(defn main []
  (let
      [current-fractals-component
       (:component (get fractals-options @current-fractals-option))]
    [:<>
     [:div.header
      (into [:div.switcher]
            (map-indexed
             (fn [i option]
               (let [is-active (= @current-fractals-option i)]
                 [switcher-a
                  is-active
                  #(when-not is-active (reset! current-fractals-option i))
                  (:name option)]))
             fractals-options))]
     [current-fractals-component window-width]
     [:div.footer]]))

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
