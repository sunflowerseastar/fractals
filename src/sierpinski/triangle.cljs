(ns triangle.sierpinski)

;; utility
(def scale 1.6)
(defn inner-width->num-rows [inner-width]
  (js/parseInt (/ (* .95 inner-width) scale)))

(def vw-95-percent-pixel-count (inner-width->num-rows (.-innerWidth js/window)))
;; 40 matches .pre-canvas height in style.css
(def canvas-height (inner-width->num-rows (- (.-innerHeight js/window) 40)))

;; state
(def num-rows (atom vw-95-percent-pixel-count))
;; (def num-rows (atom 50))
(def num-cells (atom nil))

;; triangle generation
(defn row->next-row
  "Given a yield fn and a row, generate the next row."
  [yield row]
  {:pre [(> (get row 0) 0)]} ;; we can only start from 1
  (let [row-count (count row)
        half-row (subvec row 0 (inc (double (/ row-count 2))))
        ;; put a 0 at the front since it's used in the computation
        padded-half-row (into [0] half-row)
        ;; partition into pairs and combine them with the yield fn
        next-row-first-half (vec (map (comp (partial apply yield) vec)
                                      (partition 2 1 padded-half-row)))
        ;; generate the mirrored second half so it can be added on
        next-row-second-half (vec (if (even? row-count)
                                    (-> next-row-first-half
                                        butlast
                                        reverse)
                                    (-> next-row-first-half
                                        reverse)))]
    (into next-row-first-half next-row-second-half)))

(defn pascal-rows
  "Given a yield fn and a desired number of rows, return a version of Pascal's
  triangle."
  [yield n]
  (loop [i 0
         acc []
         current-row [1]]
    (if (< i n)
      (recur (inc i)
             (conj acc current-row)
             (row->next-row yield current-row))
      acc)))

(defn sierpinski-triangle-yield
  "Instead of building Pascal's triangle, just keep track of whether this would be
  even or odd -- Sierpinski triangle."
  [n1 n2]
  (mod (+ n1 n2) 2))

(def generate-sierpinski-triangle (partial pascal-rows sierpinski-triangle-yield))
;; (generate-sierpinski-triangle 10)

(defn plot-straight-along-the-bottom
  "The original plot, from Rafik Naccache's Clojure Data Structures and Algorithms Cookbook."
  [size sierpinski-triangle-rows]
  (loop [curr-y 0
         curr-starting-x (dec (/ size 2))
         acc []]
    (if (>= curr-y canvas-height)
      acc
      (let [new-plot-row
            (->> (get sierpinski-triangle-rows curr-y)
                 (map-indexed (fn [i x] (when (> x 0) [(+ (* i 2) curr-starting-x) (* curr-y 2)])))
                 (filter identity))]
        (recur (inc curr-y)
               (dec curr-starting-x)
               (concat acc new-plot-row))))))

(defn plot-straight-along-the-top
  "The original plot, from Rafik Naccache's Clojure Data Structures and Algorithms Cookbook."
  [size sierpinski-triangle-rows]
  (for [x (range 0 size)
        y (range 0 (inc x))
        :when (= 1 (get (get sierpinski-triangle-rows x) y))]
    [x y]))

;; canvas
(defn draw!
  [canvas]
  (println "draw!")
  (let [
        size (inner-width->num-rows (.-innerWidth js/window))
        ;; size vw-95-percent-pixel-count
        ;; size @num-rows
        context (.getContext canvas "2d")
        sierpinski-triangle-rows (generate-sierpinski-triangle canvas-height)
        new-num-cells (reduce + (range 1 (inc size)))
        ;; each 1 in the sierpinski-triangle-rows becomes a "plot"
        ;; plots (plot-straight-along-the-top size sierpinski-triangle-rows)
        plots (plot-straight-along-the-bottom size sierpinski-triangle-rows)
        ]
    (println (count plots))

    ;; update meta
    (reset! num-rows (inner-width->num-rows (.-innerWidth js/window)))
    (reset! num-cells new-num-cells)
    ;; adjust scale
    (.scale context scale scale)
    ;; plot triangle points
    (doseq [p plots]
      (.fillRect context (get p 0) (get p 1) 2 2)
      )))

;; upper-right-hand corner "meta" information
(defn triangle-meta []
  [:p [:span "num-rows: " @num-rows " | cells: " @num-cells]])
