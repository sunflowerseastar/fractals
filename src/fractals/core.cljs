(ns ^:figwheel-hooks fractals.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [fractals.components :refer [hamburger nav-a]]
   [fractals.koch-curves :refer [koch-curves]]
   [fractals.more-koch-curves :refer [more-koch-curves]]
   [fractals.fass-curves :refer [fass-curves]]
   [fractals.more-fass-curves :refer [more-fass-curves]]
   [fractals.dragon-curve :refer [dragon-curve]]
   [fractals.sierpinski-curve :refer [sierpinski-curve]]
   [fractals.snowflake :refer [snowflake]]
   [fractals.quadratic-island :refer [quadratic-island]]
   [fractals.carpet :refer [sierpinski-carpet]]
   [fractals.triangle :refer [sierpinski-triangle]]))

(def fractals-options
  [{:name "more fass curves"
    :component more-fass-curves}
   {:name "fass curves"
    :component fass-curves}
   {:name "koch curves"
    :component koch-curves}
   {:name "more koch curves"
    :component more-koch-curves}
   {:name "koch snowflake"
    :component snowflake}
   {:name "quadratic koch island"
    :component quadratic-island}
   {:name "dragon curve"
    :component dragon-curve}
   {:name "sierpiński curve"
    :component sierpinski-curve}
   {:name "sierpiński carpet"
    :component sierpinski-carpet}
   {:name "sierpiński triangle"
    :component sierpinski-triangle}])
(def current-fractals-option (atom 0))

(def window-width (atom nil))

(def is-nav-active (atom false))

(defn main []
  (let
      [current-fractals-component
       (:component (get fractals-options @current-fractals-option))]
    [:<>
     [:div.header
      [:span (:name (get fractals-options @current-fractals-option))]]
     [current-fractals-component window-width]
     [:div.footer]
     [:div.nav-container {:class (when @is-nav-active "is-nav-active")}
      (into [:div.nav]
            (map-indexed
             (fn [i option]
               (let [is-active (= @current-fractals-option i)]
                 [nav-a
                  is-active
                  #(when-not is-active
                     (reset! current-fractals-option i)
                     (swap! is-nav-active not))
                  (:name option)]))
             fractals-options))]
     [hamburger #(swap! is-nav-active not) @is-nav-active]]))

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
