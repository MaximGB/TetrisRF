(ns tetrisrf.xstate.protocols)

(defprotocol MachineProtocol
  "XState based machine protocol.

   The protocol is used to obtain machine config/options unaltered by clj->js/js->clj transformations."
  (machine->config [this]
    "Returns machine config as a Clojure map")
  (machine->options [this]
    "Returns machine options as a Clojure map")
  (machine->xs-machine [this]
    "Returns XState machine instance"))


(defprotocol InterpreterProto
  "XState based interpreter protocol which uses re-frame facilities to send/recieve and handle events"
  (interpreter->machine ^Machine [this]
    "Returns currently interpreting machine.")
  (interpreter->state ^Object  [this]
    "Returns currently active state id.")
  (interpreter->started? ^boolean  [this]
    "Checks if interpreter has been started.")
  (interpreter->defer-events? ^boolean [this]
    "Checks if the interpreter is configured with defer-events? option.")
  (interpreter-start! ^InterpreterProto [this]
    "Starts machine interpretation. Registers re-frame event handlers to recieve events of the machine.")
  (interpreter-stop! ^InterpreterProto [this]
    "Stops machine interpretation. Un-registers re-frame event handlers registered at (start) call.")
  (interpreter-send-! ^InterpreterProto [this event]
    "Sends an event to the machine via re-frame facilities.
     `event` is [event & payload]"))


(defprotocol -InterpreterProto
  "Module private interpreter protocol, users should not implement or call it's methods."

  (-interpreter-transition! [this re-ctx]
    "Does the state chart transition.

     Returns re-frame context"))