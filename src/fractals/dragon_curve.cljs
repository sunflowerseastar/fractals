(ns fractals.dragon-curve
  (:require
   [fractals.l-system :refer [l-system]]))

(def dragon-variations
  [{:name "a"
    :variables #{:X :Y}
    :constants #{:+ :-}
    :start [:X]
    :rules {:X [:X :+ :Y :+]
            :Y [:- :X :- :Y]}
    :actions {:X :forward :Y :forward :+ :left :- :right}
    :delta 90
    :initial-num-iterations 10
    :max-iterations 15}])

(defn dragon-curve [window-width]
  (l-system window-width dragon-variations))
