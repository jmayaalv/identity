(ns identity.identity
  (:require [rill.handler :refer [try-command]]
            [rill.aggregate :refer [load-aggregate]]
            [rill.repository :refer [retrieve-aggregate]]
            [identity.application.command.tenant :as tenant-command]
            [identity.application.command.user :as user-command]
            [identity.application.command.group :as group-command]
            [identity.application.command.role :as role-command]
            [identity.domain.model.user :as user]
            [identity.domain.model.role :as role]))

(defonce store-atom (atom nil))                             ;;fixme add a real store

(defn provision-tenant!
  "Creates a new tenant"
  [tenant-id name description admin-first-name admin-last-name admin-email admin-username admin-password]
    (try-command @store-atom (tenant-command/provision! tenant-id name description admin-first-name admin-last-name
                            admin-email admin-username admin-password)))

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

(defn provision-group! [tenant-id name description]
  (try-command @store-atom (group-command/provision! tenant-id name description)))

(defn add-group-member! [tenant-id parent child]
  (try-command @store-atom (group-command/add-group-member! tenant-id parent child)))

(defn remove-group-member! [tenant-id parent child]
  (try-command @store-atom (group-command/remove-group-member! tenant-id parent child)))

(defn add-user-member! [tenant-id username child]
  (try-command @store-atom (group-command/add-user-member! tenant-id username child)))

(defn remove-user-member! [tenant-id username child]
  (try-command @store-atom (group-command/remove-user-member! tenant-id username child)))

(defn provision-role! [tenant-id role-name description supports-nesting]
  (try-command @store-atom (role-command/provision! tenant-id role-name description supports-nesting)))

(defn assign-user-to-role! [tenant-id role-name username]
  (try-command @store-atom (role-command/assign-user! tenant-id role-name username)))

(defn unassign-user-to-role! [tenant-id role-name username]
  (try-command @store-atom (role-command/unassign-user! tenant-id role-name username)))

(defn assign-group-to-role! [tenant-id role-name group-name]
  (try-command @store-atom (role-command/assign-user! tenant-id role-name group-name)))

(defn unassign-group-to-role! [tenant-id role-name group-name]
  (try-command @store-atom (role-command/unassign-user! tenant-id role-name group-name)))

(defn in-role? [tenant-id username role-name]
  (let [role (retrieve-aggregate @store-atom (role/role-id {:tenant-id tenant-id :role-name role-name}))]
    (role/in-role? role {:tenant-id tenant-id :username username} @store-atom)))

(defn setup!
  [event-store]
  (reset! store-atom event-store))



