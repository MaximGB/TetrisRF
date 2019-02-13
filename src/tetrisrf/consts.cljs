(ns tetrisrf.consts)

(def default-timer-interval 1000)

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
                #{"Enter"}          :action-pause
                #{"Escape"}         :action-exit})

;; TODO: gestures
