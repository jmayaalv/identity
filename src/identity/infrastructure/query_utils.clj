(ns identity.infrastructure.query-utils
  (:require [clojure.tools.logging :as log]))

(defn wait-for! [ready-fn? fn]
  @(future (loop [up-to-date (ready-fn?)]
             (if up-to-date
               (do
                 (fn))
               (do
                 (log/info "Test")
                 (Thread/sleep 300)
                 (recur (ready-fn?)))))))