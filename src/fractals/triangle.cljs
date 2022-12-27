(ns fractals.triangle
  (:require
   [reagent.core :as reagent :refer [atom]]
   [fractals.components :refer [render-canvas! switcher-a]]))

;; utility
(def scale 1)
(defn inner-width->num-rows [inner-width] (js/parseInt (/ (* .95 inner-width) scale)))

;; state
(def num-rows (atom nil))
(def num-squares (atom nil))
(def active-triangle-plot-type (atom 0))

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
  "Make an 'upright' triangle, with the point at the center top and the straight
  edge along the bottom."
  [width height sierpinski-triangle-rows]
  (loop [curr-y 0
         curr-starting-x (dec (/ width 2))
         acc []]
    (if (>= curr-y height)
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
  [width _ sierpinski-triangle-rows]
  (for [x (range 0 width)
        y (range 0 (inc x))
        :when (= 1 (get (get sierpinski-triangle-rows x) y))]
    [x y]))

(def triangle-plot-types
  [{:name "vertical"
    :plot-fn plot-straight-along-the-bottom
    :plot-rectangle-size 2
    ;; this plot needs to know how tall it will be
    :row-generation-direction :vertical}
   {:name "horizontal"
    :plot-fn plot-straight-along-the-top
    :plot-rectangle-size 1
    ;; this plot needs to know how wide it will be
    :row-generation-direction :horizontal}])

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        triangle-plot-type (get triangle-plot-types @active-triangle-plot-type)
        canvas-width (-> context .-canvas .-clientWidth)
        canvas-height (-> context .-canvas .-clientHeight)
        size (if (= (:row-generation-direction triangle-plot-type) :horizontal) canvas-width canvas-height)
        sierpinski-triangle-rows (generate-sierpinski-triangle size)
        ;; each 1 in the sierpinski-triangle-rows becomes a "plot"
        plots ((:plot-fn triangle-plot-type) canvas-width canvas-height sierpinski-triangle-rows)]

    (.setAttribute canvas "height" canvas-height)
    (.setAttribute canvas "width" canvas-width)

    ;; update controls
    (reset! num-rows (count sierpinski-triangle-rows))
    ;; adjust scale
    (.scale context scale scale)
    ;; plot triangle points
    (doseq [p plots]
      (.fillRect context (get p 0) (get p 1)
                 (:plot-rectangle-size triangle-plot-type)
                 (:plot-rectangle-size triangle-plot-type))
      (swap! num-squares inc))))

(defn sierpinski-triangle [window-width]
  [:<>
   [:div.controls
    (into [:div.switcher]
          (map-indexed
           (fn [i type]
             (let [is-active (= @active-triangle-plot-type i)]
               [switcher-a
                is-active
                #(when-not is-active (reset! active-triangle-plot-type i))
                (:name type)]))
           triangle-plot-types))]
   [:div.meta [:span "rows: " @num-rows " | squares drawn: " @num-squares]]
   [render-canvas! draw! window-width active-triangle-plot-type]])
