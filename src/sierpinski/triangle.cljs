(ns sierpinski.triangle
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

;; utility
(def scale (atom 1.4))
(defn inner-width->num-rows [inner-width] (js/parseInt (/ (* .95 inner-width) @scale)))

;; these are written as a function so call sites can get the latest DOM reading
(def canvas-width-fn (fn [] (inner-width->num-rows (.-innerWidth js/window))))
;; magic number '40' matches .pre-canvas height in style.css
(def canvas-height-fn (fn [] (inner-width->num-rows (- (.-innerHeight js/window) 40))))

;; state
(def num-rows (atom (canvas-width-fn)))
(def num-cells (atom nil))
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
  "The original plot, from Rafik Naccache's Clojure Data Structures and Algorithms Cookbook."
  [size sierpinski-triangle-rows]
  (let [height (canvas-height-fn)]
    (loop [curr-y 0
           curr-starting-x (dec (/ size 2))
           acc []]
      (if (>= curr-y height)
        acc
        (let [new-plot-row
              (->> (get sierpinski-triangle-rows curr-y)
                   (map-indexed (fn [i x] (when (> x 0) [(+ (* i 2) curr-starting-x) (* curr-y 2)])))
                   (filter identity))]
          (recur (inc curr-y)
                 (dec curr-starting-x)
                 (concat acc new-plot-row)))))))

(defn plot-straight-along-the-top
  "The original plot, from Rafik Naccache's Clojure Data Structures and Algorithms Cookbook."
  [size sierpinski-triangle-rows]
  (for [x (range 0 size)
        y (range 0 (inc x))
        :when (= 1 (get (get sierpinski-triangle-rows x) y))]
    [x y]))

(def triangle-plot-types
  [{:name "vertical"
    :plot-fn plot-straight-along-the-bottom
    :plot-rectangle-size 2
    ;; this plot needs to know how tall it will be
    :size canvas-height-fn
    :scale 1.4}
   {:name "horizontal"
    :plot-fn plot-straight-along-the-top
    :plot-rectangle-size 1
    ;; this plot needs to know how wide it will be
    :size canvas-width-fn
    :scale 2.0}])

;; canvas
(defn draw!
  [canvas]
  (println "draw!")
  (let [
        context (.getContext canvas "2d")
        triangle-plot-type (get triangle-plot-types @active-triangle-plot-type)
        size ((:size triangle-plot-type))
        sierpinski-triangle-rows (generate-sierpinski-triangle size)
        new-num-cells (reduce + (range 1 (inc size)))
        ;; each 1 in the sierpinski-triangle-rows becomes a "plot"
        plots ((:plot-fn triangle-plot-type) (canvas-width-fn) sierpinski-triangle-rows)]
    ;; update meta
    (reset! num-rows (inner-width->num-rows (.-innerWidth js/window)))
    (reset! num-cells new-num-cells)
    ;; adjust scale
    (.scale context (:scale triangle-plot-type) (:scale triangle-plot-type))
    ;; plot triangle points
    (doseq [p plots]
      (.fillRect context (get p 0) (get p 1)
                 (:plot-rectangle-size triangle-plot-type)
                 (:plot-rectangle-size triangle-plot-type)))))

(def window-width (atom nil))

(defn render-canvas!
  []
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn []
        (let [canvas (.-firstChild @dom-node)]
          (draw! canvas)))

      :component-did-mount
      (fn [this]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn []
        @window-width ;; trigger re-render
        @active-triangle-plot-type
        [:div.canvas-container
         [:canvas (if-let [node @dom-node]
                    {:width (.-clientWidth node) :height (.-clientHeight node)})]])})))

(defn triangle-meta [rows cells]
  [:p [:span "num-rows: " rows " | cells: " cells]])

(defn sierpinski-triangle []
  [:<>
   [:div.pre-canvas
    (into [:div.chooser]
          (map-indexed
           (fn [i type]
             (let [is-active (= @active-triangle-plot-type i)]
               [:a {:on-click #(when-not is-active
                                 (reset! active-triangle-plot-type i)
                                 (reset! scale (get-in @triangle-plot-types [i :scale]))
                                 (println "hi"))
                    :class (when is-active "is-active")}
                (:name type)]))
           triangle-plot-types))
    [:div.meta [:span "num-rows: " @num-rows " | cells: " @num-cells]]]
   [render-canvas!]])
