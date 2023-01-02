(ns fractals.fass-curves
  (:require
   [fractals.l-system :refer [l-system]]))

(def fass-variations
  [
   {:name "quad gosper"
    :variables #{:X :Y}
    :constants #{:+ :-}
    :start [:- :Y]
    :rules {:X [:X :X :- :Y :- :Y :+ :X :+ :X :- :Y :- :Y :X :+
                :Y :+ :X :X :Y :- :X :+ :Y :+ :X :X :+
                :Y :- :X :Y :- :Y :- :X :+ :X :+ :Y :Y :-]
            :Y [:+ :X :X :- :Y :- :Y :+ :X :+ :X :Y :+ :X :-
                :Y :Y :- :X :- :Y :+ :X :Y :Y :- :X :-
                :Y :X :+ :X :+ :Y :- :Y :- :X :+ :X :+ :Y :Y]}
    :actions {:X :forward :Y :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 2
    :max-iterations 3}
   {:name "hex gosper"
    :variables #{:X :Y}
    :constants #{:+ :-}
    :start [:X]
    :rules {:X [:X :+ :Y :+ :+ :Y :- :X :- :- :X :X :- :Y :+]
            :Y [:- :X :+ :Y :Y :+ :+ :Y :+ :X :- :- :X :- :Y]}
    :actions {:X :forward :Y :forward :+ :left :- :right}
    :delta 60
    :initial-num-iterations 3
    :max-iterations 6}])

(defn fass-curves [window-width]
  (l-system window-width fass-variations))
