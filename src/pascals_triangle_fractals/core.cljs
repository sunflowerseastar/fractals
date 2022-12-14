(ns ^:figwheel-hooks pascals-triangle-fractals.core
  (:require
   [goog.dom :as gdom]
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(println "This text is printed from src/pascals_triangle_fractals/core.cljs. Go ahead and edit it and see reloading in action.")

(defn multiply [a b] (* a b))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:text "Hello world!"}))

(defn get-app-element []
  (gdom/getElement "app"))

(defn hello-world []
  [:div
   [:h1 (:text @app-state)]
   [:h3 "Edit this in src/pascals_triangle_fractals/core.cljs and watch it change!"]])


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






(defn mount [el]
  (rdom/render [hello-world] el))

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
