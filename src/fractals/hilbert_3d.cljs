(ns fractals.hilbert-3d
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.components :refer [inc-dec]]))

(def num-iterations (atom 3))

(defn hilbert-3d []
  [:<>
   [:div.controls [inc-dec num-iterations 4 1]]
   [:div.meta [:span "line segments: " (get [0 7 63 511 4095 32767] @num-iterations)]]
   [:iframe.iframe {:src (str "https://fractals.sunflowerseastar.com/hilbert-3d#" @num-iterations)}]])
