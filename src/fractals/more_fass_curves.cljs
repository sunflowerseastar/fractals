(ns fractals.more-fass-curves
  (:require
   [fractals.l-system :refer [l-system]]))

(def fass-variations
  [{:name "Peano"
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
   {:name "Hilbert"
    :variables #{:L :R}
    :constants #{:F :+ :-}
    :start [:L]
    :rules {:L [:+ :R :F :- :L :F :L :- :F :R :+]
            :R [:- :L :F :+ :R :F :R :+ :F :L :-]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 4
    :max-iterations 8}
   {:name "Moore"
    :variables #{:L :R}
    :start [:L :F :L :+ :F :+ :L :F :L]
    :rules {:L [:- :R :F :+ :L :F :L :+ :F :R :-]
            :R [:+ :L :F :- :R :F :R :- :F :L :+]}
    :actions {:F :forward :+ :left :- :right}
    :delta 90
    :max-iterations 7}
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
    :max-iterations 4}])

(defn more-fass-curves [window-width]
  (l-system window-width fass-variations))
