(ns identity.infrastructure.common
  (:import (java.util UUID)))

(defn new-uuid
  "returns a new uuid"
  []
   (UUID/randomUUID))

