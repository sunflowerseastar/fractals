(ns fractals.more-fass-curves
  (:require
   [fractals.l-system :refer [l-system]]))

(def fass-variations
  [{:name "Hilbert"
    :variables #{:L :R}
    :constants #{:F :+ :-}
    :start [:L]
    :rules {:L [:+ :R :F :- :L :F :L :- :F :R :+]
            :R [:- :L :F :+ :R :F :R :+ :F :L :-]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 4
    :max-iterations 8}
   {:name "Peano"
    :variables #{:L :R}
    :constants #{:F :+ :-}
    :start [:L]
    :rules {:L [:L :+ :F :+ :R :- :F :- :L :+ :F :+ :R :- :F :- :L :- :F :- :R :+ :F :+ :L :- :F :- :R :- :F :- :L :+ :F :+ :R :- :F :- :L :- :F :- :R :- :F :-
                :L :+ :F :+ :R :+ :F :+ :L :+ :F :+
                :R :- :F :- :L :+ :F :+ :R :+ :F :+ ;; :L :- :R :- :F :+ :F :+ :L :+ :F :+  <- error in tABoP (two F's in a row)
                :L :- :F :- :R :+ :F :+ :L :+ :F :+ :R :- :F :- :L :+ :F :+ :R :- :F :- :L]
            :R [:R :- :F :- :L :+ :F :+ :R :- :F :- :L :+ :F :+ :R :+ :F :+ :L :- :F :- :R :+ :F :+ :L :+ :F :+ :R :- :F :- :L :+ :F :+ :R :+ :F :+ :L :+ :F :+
                :R :- :F :- :L :- :F :- :R :- :F :- :L :+ :F :+ :R :- :F :- :L :- :F :- :R :+ :F :+ :L :- :F :- :R :- :F :- :L :+ :F :+ :R :- :F :- :L :+ :F :+ :R]}
    :actions {:F :forward :+ :left :- :right}
    :delta 45
    :initial-num-iterations 2
    :max-iterations 3}
   {:name "3x3"
    :variables #{:L :R}
    :start [:- :L]
    :rules {:L [:L :F :+ :R :F :R :+ :F :L :- :F :- :L :F :L :F :L :- :F :R :F :R :+]
            :R [:- :L :F :L :F :+ :R :F :R :F :R :+ :F :+ :R :F :- :L :F :L :- :F :R]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 3
    :max-iterations 5}
   {:name "4x4"
    :variables #{:L :R}
    :start [:- :L]
    :rules {:L [:L :F :L :F :+ :R :F :R :+ :F :L :F :L :- :F :R :F :- :L :F :L :-
                :F :R :+ :F :+ :R :F :- :L :F :L :- :F :R :F :R :F :R :+]
            :R [:- :L :F :L :F :L :F :+ :R :F :R :+ :F :L :- :F :- :L :F :+ :R :F :R :+
                :F :L :F :+ :R :F :R :F :- :L :F :L :- :F :R :F :R]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 2
    :max-iterations 4}
   {:name "7x7"
    :variables #{:X :Z}
    :constants #{:+ :-}
    :start [:X]
    :rules {:X [:X :X :X :- :Z :Z :- :Z :- :X :+ :Z :+ :X :X :+ :Z :- :X :Z :- :Z :- :X :+ :X :+ :Z :Z :+ :X :X :X :+ :Z :- :X :Z :- :Z :- :X :+ :X :+ :Z :Z :+ :X :X :X :+ :Z :X :Z :- :Z :- :X :+ :Z :X :- :Z :Z :- :X :- :Z :+ :X :+ :X :X :+ :Z :Z :Z :-]
            :Z [:+ :X :X :X :- :Z :Z :- :Z :- :X :+ :Z :+ :X :X :+ :Z :X :- :Z :+ :X :+ :X :Z :X :- :Z :Z :Z :- :X :X :- :Z :- :Z :+ :X :+ :X :Z :+ :X :- :Z :Z :Z :- :X :X :- :Z :- :Z :+ :X :+ :X :Z :+ :X :- :Z :Z :- :X :- :Z :+ :X :+ :X :X :+ :Z :Z :Z]}
    :actions {:X :forward :Z :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 2
    :max-iterations 3}
   {:name "9x9"
    :variables #{:L :R}
    :constants #{:+ :-}
    :start [:L]
    :rules {:L [:R :R :- :L :- :L :+ :R :+ :R :L :+ :R :- :L :R :+ :L :- :R :- :L :+ :R :L :L :- :R :- :L :R :L :R :L :+ :R :- :L :R :L :R :+ :L :+ :R :R :L :R :L :+ :R :- :L :R :L :- :L :- :R :L :+ :R :- :L :R :L :R :L :+ :R :+ :R :L :R :L :R :L :+ :R :- :L :L :- :R :- :L :+ :R :L :L :- :R :- :L :R :+ :R :+ :L :- :L :- :R :+ :R :+ :L :L :- :R :- :L :R :+ :R :+ :L :- :L :- :R :+ :R :+ :L :L :+]
            :R [:- :R :R :- :L :- :L :+ :R :+ :R :- :L :- :L :R :+ :L :+ :R :R :- :L :- :L :+ :R :+ :R :- :L :- :L :R :+ :L :+ :R :R :L :- :R :+ :L :+ :R :R :+ :L :- :R :L :R :L :R :L :- :L :- :R :L :R :L :R :+ :L :- :R :L :+ :R :+ :R :L :R :+ :L :- :R :L :R :L :L :- :R :- :L :R :L :R :+ :L :- :R :L :R :L :R :+ :L :+ :R :R :L :- :R :+ :L :+ :R :- :L :R :+ :L :- :R :L :- :L :- :R :+ :R :+ :L :L]}
    :actions {:L :forward :R :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 1
    :max-iterations 3}])

(defn more-fass-curves [window-width]
  (l-system window-width fass-variations))
