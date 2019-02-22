(ns tetrisrf.consts)

(def default-timer-interval 500)

(def first-level-score 10)

(def level-up-timer-coef 0.9)

(def level-up-score-coef 1.1)

(def color-white "rgb(255, 255, 255)")

(def color-red "rgb(255, 0, 0)")

(def color-green "rgb(0, 255, 0)")

(def color-blue "rgb(0, 0, 255)")

(def game-keys {#{"ArrowLeft"}      :action-left
                #{"ArrowRight"}     :action-right
                #{"ArrowUp"}        :action-rotate-cw
                #{"ArrowUp" :shift} :action-rotate-ccw
                #{"ArrowDown"}      :action-down
                #{" "}              :action-drop
                #{"Enter"}          :action-run-stop
                #{"Escape"}         :action-exit})

;; TODO: gestures

(def score-per-line {0 0
                     1 10
                     2 50
                     3 250
                     4 1250})
