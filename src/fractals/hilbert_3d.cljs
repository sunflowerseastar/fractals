(ns fractals.hilbert-3d
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.components :refer [inc-dec]]))

(def num-iterations (atom 3))

(defn i1 []
  [:iframe.iframe {:src "https://fractals.sunflowerseastar.com/hilbert-3d/#1"}])

(defn i2 []
  [:iframe.iframe {:src "https://fractals.sunflowerseastar.com/hilbert-3d/#2"}])

(defn i3 []
  [:iframe.iframe {:src "https://fractals.sunflowerseastar.com/hilbert-3d/#3"}])

(defn i4 []
  [:iframe.iframe {:src "https://fractals.sunflowerseastar.com/hilbert-3d/#4"}])

(defn hilbert-3d []
  [:<>
   [:div.controls [inc-dec num-iterations 4 1]]
   [:div.meta [:span "line segments: " (get [0 7 63 511 4095 32767] @num-iterations)]]
   ;; this is written in an odd way because otherwise mobile browsers and
   ;; desktop Safari won't refresh the iframe for just the hash change
   (cond
     (= @num-iterations 1) [i1]
     (= @num-iterations 2) [i2]
     (= @num-iterations 3) [i3]
     (= @num-iterations 4) [i4])])
