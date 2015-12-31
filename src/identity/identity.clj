(ns identity.identity
  (:require [rill.handler :refer [try-command]]
            [identity.application.command.tenant :as tenant-command]
            [identity.application.command.user :as user-command]))

(defonce store-atom (atom nil))                             ;;fixme add a real store

(defn provision-tenant!
  "Creates a new tenant"
  [tenant-id name description admin-first-name admin-last-name admin-email admin-username admin-password]
  (do (try-command @store-atom
                   (tenant-command/provision! tenant-id name description))
      (try-command @store-atom
                   (tenant-command/activate! tenant-id))
      (try-command @store-atom (user-command/register! tenant-id admin-first-name admin-last-name
                                                       admin-email admin-username admin-password))
      ))

(defn activate-tenant!
  [tenant-id]
  (try-command @store-atom (tenant-command/activate! tenant-id)))

(defn deactivate-tenant!
  [tenant-id]
  (try-command @store-atom (tenant-command/deactivate! tenant-id)))

(defn change-tenant-name!
  [tenant-id new-name]
  (try-command @store-atom (tenant-command/change-name! tenant-id new-name)))

(defn change-description!
  [tenant-id new-name]
  (try-command @store-atom (tenant-command/change-description! tenant-id new-name)))

(defn register-user! [tenant-id first-name last-name email username password]
  (try-command @store-atom (user-command/register! tenant-id  first-name last-name email username password)))

(defn change-password! [tenant-id username password new-password]
  (try-command @store-atom (user-command/change-password! tenant-id username password new-password)))

(defn change-user-name! [tenant-id username first-name last-name]
  (try-command @store-atom (user-command/change-name! tenant-id username first-name last-name)))

(defn enable-user! [tenant-id username]
  (try-command @store-atom (user-command/enable! tenant-id username)))

(defn disable-user! [tenant-id username]
  (try-command @store-atom (user-command/disable! tenant-id username)))

(defn setup!
  [event-store]
  (reset! store-atom event-store))



