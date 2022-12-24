(ns sierpinski.turtle)

(defn draw-forward! [context x y step angle num-lines]
  (let [theta (/ (* Math/PI @angle) 180.0)
        new-x (+ @x (* @step (Math/cos theta)))
        new-y (- @y (* @step (Math/sin theta)))]
    (reset! x new-x)
    (reset! y new-y)
    (.lineTo context new-x new-y)
    (swap! num-lines inc)))

(defn draw-turtle! [context x y step angle num-lines grammar sentence]
  (doseq [letter (filter identity sentence)]
    (let [action (letter (:actions grammar))]
      (cond
        (= action :forward)
        (draw-forward! context x y step angle num-lines)
        (= action :left)
        (swap! angle #(mod (+ % 90) 360))
        (= action :right)
        (swap! angle #(mod (- % 90) 360))))))
