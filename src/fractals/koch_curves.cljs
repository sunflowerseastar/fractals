(ns fractals.koch-curves
  (:require
   [fractals.l-system :refer [l-system]]))

(def koch-variations
  [{:name "rings"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 4}
   {:name "box"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 4
    :max-iterations 5}
   {:name "crystal"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 4
    :max-iterations 5}
   {:name "dragon"
    :variables #{:X :Y}
    :constants #{:+ :-}
    :start [:X]
    :rules {:X [:X :+ :Y :+]
            :Y [:- :X :- :Y]}
    :actions {:X :forward :Y :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 10
    :max-iterations 15}])

(defn koch-curves [window-width]
  (l-system window-width koch-variations))
