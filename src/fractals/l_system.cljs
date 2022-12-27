(ns fractals.l-system)

(defn is-variable? [grammar symbol]
  (contains? (:variables grammar) symbol))

(defn rewrite-sentence [grammar sentence]
  (flatten (map #(if (is-variable? grammar %) ((:rules grammar) %) %) sentence)))

(defn l-system [grammar n]
  (->> (:start grammar)
       (iterate #(rewrite-sentence grammar %))
       (take (inc n))
       last))
