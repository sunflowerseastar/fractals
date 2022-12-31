(ns fractals.utility)

;; l-system

(defn is-variable? [grammar symbol]
  (contains? (:variables grammar) symbol))

(defn rewrite-sentence [grammar sentence]
  (flatten (map #(if (is-variable? grammar %) ((:rules grammar) %) %) sentence)))

(defn get-sentence
  "Given an l-system grammar and a number of iterations, return the sentence after rewriting the axiom and subsequent sentences n times."
  [grammar n]
  (->> (:start grammar)
       (iterate #(rewrite-sentence grammar %))
       (take (inc n))
       last))


;; canvas positioning

(def canvas-padding-px 20)

;; equilateral triangle height
(defn eth [len] (/ (* (Math/sqrt 3) len) 2))

(defn get-centered-equilateral-triangle-canvas-positioning
  [canvas-width canvas-height]
  (let [;; calculate the positioning of the triangle. And by "triangle," I mean
        ;; the resulting triangle-ish Sierpinski curve.
        canvas-width-padded (- canvas-width (* 2 canvas-padding-px))
        canvas-height-padded (- canvas-height (* 2 canvas-padding-px))

        ;; get the height of the resulting triangle if it was based on the
        ;; canvas width/height
        canvas-width-eth (eth canvas-width-padded)

        ;; decrement the canvas-width-padded (or canvas-height-padded, in the
        ;; edge case where it is taller) and keep checking the eth until you
        ;; find the max length (which is the width, since the equilateral
        ;; triangle's "flat" edge is on the bottom) whose height is smaller than
        ;; canvas-height-padded
        max-length-whose-eth-fits-canvas-height-padded
        (->> (iterate dec (max canvas-height-padded canvas-width-padded))
             (drop-while #(> (eth %) canvas-height-padded))
             first)

        ;; if we drew the triangle based on the canvas width, would it be taller
        ;; than the canvas?...
        is-triangle-taller-than-canvas-height (> canvas-width-eth canvas-height-padded)
        ;; ...and then determine what the triangle size will be based on
        triangle-length (if is-triangle-taller-than-canvas-height max-length-whose-eth-fits-canvas-height-padded canvas-width-padded)

        ;; from there, figure out the [bottom left] starting x/y position in
        ;; order to center the triangle in the canvas
        starting-x (if is-triangle-taller-than-canvas-height
                     (+ (- (/ canvas-width-padded 2) (/ max-length-whose-eth-fits-canvas-height-padded 2))
                        canvas-padding-px)
                     canvas-padding-px)
        starting-y (if is-triangle-taller-than-canvas-height
                     (+ canvas-height-padded canvas-padding-px)
                     (- canvas-height-padded (/ (- canvas-height-padded canvas-width-eth) 2)))]
    [starting-x starting-y triangle-length]))

(defn snowflake-height [width] (* (/ (eth width) 3) 4))

(defn get-centered-snowflake-canvas-positioning
  "'Snowflake' is specifically an equilateral triangle with the flat side on the
  bottom plus another 1/3 (of itself) height."
  [canvas-width canvas-height]
  (let [;; calculate the positioning of the triangle. And by "triangle," I mean
        ;; the resulting triangle-ish Sierpinski curve.
        canvas-width-padded (- canvas-width (* 2 canvas-padding-px))
        canvas-height-padded (- canvas-height (* 2 canvas-padding-px))

        ;; get the height of the resulting triangle if it was based on the
        ;; canvas width/height
        canvas-width-snowflake-height (snowflake-height canvas-width-padded)

        ;; decrement the canvas-width-padded (or canvas-height-padded, in the
        ;; edge case where it is taller) and keep checking the snowflake-height until you
        ;; find the max length (which is the width, since the equilateral
        ;; triangle's "flat" edge is on the bottom) whose height is smaller than
        ;; canvas-height-padded
        max-length-whose-snowflake-height-fits-canvas-height-padded
        (->> (iterate dec (max canvas-height-padded canvas-width-padded))
             (drop-while #(> (snowflake-height %) canvas-height-padded))
             first)

        ;; if we drew the triangle based on the canvas width, would it be taller
        ;; than the canvas?...
        is-triangle-taller-than-canvas-height (> canvas-width-snowflake-height canvas-height-padded)

        eth-height (if is-triangle-taller-than-canvas-height
                     (eth max-length-whose-snowflake-height-fits-canvas-height-padded)
                     (eth canvas-width-padded))

        ;; ...and then determine what the triangle size will be based on
        ;; triangle-length (if is-triangle-taller-than-canvas-height max-length-whose-snowflake-height-fits-canvas-height-padded canvas-width-padded)
        triangle-length (if is-triangle-taller-than-canvas-height
                          max-length-whose-snowflake-height-fits-canvas-height-padded
                          ;; (eth max-length-whose-snowflake-height-fits-canvas-height-padded)
                          ;; eth-height
                          canvas-width-padded)

        ;; from there, figure out the [bottom left] starting x/y position in
        ;; order to center the triangle in the canvas
        starting-x (if is-triangle-taller-than-canvas-height
                     (+ (- (/ canvas-width-padded 2) (/ max-length-whose-snowflake-height-fits-canvas-height-padded 2))
                        canvas-padding-px)
                     canvas-padding-px)
        starting-y (if is-triangle-taller-than-canvas-height
                     (+ (/ (- canvas-height (snowflake-height max-length-whose-snowflake-height-fits-canvas-height-padded)) 2) eth-height)
                     (+ (/ (- canvas-height (snowflake-height canvas-width-padded)) 2) (eth canvas-width-padded)))]
    [starting-x starting-y triangle-length]))

(defn get-centered-square-canvas-positioning
  [canvas-width canvas-height canvas-inner-square-size-fn inner-square-padding-fn]
  (let [;; context (.getContext canvas "2d")
        ;; calculate the positioning of the triangle. And by "triangle," I mean
        ;; the resulting triangle-ish Sierpinski curve.
        ;; canvas-padding-px 20
        canvas-width-padded (- canvas-width (* 2 canvas-padding-px))
        canvas-height-padded (- canvas-height (* 2 canvas-padding-px))

        short-edge (if (< canvas-width-padded canvas-height-padded) :width :height)
        short (if (= short-edge :width) canvas-width-padded canvas-height-padded)
        long (if (= short-edge :width) canvas-height-padded canvas-width-padded)

        ;; Since the quadratic koch island goes outside the bounds of the
        ;; original square on each iteration, it needs some extra padding. This
        ;; will give the original square (the drawing of the axiom before any
        ;; rewrites) enough padding to accommodate the iterations.
        canvas-inner-square-size (canvas-inner-square-size-fn short)
        inner-square-padding (inner-square-padding-fn canvas-inner-square-size)

        starting-x (if (= short-edge :height) (+ (/ (- long short) 2) inner-square-padding canvas-padding-px)
                       (+ canvas-padding-px inner-square-padding))
        starting-y (if (= short-edge :width) (- (+ (/ (- long short) 2)
                                                   canvas-width-padded
                                                   canvas-padding-px)
                                                inner-square-padding)
                       (- (+ canvas-height-padded canvas-padding-px) inner-square-padding))]
    [starting-x starting-y canvas-inner-square-size]))

(defn center-draw-points [canvas-width canvas-height draw-points]
  (let [min-x (first (apply min-key first draw-points))
        max-x (first (apply max-key first draw-points))
        min-y (second (apply min-key second draw-points))
        max-y (second (apply max-key second draw-points))

        draw-points-width (- max-x min-x)
        draw-points-height (- max-y min-y)

        canvas-width-padded (- canvas-width (* 2 canvas-padding-px))
        canvas-height-padded (- canvas-height (* 2 canvas-padding-px))

        draw-points-width-percentage-of-canvas-width (/ draw-points-width canvas-width-padded)
        draw-points-height-percentage-of-canvas-height (/ draw-points-height canvas-height-padded)

        short-edge (if (< draw-points-width-percentage-of-canvas-width
                          draw-points-height-percentage-of-canvas-height)
                     :height
                     :width)
        scale-multiplier (if (= short-edge :width)
                           (/ canvas-width-padded draw-points-width)
                           (/ canvas-height-padded draw-points-height))

        draw-points-width-scaled (* draw-points-width scale-multiplier)
        draw-points-height-scaled (* draw-points-height scale-multiplier)
        x-centering-offset (if (= short-edge :height)
                             (/ (- canvas-width-padded draw-points-width-scaled) 2)
                             canvas-padding-px)
        y-centering-offset (if (= short-edge :width)
                             (+ (/ (- canvas-height-padded draw-points-height-scaled) 2)
                                canvas-padding-px)
                             canvas-padding-px)]
    (map (fn [[x y]] [(-> x
                          (- min-x)
                          (* scale-multiplier)
                          (+ x-centering-offset))
                      (-> y
                          (- min-y)
                          (* scale-multiplier)
                          (+ y-centering-offset))])
         draw-points)))
