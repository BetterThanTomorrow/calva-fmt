(ns calva.fmt.ontype-formatting-provider
  (:require
   ["vscode" :as vscode]
   [cljfmt.core :as cljfmt]))


(defonce state (atom {}))


(defn update-statusbar! [item, should-adjust-indent?]
  (aset item "text" (str "AAI: " (if should-adjust-indent?
                                   "on"
                                   "off")))
  (aset item "command" "calva.fmt.toggleAutoAdjustIndent")
  (aset item "tooltop" (str (if should-adjust-indent?
                              "Disable"
                              "ENable")
                            " auto adjustment of indents for new lines")))


(defn- should-adjust-indent-on-newline? [configuration]
  (.get configuration "autoAdjustIndentOnNewLines"))


(defn- set-should-adjust-indent-on-newline? [configuration do-it?]
  (.update configuration "autoAdjustIndentOnNewLines" do-it?))


(defn figure-out-indent [text current-position]
  14)


(defn init []
  (let [configuration         (vscode/workspace.getConfiguration "calva.fmt")
        should-adjust-indent? (should-adjust-indent-on-newline? configuration)
        statusbar-item        (.createStatusBarItem vscode/window (.-Left vscode/StatusBarAlignment))]
    (swap! state assoc :statusbar-item statusbar-item)
    (update-statusbar! statusbar-item should-adjust-indent?)
    (.show statusbar-item)))


(defn toggleAutoAdjustIndentCommand []
  (let [configuration (vscode/workspace.getConfiguration "calva.fmt")
        should-adjust-indent? (should-adjust-indent-on-newline? configuration)
        should-adjust-indent? (not should-adjust-indent?)]
    (set-should-adjust-indent-on-newline? configuration should-adjust-indent?)
    (when-let [statusbar-item (:statusbar-item @state)]
      (update-statusbar! statusbar-item should-adjust-indent?))))


(deftype OnTypeEditProvider []
  Object
  (provideOnTypeFormattingEdits [document, position, ch, options]
    (assert false position)
    (let [configuration (vscode/workspace.getConfiguration "calva.fmt")
          should-adjust-indent? (should-adjust-indent-on-newline? configuration)]
      (if should-adjust-indent?
        (let [range-up-to-here (vscode/Range. (vscode/Position. 0 0) position)
              text (.getText document)
              indent (figure-out-indent text position)
              start-position (.with (.-line position) 0)
              end-position position]
          (if-not (= (.-character end-position) indent)
            (if (> (.character end-position) indent)
              #js [(.delete vscode/TextEdit (vscode.Range. (.with end-position (.-line end-position) indent) end-position))]
              #js [(.insert vscode/TextEdit start-position, (apply str (repeat (- indent (.character end-position)))))])
            nil))
        nil))))