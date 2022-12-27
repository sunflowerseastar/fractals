(ns fractals.utility)

;; equilateral triangle height
(defn eth [len] (/ (* (Math/sqrt 3) len) 2))

(defn get-centered-equilateral-triangle-canvas-positioning
  [canvas-width canvas-height canvas-padding-px]
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
  [canvas-width canvas-height canvas-padding-px]
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
