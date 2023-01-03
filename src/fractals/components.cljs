(ns fractals.components
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(defn switcher-a
  "'a' as in 'an <a> tag anchor"
  [is-active on-click-fn & children]
  [:a {:on-click on-click-fn :class (when is-active "is-active")} children])

(defn switcher
  [koch-variations active-koch-variation]
  (into [:div.switcher]
        (map-indexed
         (fn [i type]
           (let [is-active (= @active-koch-variation i)]
             [switcher-a
              is-active
              #(when-not is-active (reset! active-koch-variation i))
              (:name type)]))
         koch-variations)))

;; There are two very similar inc-dec components because the first one operates
;; on a "simple" single-valued atom, while the second one does a swap!..update
;; on an atom that has a vector of iteration counts. The difference is subtle.
(defn inc-dec
  "Given a simple `num-iterations` atom and a max, return a +/- switcher."
  [num-iterations max-iterations]
  [:div.inc-dec
   [:a.box-button.box-button-left
    {:class (when (< @num-iterations 1) "inactive")
     :on-click #(when (pos? @num-iterations) (swap! num-iterations dec))} "-"]
   [:span @num-iterations]
   [:a.box-button.box-button-right
    {:class (when (>= @num-iterations max-iterations) "inactive")
     :on-click #(when (< @num-iterations max-iterations) (swap! num-iterations inc))} "+"]])

(defn inc-dec-with-vec-atom
  "Given a `num-iterations` atom that contains a vector of iterations, and a max,
  return a +/- switcher."
  [num-iterations koch-iterations max-iterations active-koch-variation]
  [:div.inc-dec
   [:a.box-button.box-button-left
    {:class (when (< num-iterations 1) "inactive")
     :on-click #(when (pos? num-iterations)
                  (swap! koch-iterations update @active-koch-variation dec))}
    "-"]
   [:span num-iterations]
   [:a.box-button.box-button-right
    {:class (when (>= num-iterations max-iterations) "inactive")
     :on-click #(when (< num-iterations max-iterations)
                  (swap! koch-iterations update @active-koch-variation inc))}
    "+"]])

(defn nav-a
  "'a' as in 'an <a> tag anchor"
  [is-active on-click-fn & children]
  [:a {:on-click on-click-fn :class (when is-active "is-active")} children])

(defn render-canvas!
  [draw-fn & redraw-atoms]
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn []
        (let [canvas (.-firstChild (.-firstChild @dom-node))]
          ;; (println "! draw")
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

(defn icon-qki
  "'qki' is quadratic Koch island"
  []
  [:svg {:viewBox [0 0 340 340] :width "100%" :height "100%"}
   [:path {:d "M137 38V33.5H132.5V38H137ZM137 71H132.5V75.5H137V71ZM170 38V42.5H174.5V38H170ZM170 5H174.5V0.5H170V5ZM137 5L137 9.5H137V5ZM104 5L104 0.499996L99.5 0.499995L99.5 4.99999L104 5ZM104 38L99.5 38V38H104ZM104 71V75.5H108.5V71H104ZM71 71V66.5H66.5L66.5 71L71 71ZM71 104L66.5 104L66.5 108.5H71V104ZM104 104L104 99.5H104V104ZM137 104H141.5V99.5L137 99.5L137 104ZM137 170V174.5H141.5V170H137ZM104 170H99.5V174.5H104V170ZM104 137H108.5V132.5H104V137ZM71 137V132.5H66.5V137H71ZM71 203V207.5H75.5V203H71ZM38 203H33.5V207.5H38V203ZM38 170H42.5V165.5H38V170ZM5 170V165.5H0.5V170H5ZM5 236H0.5V240.5H5V236ZM71 236H75.5V231.5H71V236ZM71 269H66.5V273.5H71V269ZM104 269V273.5H108.5V269H104ZM104 203V198.5H99.5V203H104ZM170 203H174.5V198.5H170V203ZM170 236V240.5H174.5V236H170ZM137 236V231.5H132.5V236H137ZM137 269H132.5V273.5H137V269ZM203 269H207.5V264.5H203V269ZM203 302V306.5H207.5V302H203ZM170 302V297.5H165.5V302H170ZM170 335H165.5V339.5H170V335ZM236 335V339.5H240.5V335H236ZM236 269V264.5H231.5V269H236ZM269 269V273.5H273.5V269H269ZM269 236H273.5V231.5H269V236ZM203 236H198.5V240.5H203V236ZM203 170V165.5H198.5V170H203ZM236 170H240.5V165.5H236V170ZM236 203H231.5V207.5H236V203ZM269 203V207.5H273.5V203H269ZM269 137V132.5H264.5V137H269ZM302 137H306.5V132.5H302V137ZM302 170H297.5V174.5H302V170ZM335 170V174.5H339.5V170H335ZM335 104H339.5V99.5H335V104ZM302 104L302 99.5L302 99.5L302 104ZM269 104H264.5V108.5L269 108.5L269 104ZM269 71H273.5V66.5H269V71ZM236 71V66.5H231.5V71H236ZM236 137V141.5H240.5V137H236ZM170 137H165.5V141.5H170V137ZM170 104V99.5H165.5V104H170ZM203 104V108.5H207.5V104H203ZM203 71H207.5V66.5H203V71ZM132.5 38V71H141.5V38H132.5ZM170 33.5H137V42.5H170V33.5ZM165.5 5V38H174.5V5H165.5ZM137 9.5H170V0.5H137V9.5ZM104 9.5L137 9.5L137 0.5L104 0.499996L104 9.5ZM108.5 38L108.5 5L99.5 4.99999L99.5 38L108.5 38ZM108.5 71V38H99.5V71H108.5ZM71 75.5H104V66.5H71V75.5ZM75.5 104L75.5 71L66.5 71L66.5 104L75.5 104ZM104 99.5H71V108.5H104V99.5ZM137 99.5L104 99.5L104 108.5L137 108.5L137 99.5ZM141.5 137V104H132.5V137H141.5ZM141.5 170V137H132.5V170H141.5ZM104 174.5H137V165.5H104V174.5ZM99.5 137V170H108.5V137H99.5ZM71 141.5H104V132.5H71V141.5ZM75.5 170V137H66.5V170H75.5ZM75.5 203V170H66.5V203H75.5ZM38 207.5H71V198.5H38V207.5ZM33.5 170V203H42.5V170H33.5ZM5 174.5H38V165.5H5V174.5ZM9.5 203V170H0.5V203H9.5ZM9.5 236V203H0.5V236H9.5ZM38 231.5H5V240.5H38V231.5ZM71 231.5H38V240.5H71V231.5ZM75.5 269V236H66.5V269H75.5ZM104 264.5H71V273.5H104V264.5ZM99.5 236V269H108.5V236H99.5ZM99.5 203V236H108.5V203H99.5ZM137 198.5H104V207.5H137V198.5ZM170 198.5H137V207.5H170V198.5ZM174.5 236V203H165.5V236H174.5ZM137 240.5H170V231.5H137V240.5ZM141.5 269V236H132.5V269H141.5ZM170 264.5H137V273.5H170V264.5ZM203 264.5H170V273.5H203V264.5ZM207.5 302V269H198.5V302H207.5ZM170 306.5H203V297.5H170V306.5ZM174.5 335V302H165.5V335H174.5ZM203 330.5H170V339.5H203V330.5ZM236 330.5H203V339.5H236V330.5ZM231.5 302V335H240.5V302H231.5ZM231.5 269V302H240.5V269H231.5ZM269 264.5H236V273.5H269V264.5ZM264.5 236V269H273.5V236H264.5ZM236 240.5H269V231.5H236V240.5ZM203 240.5H236V231.5H203V240.5ZM198.5 203V236H207.5V203H198.5ZM198.5 170V203H207.5V170H198.5ZM236 165.5H203V174.5H236V165.5ZM240.5 203V170H231.5V203H240.5ZM269 198.5H236V207.5H269V198.5ZM264.5 170V203H273.5V170H264.5ZM264.5 137V170H273.5V137H264.5ZM302 132.5H269V141.5H302V132.5ZM306.5 170V137H297.5V170H306.5ZM335 165.5H302V174.5H335V165.5ZM330.5 137V170H339.5V137H330.5ZM330.5 104V137H339.5V104H330.5ZM302 108.5H335V99.5H302V108.5ZM269 108.5L302 108.5L302 99.5L269 99.5L269 108.5ZM264.5 71V104H273.5V71H264.5ZM236 75.5H269V66.5H236V75.5ZM240.5 104V71H231.5V104H240.5ZM240.5 137V104H231.5V137H240.5ZM203 141.5H236V132.5H203V141.5ZM170 141.5H203V132.5H170V141.5ZM165.5 104V137H174.5V104H165.5ZM203 99.5H170V108.5H203V99.5ZM198.5 71V104H207.5V71H198.5ZM170 75.5H203V66.5H170V75.5ZM137 75.5H170V66.5H137V75.5Z"} ]])

(defn hamburger
  "Not actually a hamburger. Just the functional equivalent. It's a quadrilateral
  Koch island at 1 iteration."
  [on-click is-nav-active]
  [:div.hamburger
   {:on-click on-click
    :class (when is-nav-active "is-nav-active")}
   [icon-qki]])

(def logo-matrix [[0 1 1 0 1 1 1 0 1 1 0 1 1]
                  [0 1 0 0 1 1 0 0 1 0 0 1 0]
                  [1 1 0 0 1 0 0 1 1 0 1 1 0]])

(defn sfss-logo []
  (into [:div.logo]
        (map
         (fn [x] [:div {:class (when (pos? x) "logo-block")}])
         (flatten logo-matrix))))
