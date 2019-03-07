(ns tetrisrf.consts)

(def default-timer-interval 500)

(def first-level-score 100)

(def level-up-timer-coef 0.9)

(def level-up-score-coef 1.1)

(def field-cell-width 20)

(def field-cell-height 20)

(def color-white [0xff 0xff 0xff])

(def color-red [0xcb 0x12 0x12])

(def color-green [0x0 0xff 0x0])

(def color-blue [0x52 0x4b 0x96])

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
