(ns identity.identity_test
  (:use clojure.test)
  (:require [rill.aggregate :refer [load-aggregate]]
            [rill.event-store :refer [retrieve-events]]
            [rill.repository :refer [wrap-basic-repository]]
            [rill.temp-store :refer [given]]
            [identity.identity :as identity]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.user :as user]
            [identity.domain.model.group :as group]
            [identity.infrastructure.common :refer [new-uuid]]
            [identity.domain.model.role :as role]))

(deftest test-provision-tenant
  (testing "Tenant is provisioned"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sauze.com"
          username "kaiser"
          password "thepwass"
          user-id (user/user-id {:tenant-id tenant-id :username username})
          store (wrap-basic-repository (given []))
          _ (identity/setup! store)
          [status _] (identity/provision-tenant! tenant-id name description
                                        first-name last-name email username password)
          tenant (load-aggregate (retrieve-events store tenant-id))
          user-details (identity/user-details user-id 0)]
      (is (= name (:name tenant)))
      (is (= description (:description tenant)))
      (is (= first-name (:first-name user-details)))
      (is (= last-name (:last-name user-details)))
      (is (= username (:username user-details)))
      (is (= email (:email user-details)))
      (is (= (user/encrypt! password tenant-id) (:password user-details)))
      ;(is (user/enabled? user-details))
      ;(is (identity/in-role? tenant-id username role/admin-role-name)))
    )))

;(deftest test-tenant-update
;  (testing "Tenant name is updated"
;    (let [tenant-id (new-uuid)
;          name "A tenant"
;          description "The tenant description"
;          new-name "New tenant"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id name description "first-name" "last-name" "email@tan.com"
;                                            "username" "password")]))
;          _ (identity/setup! store)
;          _ (identity/change-tenant-name! tenant-id new-name)
;          tenant (load-aggregate (retrieve-events store tenant-id))]
;      (is (= new-name (:name tenant)))))
;  (testing "Tenant description is updated"
;    (let [tenant-id (new-uuid)
;          name "A tenant"
;          description "The tenant description"
;          new-desc "New Desc tenant"
;          store (given [(tenant/provisioned tenant-id name description "first-name" "last-name" "email@tan.com"
;                                            "username" "password")])
;          _ (identity/setup! store)
;          _ (identity/change-description! tenant-id new-desc)
;          tenant (load-aggregate (retrieve-events store tenant-id))]
;      (is (= new-desc (:description tenant))))))
;;
;(deftest test-tenant-status
;  (testing "Tenant is activated"
;    (let [tenant-id (new-uuid)
;          name "A tenant"
;          description "The tenant description"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id name description "first-name" "last-name" "email@tan.com"
;                                            "username" "password")]))
;          _ (identity/setup! store)
;          _ (identity/activate-tenant! tenant-id)
;          tenant (load-aggregate (retrieve-events store tenant-id))]
;      (is (tenant/active? tenant))))
;
;  (testing "Tenant is deactivated"
;    (let [tenant-id (new-uuid)
;          name "A tenant"
;          description "The tenant description"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id name description "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)]))
;          _ (identity/setup! store)
;          _ (identity/deactivate-tenant! tenant-id)
;          tenant (load-aggregate (retrieve-events store tenant-id))]
;      (is (tenant/inactive? tenant))))
;  )
;
;(deftest test-user-registration
;  (testing "User can be register to an active tenant"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)]))
;          _ (identity/setup! store)
;          _ (identity/register-user! tenant-id first-name last-name email username password)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (= first-name (:first-name user)))
;      (is (= last-name (:last-name user)))
;      (is (= username (:username user)))
;      (is (= email (get-in user [:contact-info :email])))
;      (is (= (user/encrypt! password tenant-id) (:password user)))
;      (is (not (user/enabled? user)))))
;  (testing "User can not register to an inactive tenant"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)]))
;          _ (identity/setup! store)
;          [status _] (identity/register-user! tenant-id first-name last-name email username password)]
;      (is (= status :rejected))))
;  (testing "User can not register if username already exists"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (user/registered tenant-id first-name last-name email username password)]))
;          _ (identity/setup! store)
;          [status _] (identity/register-user! tenant-id first-name last-name email username password)]
;      (is (= status :rejected)))))
;
;(deftest test-user-info
;  (testing "User can change the password"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          new-pass "newpass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(user/registered tenant-id first-name last-name email username password)]))
;          _ (identity/setup! store)
;          _ (identity/change-password! tenant-id username password new-pass)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (user/valid-credentials? user username new-pass))))
;  (testing "User can't change the password if invalid credentials provided"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          new-pass "newpass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(user/registered tenant-id first-name last-name email username password)]))
;          _ (identity/setup! store)
;          [status _] (identity/change-password! tenant-id username "invalid pass" new-pass)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (= password (:password user)))
;      (is (= status :rejected))))
;
;  (testing "User password can't be blank"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          new-pass "      "
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(user/registered tenant-id first-name last-name email username password)]))
;          _ (identity/setup! store)
;          [status _] (identity/change-password! tenant-id username password new-pass)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (= password (:password user)))
;      (is (= status :rejected))))
;
;  (testing "User can change its name"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          new-first-name "Kevin"
;          new-last-name "Spacey"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(user/registered tenant-id first-name last-name email username password)]))
;          _ (identity/setup! store)
;          _ (identity/change-user-name! tenant-id username new-first-name new-last-name)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (= new-first-name (:first-name user))
;          (= new-last-name (:last-name user))))))

