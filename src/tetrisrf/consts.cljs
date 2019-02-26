(ns tetrisrf.consts)

(def default-timer-interval 500)

(def first-level-score 10)

(def level-up-timer-coef 0.9)

(def level-up-score-coef 1.1)

(def field-cell-width 20)

(def field-cell-height 20)

(def color-white "#FFFFFF")

(def color-red "#FF0000")

(def color-green "#00FF00")

(def color-blue "#0000FF")

(def game-keys {#{"ArrowLeft"}      :action-left
                #{"ArrowRight"}     :action-right
                #{"ArrowUp"}        :action-rotate-cw
                #{"ArrowUp" :shift} :action-rotate-ccw
                #{"ArrowDown"}      :action-down
                #{" "}              :action-drop
                #{"Enter"}          :action-run-pause
                #{"Escape"}         :action-exit})

;; TODO: gestures

(def score-per-line {0 0
                     1 10
                     2 50
                     3 250
                     4 1250})
