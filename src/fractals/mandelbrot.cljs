(ns fractals.mandelbrot
  (:require
   [fractals.components :refer [switcher]]
   [reagent.core :refer [atom]]))

(def active-preset-index (atom 0))
(def presets
  [{:name "a"
    :iterations 250
    :zoom 260
    :offsetx 0
    :offsety 0
    :panx -140
    :pany 0}
   {:name "b"
    :iterations 1000
    :zoom 26214400
    :offsetx -377
    :offsety -377
    :panx 6908792
    :pany 66576}
   {:name "c"
    :iterations 500
    :zoom 3750
    :offsetx -377
    :offsety -377
    :panx -2700
    :pany 294
    :colorscale "pow"
    :reverse true}
   {:name "d"
    :iterations 2000
    :zoom 6553600
    :offsetx -377
    :offsety -377
    :panx 2556691
    :pany -1758616
    :interpolation "rainbow"}
   {:name "e"
    :iterations 2400
    :zoom 419392000
    :offsetx -377
    :offsety -377
    :panx 162175151
    :pany -112064303
    :interpolation "rainbow"
    :reverse true}])

(defn mandelbrot []
  (let [preset (get presets @active-preset-index)
        query-params (str
                      "?iterations=" (:iterations preset)
                      "&zoom=" (:zoom preset)
                      "&offsetx=" (:offsetx preset)
                      "&offsety=" (:offsety preset)
                      "&panx=" (:panx preset)
                      "&pany=" (:pany preset)
                      "&colorscale=" (:colorscale preset)
                      "&interpolation=" (:interpolation preset)
                      "&reverse=" (:reverse preset))]
    [:<> [:div.controls
          [switcher presets active-preset-index]]
     [:iframe.iframe {:src (str "https://fractals.sunflowerseastar.com/mandelbrot-set/" query-params)}]]))
