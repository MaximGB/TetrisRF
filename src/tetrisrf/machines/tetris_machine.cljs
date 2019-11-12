(ns tetrisrf.machines.tetris-machine
  (:require [tetrisrf.xstate.core :as xs]
            [tetrisrf.tetraminos :refer [tetraminos]]
            [tetrisrf.actions :refer [blend-tetramino
                                      calc-next-level-score
                                      calc-next-level-timer-interval
                                      calc-score
                                      can-act?
                                      field-complete-lines-count
                                      field-remove-complete-lines
                                      has-tetramino?
                                      move-down
                                      move-left
                                      move-right
                                      place-tetramino-centered
                                      rotate-90ccw
                                      rotate-90cw]]
            [tetrisrf.services.timer :as timer]
            [tetrisrf.db :refer [initial-db]]))


(xs/def-machine
  machine
  {:id :tetris-machine

   :initial :ready

   :states {:ready     {:entry :initialize-db
                        :on    {:action-run-pause  [{:cond    :can-place-next-tetramino?
                                                     :target  :playing}

                                                    {:target  :game-over}]}}

            :playing   {:entry [:init-timer :place-next-tetramino]
                        :exit  :dispose-timer

                        :on    {:tick             [{:cond    :can-move-down?
                                                    :actions :move-down}

                                                   {:cond    :can-place-next-tetramino?
                                                    :actions [:blend-tetramino :update-timer-interval :place-next-tetramino]}

                                                   {:actions :blend-tetramino
                                                    :target  :game-over}]

                                :action-right      {:cond    :can-move-right?
                                                    :actions :move-right}

                                :action-left       {:cond    :can-move-left?
                                                    :actions :move-left}

                                :action-down       {:cond    :can-move-down?
                                                    :actions [:move-down :re-send]}

                                :action-rotate-cw  {:cond    :can-rotate-cw?
                                                    :actions :rotate-cw}

                                :action-rotate-ccw {:cond    :can-rotate-ccw?
                                                    :actions :rotate-ccw}}}

            :game-over {:on    {:action-run-pause  {:target  :playing
                                                    :actions :initialize-db}}}}})


(xs/def-action-idb
  machine
  :initialize-db
  (fn [db]
    (-> db
        (merge initial-db)
        (assoc :next-tetramino (rand-nth tetraminos)))))


(xs/def-action-ifx
  machine
  :init-timer
  [xs/cofx-instance [::timer/timer ::timer/create nil]]
  (fn [cofx]
    (let [i (xs/cofx-instance cofx)
          db (:db cofx)
          timer (get-in cofx [::timer/timer ::timer/create])
          timer-id (gensym ::timer)]
      {:db (assoc db
                  :timer-id timer-id)

       ::timer/timer [::timer/register [timer-id timer]
                      ::timer/start [timer-id]
                      ::timer/set-interval [timer-id (:timer-interval initial-db)]
                      ::timer/set-tick-handler [timer-id #(xs/interpreter-send! i :tick)]]})))


(xs/def-action-ifx
  machine
  :dispose-timer
  (fn [cofx]
    (let [db (:db cofx)
          timer-id (get-in cofx [:db :timer-id])]
      {:db (dissoc db :timer-id)
       ::timer/timer [::timer/unregister [timer-id]]})))


(xs/def-action-idb
  machine
  :place-next-tetramino
  (fn [db]
    (let [next-tetramino (:next-tetramino db)
          next-next-tetramino (rand-nth tetraminos)
          next-tetramino-field (:next-tetramino-field db)
          field (:field db)]
      (assoc db
             :field (place-tetramino-centered field next-tetramino)
             :next-tetramino next-next-tetramino
             :next-tetramino-field (place-tetramino-centered next-tetramino-field next-next-tetramino :center-v true)))))


(xs/def-guard-idb
  machine
  :can-move-down?
  (fn [db]
    (can-act? (:field db) move-down)))


(xs/def-action-idb
  machine
  :move-down
  (fn [db]
    (update db :field move-down)))


(xs/def-guard-idb
  machine
  :can-rotate-cw?
  (fn [db]
    (can-act? (:field db) rotate-90cw)))


(xs/def-action-idb
  machine
  :rotate-cw
  (fn [db]
    (update db :field rotate-90cw)))


(xs/def-guard-idb
  machine
  :can-rotate-ccw?
  (fn [db]
    (can-act? (:field db) rotate-90ccw)))


(xs/def-action-idb
  machine
  :rotate-ccw
  (fn [db]
    (update db :field rotate-90ccw)))


(xs/def-guard-idb
  machine
  :can-move-right?
  (fn [db]
    (can-act? (:field db) move-right)))


(xs/def-action-idb
  machine
  :move-right
  (fn [db]
    (update db :field move-right)))


(xs/def-guard-idb
  machine
  :can-move-left?
  (fn [db]
    (can-act? (:field db) move-left)))


(xs/def-action-idb
  machine
  :move-left
  (fn [db]
    (update db :field move-left)))


(xs/def-guard-idb
  machine
  :can-place-next-tetramino?
  (fn [db]
    (let [field (:field db)
          field-blended (blend-tetramino field)
          next-tetramino (:next-tetramino db)]
      (can-act? field-blended #(place-tetramino-centered %1 next-tetramino)))))


(xs/def-action-idb
  machine
  :blend-tetramino
  (fn [db]
    (let [field (:field db)
          field-blended (blend-tetramino field)
          complete-lines-count (field-complete-lines-count field-blended)
          field-cleared (field-remove-complete-lines field-blended)
          score (calc-score (:score db) complete-lines-count)
          next-level-score (:next-level-score db)
          level (:level db)
          timer-interval (:timer-interval db)
          level-up (>= score next-level-score)]
      (assoc db
             :field field-cleared

             :score score

             :level (if level-up
                      (inc level)
                      level)

             :next-level-score (if level-up
                                 (calc-next-level-score score)
                                 next-level-score)

             :timer-interval (if level-up
                               (calc-next-level-timer-interval timer-interval)
                               timer-interval)))))


(xs/def-action-ifx
  machine
  :update-timer-interval
  (fn [cofx]
    {::timer/timer [::timer/set-interval [(get-in cofx [:db :timer-id]) (get-in cofx [:db :timer-interval])]]}))


(xs/def-action-fx
  machine
  :re-send
  [xs/cofx-instance]
  (fn [cofx event]
    {xs/fx-send (into [(xs/cofx-instance cofx)] event)}))
