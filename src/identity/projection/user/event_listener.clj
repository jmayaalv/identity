(ns identity.projection.user.event-listener
  (:require [clojure.core.async :refer [<!! thread]]
          [clojure.tools.logging :as log]
          [identity.projection.user.event-handler :refer [handle-event]]))

(defn listen!
  "listen on event-channel"
  [model-atom event-channel]
  (thread
    (loop []
      (when-let [event (<!! event-channel)]
        ;(cond (= (:rill.message/type event) :identity.domain.model.user/Registered) (log/info event))
        (swap! model-atom handle-event event)
        (recur)))))

