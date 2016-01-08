(ns identity.projection.user.event-listener
  (:require [clojure.core.async :refer [<!! thread put!]]
          [identity.projection.user.event-handler :refer [handle-event]]))

(defn listen!
  "listen on event-channel"
  [model-atom event-channel]
  (thread
    (loop []
      (when-let [event (<!! event-channel)]
        (swap! model-atom handle-event event)
        (recur)))))

