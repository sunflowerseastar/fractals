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
    (.clearRect context 0 0 (.-width canvas) (.-height canvas))))

(defn render-ball!
  [canvas x y y-size]
  (let [context (.getContext canvas "2d")
        play-board-height (.-height canvas)
        ;; the ball height is the size of one square on the virtual grid we are considering the canvas to be
        ball-height (.floor js/Math (/ play-board-height y-size))
        ball-center-x (* (+ x 0.5) ball-height)
        ball-center-y (* (+ y 0.5) ball-height)
        ball-radius (/ ball-height 2)]
    ;; (println ball-height ball-center-x ball-center-y ball-radius)
    ;; (println play-board-height)
    (.beginPath context)
    ;; draw the circle
    (.arc context
          ball-center-x
          ball-center-y
          ball-radius 0
          (* 2 (.-PI js/Math)) false)
    (set! (.-fillStyle context) "orange")
    (.fill context)
    ;; fill it in
    (set! (.-lineWidth context) 5)
    (set! (.-strokeStyle context) "#003300")
    (.stroke context)))


;; canvas control and general DOM

(def window-width (reagent/atom nil))

(defn render-canvas!
  "Initiate and control a canvas rendering."
  []
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn [this]
        (let [canvas (.-firstChild @dom-node) ;; (.getElementById js/document "canvas-id")
              dummy-x-y [5 5]]
          (clear! canvas)
          (render-ball! canvas (get dummy-x-y 0) (get dummy-x-y 1) 20)))

      :component-did-mount
      (fn [this]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn []
        @window-width ;; Trigger re-render on window resizes
        [:div.canvas-container
         ;; reagent-render is called before the compoment mounts, so protect
         ;; against the null dom-node that occurs on the first render
         [:canvas (if-let [node @dom-node] {:width (.-clientWidth node) :height (.-clientHeight node)})]
         ]
        )})))


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
