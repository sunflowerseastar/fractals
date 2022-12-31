(ns fractals.koch-curves
  (:require
   [fractals.l-system :refer [l-system]]))

(def koch-variations
  [{:name "a"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :- :F :+ :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 4}
   {:name "b"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :F :- :F :- :F :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 5}
   {:name "c"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :+ :F :- :F :- :F :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 5}
   {:name "d"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :F :- :F :- :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 5}
   {:name "e"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :- :F :F :- :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 6}
   {:name "f"
    :variables #{:F}
    :constants #{:+ :-}
    :start [:F :- :F :- :F :- :F]
    :rules {:F [:F :- :F :+ :F :- :F :- :F]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 6}])

(defn koch-curves [window-width]
  (l-system window-width koch-variations))