;(deftest test-user-enablement
;  (testing "User can be disabled"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (user/registered tenant-id first-name last-name email username password)
;                        (user/enabled tenant-id username)]))
;          _ (identity/setup! store)
;          [_ _] (identity/disable-user! tenant-id username)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (user/disabled? user))))
;
;  (testing "User can't be disabled if tenant is inactive"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)
;                        (user/registered tenant-id first-name last-name email username password)
;                        (user/enabled tenant-id username)]))
;          _ (identity/setup! store)
;          [status _] (identity/disable-user! tenant-id username)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (user/enabled? user))
;      (is (= status :rejected))))
;
;  (testing "User can be enabled"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (user/registered tenant-id first-name last-name email username password)
;                        (user/disabled tenant-id username)]))
;          _ (identity/setup! store)
;          [_ _] (identity/enable-user! tenant-id username)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (user/enabled? user))))
;
;  (testing "User can't be enabled if tenant is inactive"
;    (let [tenant-id (new-uuid)
;          first-name "Kaiser"
;          last-name "Sausze"
;          email "kaiser@sausze.com"
;          username "theuser"
;          password "thepass"
;          user-id (user/user-id {:tenant-id tenant-id :username username})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)
;                        (user/registered tenant-id first-name last-name email username password)
;                        (user/disabled tenant-id username)]))
;          _ (identity/setup! store)
;          [status _] (identity/enable-user! tenant-id username)
;          user (load-aggregate (retrieve-events store user-id))]
;      (is (user/disabled? user))
;      (is (= status :rejected)))))
;
;(deftest test-group
;  (testing "A group can be added"
;    (let [tenant-id (new-uuid)
;          name "group 1"
;          description "The desc"
;          group-id (group/group-id {:tenant-id tenant-id :name name})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)]))
;          _ (identity/setup! store)
;          [_ _] (identity/provision-group! tenant-id name description)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (= name (:name group)))
;      (is (= tenant-id (:tenant-id group)))
;      (is (= description (:description group)))))
;
;  (testing "A group can't be added to an inactive tenant"
;    (let [tenant-id (new-uuid)
;          name "group 1"
;          description "The desc"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)]))
;          _ (identity/setup! store)
;          [status _] (identity/provision-group! tenant-id name description)]
;      (is (= status :rejected))))
;
;  (testing "A duplicated group can't be added"
;    (let [tenant-id (new-uuid)
;          name "group 1"
;          description "The desc"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id name description)]))
;          _ (identity/setup! store)
;          [status _] (identity/provision-group! tenant-id name description)]
;      (is (= status :rejected))))
;
;  (testing "A group with a blank name can't be added"
;    (let [tenant-id (new-uuid)
;          name "  "
;          description "The desc"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id name description)]))
;          _ (identity/setup! store)
;          [status _] (identity/provision-group! tenant-id name description)]
;      (is (= status :rejected))))
;
;  (testing "Add a group as group member"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)]))
;          _ (identity/setup! store)
;          [_ _] (identity/add-group-member! tenant-id parent child)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (group/member? group child :group))))
;
;  (testing "Don't add a group if already a member of the parent group"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)
;                        (group/group-member-added tenant-id parent child)]))
;          _ (identity/setup! store)
;          [status _] (identity/add-group-member! tenant-id parent child)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (= status :rejected))))
;
;  (testing "Don't add a group if tenant inactive"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)]))
;          _ (identity/setup! store)
;          [status _] (identity/add-group-member! tenant-id parent child)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (not (group/member? group child :group)))
;      (is (= status :rejected))))
;
;  (testing "Don't add group if child doesn't exist"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id parent nil)]))
;          _ (identity/setup! store)
;          [status _] (identity/add-group-member! tenant-id parent child)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (= status :rejected))
;      (is (not (group/member? group child :group)))))
;
;  (testing "Don't add group if parent doesn't exist"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id child nil)]))
;          _ (identity/setup! store)
;          [status _] (identity/add-group-member! tenant-id parent child)]
;      (is (= status :rejected))))
;
;  (testing "Group Member can be removed"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)
;                        (group/group-member-added tenant-id parent child)]))
;          _ (identity/setup! store)
;          [_ _] (identity/remove-group-member! tenant-id parent child)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (not (group/member? group child :group)))))
;
;  (testing "Group Member can't be removed on an inactive tenant"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)
;                        (group/group-member-added tenant-id parent child)]))
;          _ (identity/setup! store)
;          [status _] (identity/remove-group-member! tenant-id parent child)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (group/member? group child :group))
;      (is (= status :rejected))))
;
;  (testing "Add user as group member"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          username "username"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (user/registered tenant-id "first-name" "last-name" "email@dot.com" username "password")
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)]))
;          _ (identity/setup! store)
;          [_ _] (identity/add-user-member! tenant-id parent username)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (group/member? group username :user))))
;
;  (testing "An user can't be added as group member on an inactive tenant"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          username "username"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (user/registered tenant-id "first-name" "last-name" "email@dot.com" username "password")
;                        (group/provisioned tenant-id parent nil)
;                        (tenant/deactivated tenant-id)]))
;          _ (identity/setup! store)
;          [status _] (identity/add-user-member! tenant-id parent username)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (not (group/member? group username :user)))
;      (is (= status :rejected))))
;
;  (testing "An user member can be removed from a group"
;    (let [tenant-id (new-uuid)
;          parent "parent"
;          child "child"
;          username "username"
;          group-id (group/group-id {:tenant-id tenant-id :name parent})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (user/registered tenant-id "first-name" "last-name" "email@dot.com" username "password")
;                        (group/provisioned tenant-id parent nil)
;                        (group/provisioned tenant-id child nil)
;                        (group/user-member-added tenant-id parent username)]))
;          _ (identity/setup! store)
;          [_ _] (identity/remove-user-member! tenant-id parent username)
;          group (load-aggregate (retrieve-events store group-id))]
;      (is (not (group/member? group username :user))))))
;
;(deftest test-role
;  (testing "Role can be provisioned"
;    (let [tenant-id (new-uuid)
;          name "A Role"
;          description "The desec"
;          nesting? true
;          role-id (role/role-id {:tenant-id tenant-id :role-name name})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)]))
;          _ (identity/setup! store)
;          [_ _] (identity/provision-role! tenant-id name description nesting?)
;          role (load-aggregate (retrieve-events store role-id))]
;      (is (= tenant-id (:tenant-id role)))
;      (is (= name (:name role)))
;      (is (not (nil? (:group-name role))))
;      (is (= description (:description role)))
;      (is (= nesting? (:supports-nesting role)))))
;
;  (testing "Role can't be provisioned on an inactive tenant"
;    (let [tenant-id (new-uuid)
;          name "A Role"
;          description "The desec"
;          nesting? true
;          role-id (role/role-id {:tenant-id tenant-id :role-name name})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (tenant/deactivated tenant-id)]))
;          _ (identity/setup! store)
;          [status _] (identity/provision-role! tenant-id name description nesting?)
;          role (load-aggregate (retrieve-events store role-id))]
;      (is (nil? role))
;      (is (= :rejected status))))
;
;  (testing "Role with a blank name can't be provisioned "
;    (let [tenant-id (new-uuid)
;          name "  "
;          description "The desec"
;          nesting? true
;          role-id (role/role-id {:tenant-id tenant-id :role-name name})
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)]))
;          _ (identity/setup! store)
;          [status _] (identity/provision-role! tenant-id name description nesting?)
;          role (load-aggregate (retrieve-events store role-id))]
;      (is (nil? role))
;      (is (= :rejected status))))
;
;  (testing "Role with a duplicated name can't be provisioned"
;    (let [tenant-id (new-uuid)
;          name "A role"
;          description "The desec"
;          nesting? true
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (role/provisioned tenant-id name description (group/new-internal-group-name) nesting?)]))
;          _ (identity/setup! store)
;          [status _] (identity/provision-role! tenant-id name description nesting?)]
;      (is (= :rejected status))))
;
;  (testing "An user can be assigned to a role"
;    (let [tenant-id (new-uuid)
;          role-name "A role"
;          username "theuser"
;          description "The desc"
;          nesting? true
;          group-name (group/new-internal-group-name)
;          store (wrap-basic-repository (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc" "first-name" "last-name" "email@tan.com"
;                                            "username" "password")
;                        (tenant/activated tenant-id)
;                        (role/provisioned tenant-id role-name description group-name nesting?)
;                        (user/registered tenant-id "first-name" "last-name" "email@dat.com" username "password")]))
;          _ (identity/setup! store)
;          [_ _] (identity/assign-user-to-role! tenant-id role-name username)]
;      (is (identity/in-role? tenant-id username role-name)))))