(ns tetrisrf.xstate.subscriptions
  (:require [re-frame.core :as rf]
            [tetrisrf.xstate.protocols :as protocols]))


(rf/reg-sub
 ::interpreter-db
 (fn [db [_ interpreter]]
   (let [path (protocols/interpreter->path interpreter)]
     (get-in db path))))


(defn reg-isub
  "Creates subscription to isolated by interpreter path part of the application database.

   The subscribe call should pass interpreter as the first item in the subscription query:

       (rf/subscribe [::my-sub interpreter other query parts])

   where ::my-sub should be registered with (reg-isub ::my-sub computation-fn) call."
  [id computation-fn]
  (rf/reg-sub
   id
   (fn [[_ interpreter]]
     (rf/subscribe [::interpreter-db interpreter]))
   computation-fn))
