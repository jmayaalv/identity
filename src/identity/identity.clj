(ns identity.identity
  (:require [rill.handler :refer [try-command]]
            [identity.application.command.tenant :as tenant-command]
            [identity.application.command.user :as user-command]))

(defonce store-atom (atom nil))                             ;;fixme add a real store

(defn provision-tenant!
  "Creates a new tenant"
  [tenant-id name description admin-id admin-first-name admin-last-name admin-email admin-username admin-password]
  (do (try-command @store-atom
                   (tenant-command/provision! tenant-id name description))
      (try-command @store-atom
                   (tenant-command/activate! tenant-id))
      (try-command @store-atom (user-command/register! tenant-id admin-id admin-first-name admin-last-name
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

(defn register-user! [tenant-id user-id first-name last-name email username password]
  (try-command @store-atom (user-command/register! tenant-id user-id first-name last-name email username password)))

(defn change-email! [user-id email]
  (try-command @store-atom (user-command/change-email! user-id email)))

(defn change-password! [user-id password new-password]
  (try-command @store-atom (user-command/change-password! user-id password new-password)))

(defn change-user-name! [user-id first-name last-name]
  (try-command @store-atom (user-command/change-name! user-id first-name last-name)))

(defn enable-user! [user-id tenant-id]
  (try-command @store-atom (user-command/enable! user-id tenant-id)))

(defn disable-user! [user-id tenant-id]
  (try-command @store-atom (user-command/disable! user-id tenant-id)))

(defn setup!
  [event-store]
  (reset! store-atom event-store))



