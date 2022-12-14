(ns ^:figwheel-hooks pascals-triangle-fractals.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

;; [[1 0 0 0 0 0 0]
;;  [1 1 0 0 0 0 0]
;;  [1 1 1 0 0 0 0]
;;  [1 1 1 1 0 0 0]
;;  [1 1 1 1 1 0 0]
;;  [1 1 1 1 1 1 0]
;;  [1 1 1 1 1 1 1]]

;; [[1]
;;  [1 1]
;;  [1 1 1]
;;  [1 1 1 1]
;;  [1 1 1 1 1]
;;  [1 1 1 1 1 1]
;;  [1 1 1 1 1 1 1]]

;; [[1]
;;  [1 1]
;;  [1 2 1]
;;  [1 3 3 1]
;;  [1 4 6 4 1]
;;  [1 5 10 10 5 1]
;;  [1 6 15 20 15 6 1]]


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

;; The "yield" is the function that combines each element's upstairs neighbors. Regular Pascal is just summing the nubmers, and this computes the powers of binomials. Sierpinski continually computes the even/odd-ness of the sums (and note that even/odd-ness works recursively).
(def pascal-yield +)

(defn sierpinski-yield
  "Instead of building Pascal's triangle, just keep track of whether this would be
  even or odd -- Sierpinski triangle."
  [n1 n2]
  (mod (+ n1 n2) 2))

(def generate-sierpinski (partial pascal-rows sierpinski-yield))
(def generate-pascal (partial pascal-rows pascal-yield))

(generate-sierpinski 10)
(generate-pascal 10)


;; canvas
;;
(defn clear!
  [canvas]
  (let [context (.getContext canvas "2d")]
    (.scale context 1 1)
    (.clearRect context 0 0 (.-width canvas) (.-height canvas))))

;; (defn draw-x [size]
;;   (let [img (BufferedImage. size size BufferedImage/TYPE_INT_ARGB)
;;         plot-rows (generate-sierpinski size)
;;         plots (for [x (range 0 size)
;;                     y (range 0 x)]
;;                 (if (= 1 (get (get plot-rows x) y)) [x y]))
;;         gfx (.getGraphics img)]
;;     (.setColor gfx Color/WHITE)
;;     (.fillRect gfx 0 0 size size)
;;     (.setColor gfx Color/BLACK)
;;     (doseq [p (filter (comp not nil?) plots)]
;;       (.drawLine gfx
;;                  (get p 0)
;;                  (get p 1)
;;                  (get p 0)
;;                  (get p 1)))
;;     (ImageIO/write img "png" (File. "/result-asdf.png"))))


(defn draw!
  [canvas size]
  (let [context (.getContext canvas "2d")
        sierpinski-rows (generate-sierpinski size)
        ;; each 1 in the sierpinski-rows is a "plot"
        plots (for [x (range 0 size)
                    y (range 0 (inc x))
                    :when (= 1 (get (get sierpinski-rows x) y))]
                [x y])]
    ;; (println size)
    ;; (println sierpinski-rows)
    ;; (println plots)
    ;; (println px)
    (.scale context 10 10)
    (doseq [p plots]
      (.fillRect context (get p 0) (get p 1)
                 1 1))))

;; canvas control and general DOM

(def window-width (reagent/atom nil))

(defn render-canvas!
  "Initiate and control a canvas rendering."
  []
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn [this]
        (let [canvas (.-firstChild @dom-node)]
          ;; (clear! canvas)
          (draw! canvas 60)))

      :component-did-mount
      (fn [this]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn []
        @window-width ;; Trigger re-render on window resizes
        [:div.canvas-container
         ;; reagent-render is called before the compoment mounts, so protect
         ;; against the null dom-node that occurs on the first render
         [:canvas (if-let [node @dom-node]
                    {:width (.-clientWidth node) :height (.-clientHeight node)})]])})))


(defn main []
  [:<>
   [:div.pre-canvas [:p "pre"]]
   [:div.canvas-outer-container [render-canvas!]]
   [:div.post-canvas [:p "post"]]])


;; reagent setup + resizing

(defn on-window-resize [evt]
  (reset! window-width (.-innerWidth js/window)))

(defn mount [el]
  (rdom/render [main] el)
  (.addEventListener js/window "resize" on-window-resize))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

;; conditionally start your application based on the presence of an "app" element
;; this is particularly helpful for testing this ns without launching the app
(mount-app-element)

;; specify reload hook with ^:after-load metadata
(defn ^:after-load on-reload []
  (mount-app-element)
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
