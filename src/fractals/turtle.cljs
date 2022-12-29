(ns fractals.turtle)

(defn draw-forward! [context x y step angle num-lines]
  (let [theta (/ (* Math/PI @angle) 180.0)
        new-x (+ @x (* @step (Math/cos theta)))
        new-y (- @y (* @step (Math/sin theta)))]
    (reset! x new-x)
    (reset! y new-y)
    (.lineTo context new-x new-y)
    (swap! num-lines inc)))

(defn turtle-draw-to-canvas! [context x y step angle delta num-lines grammar sentence]
  (doseq [letter (filter identity sentence)]
    (let [action (letter (:actions grammar))]
      (cond
        (= action :forward)
        (draw-forward! context x y step angle num-lines)
        (= action :left)
        (swap! angle #(mod (+ % delta) 360))
        (= action :right)
        (swap! angle #(mod (- % delta) 360))))))

(defn draw-2 [context [x y]]
  ;; (println "!! draw-2" x y)
  (.lineTo context x y))

(defn point-forward [x y step angle]
  ;; (println "point-forward" x y step angle)
  (let [theta (/ (* Math/PI angle) 180.0)
        new-x (+ x (* step (Math/cos theta)))
        new-y (- y (* step (Math/sin theta)))]
    ;; (reset! x new-x)
    ;; (reset! y new-y)
    ;; (.lineTo context new-x new-y)
    ;; (swap! num-lines inc)
    ;; (println new-x new-y)
    [new-x new-y]
    ))

(defn bounds [xs]
  (let [min-x (first (apply min-key first xs))
        max-x (first (apply max-key first xs))
        min-y (second (apply min-key second xs))
        max-y (second (apply max-key second xs))]
    ;; TODO shift to positive (make sure no negatives)
    ;; calculate delta between x's and y's
    ;; calculate scale based on canvas and aspect ratios
    ;; use scale when drawing over draw-points
    [min-x max-x min-y max-y]))

(defn turtle-return-draw-points [angle delta grammar sentence]
  (loop [letters (filter identity sentence)
         angle @angle
         x 300
         y 300
         acc []]
    ;; (println "l:" letters "angle:" angle "acc:" acc)
    (if (empty? letters) acc
        (let [action ((first letters) (:actions grammar))]
          (cond (= action :forward)
                (let [[new-x new-y] (point-forward x y 20 angle)]
                  (recur (rest letters)
                         angle
                         new-x
                         new-y
                         (conj acc [new-x new-y])))
                (= action :left)
                (recur (rest letters)
                       (mod (+ angle delta) 360)
                       x y
                       acc)
                (= action :right)
                (recur (rest letters)
                       (mod (- angle delta) 360)
                       x y
                       acc))))))

(defn generate-and-center-draw-points [angle delta grammar sentence canvas-width canvas-height]
  (let [dps (turtle-return-draw-points angle delta grammar sentence)
        [min-x max-x min-y max-y] (bounds dps)

        dps-width (- max-x min-x)
        dps-height (- max-y min-y)

        per-x-to-canvas (/ dps-width canvas-width)
        per-y-to-canvas (/ dps-height canvas-height)

        short-edge (if (< per-x-to-canvas per-y-to-canvas) :height :width)
        scale-multiplier (if (= short-edge :width) (/ canvas-width dps-width) (/ canvas-height dps-height))]
    ;; (println "dps:" dps)
    ;; (println "bounds:" bounds)
    ;; (println "min/max x/y:" min-x max-x min-y max-y)
    ;; (println "per xy" per-x-to-canvas per-y-to-canvas)
    ;; (println "se, sm" short-edge scale-multiplier)

    (map
     (fn [[x y]] [(-> x (- min-x) (* scale-multiplier))
                  (-> y (- min-y) (* scale-multiplier))])
     dps)))
