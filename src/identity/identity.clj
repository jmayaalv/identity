(ns identity.identity
  (:require [rill.handler :refer [try-command]]
            [identity.application.command.tenant :as tenant-command]
            [identity.application.command.user :as user-command]))

(defonce store-atom (atom nil))                             ;;fixme add a real store

(defn provision-tenant!
  "Creates a new tenant"
  [tenant-id name description admin-first-name admin-last-name admin-email admin-username admin-password]
  (try-command @store-atom
               (tenant-command/provision! tenant-id name description)))

(defn activate-tenant!
  [tenant-id]
  (try-command @store-atom (tenant-command/activate! tenant-id)))

(defn deactivate-tenant!
  [tenant-id]
  (try-command @store-atom (tenant-command/deactivate! tenant-id)))


(defn setup!
  [event-store]
  (reset! store-atom event-store))