(ns sierpinski.components)

(defn switcher-a
  "'a' as in 'an <a> tag anchor"
  [is-active on-click-fn & children]
  [:a {:on-click on-click-fn :class (when is-active "is-active")} children])
