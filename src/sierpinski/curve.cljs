;; https://nakkaya.com/2010/01/26/lindenmayer-system-in-clojure/
;; https://rosettacode.org/wiki/Sierpinski_curve#C++
;; https://en.wikipedia.org/wiki/Sierpi%C5%84ski_curve

(ns sierpinski.curve
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]))

(def x (atom 200))
(def y (atom 200))
(def angle (atom 0))

(defn is-variable? [grammar symbol]
  (contains? (:variables grammar) symbol))

(defn rewrite-sentence [grammar sentence]
  (reduce
   (fn [acc x]
   (concat acc (if (is-variable? grammar x) (x (:rules grammar)) [x])))
   []
   sentence))

(defn l-system [grammar n]
  (->> (:start grammar)
       (iterate #(rewrite-sentence grammar %))
       (take (inc n))
       last))

;; Alphabet: X, Y
;; Constants: F, +, −
;; Axiom: XF
;; Production rules:
;; X → YF + XF + Y
;; Y → XF − YF − X

(def sierpinski-curve-grammar
  {:variables #{:X :Y}
   :constants #{:F :+ :-}
   :start [:F :X]
   :rules {:X [:Y :F :+ :X :F :+ :Y]
           :Y [:X :F :- :Y :F :- :X]}
   :actions {:F :forward :+ :left :- :right}})

(defn draw-forward [context]
  ;; (println "!! DRAW" @x @y @angle)
  (let [theta (/ (* Math/PI @angle) 180.0)
        new-x (+ @x (* 10 (Math/cos theta)))
        new-y (- @y (* 10 (Math/sin theta)))]
    ;; (println new-x new-y)
    (reset! x new-x)
    (reset! y new-y)
    (.lineTo context new-x new-y)))

(defn draw-turtle [context grammar sentence]
  ;; (println "draw-turtle: " sentence)
  (doseq [letter sentence]
    (let [action (letter (:actions grammar))]
      ;; (println "letter:" letter "action:" action "@angle:" @angle)
      (cond
        (= action :forward)
        (draw-forward context)
        (= action :left)
        (swap! angle #(mod (- % 60) 360))
        (= action :right)
        (swap! angle #(mod (+ % 60) 360))
        :else (println "else")))))

;; canvas
(defn draw!
  [canvas]
  (let [context (.getContext canvas "2d")
        ;; calculate the max-area centered square of the canvas
        canvas-width (-> context .-canvas .-clientWidth)
        canvas-height (-> context .-canvas .-clientHeight)
        sc-1 (l-system sierpinski-curve-grammar 1)]
    (.setAttribute canvas "height" canvas-height)
    (.setAttribute canvas "width" canvas-width)

    (draw-turtle context sierpinski-curve-grammar sc-1)
    (draw-forward context)
    (.stroke context)))

(defn render-canvas!
  [window-width]
  (let [dom-node (reagent/atom nil)]
    (reagent/create-class
     {:component-did-update
      (fn []
        (let [canvas (.-firstChild (.-firstChild @dom-node))]
          (draw! canvas)))

      :component-did-mount
      (fn [this]
        (reset! dom-node (rdom/dom-node this)))

      :reagent-render
      (fn []
        @window-width ;; trigger re-render
        [:div.canvas-container
         [:div.canvas-inner-container
          [:canvas (if-let [node @dom-node]
                     {:width (.-clientWidth node) :height (.-clientHeight node)})]]])})))

(defn sierpinski-curve [window-width]
  [:<>
   [:div.controls-post-canvas-left [:span "post-canvas-left"]]
   [:div.controls-post-canvas-right [:span "post-canvas-right"]]
   [render-canvas! window-width]])
