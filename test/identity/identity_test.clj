(ns identity.identity_test
  (:use clojure.test)
  (:require [rill.aggregate :refer [load-aggregate]]
            [rill.event-store :refer [retrieve-events]]
            [rill.temp-store :refer [given]]
            [identity.identity :as identity]
            [identity.domain.model.tenant :as tenant]
            [identity.domain.model.user :as user]
            [identity.infrastructure.common :refer [new-uuid]]))

(deftest test-provision-tenant
  (testing "Tenant is provisioned"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sauze.com"
          username "kaiser"
          password "thepwass"
          store (given [])
          _ (identity/setup! store)
          _ (identity/provision-tenant! tenant-id name description
                                        user-id first-name last-name email username password)
          tenant (load-aggregate (retrieve-events store tenant-id))
          user (load-aggregate (retrieve-events store user-id))]
      (is (= name (:name tenant)))
      (is (= description (:description tenant)))
      (is (= first-name (:first-name user)))
      (is (= last-name (:last-name user)))
      (is (= username (:username user)))
      (is (= email (get-in user [:contact-info :email])))
      (is (= (user/encrypt! password user-id) (:password user)))
      (is (user/enabled? user)))))

(deftest test-tenant-status
  (testing "Tenant is activated"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          store (given [(tenant/provisioned tenant-id name description)])
          _ (identity/setup! store)
          _ (identity/activate-tenant! tenant-id)
          tenant (load-aggregate (retrieve-events store tenant-id))]
      (is (tenant/active? tenant))))

  (testing "Tenant is deactivated"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          store (given [(tenant/provisioned tenant-id name description)
                        (tenant/activated tenant-id)])
          _ (identity/setup! store)
          _ (identity/deactivate-tenant! tenant-id)
          tenant (load-aggregate (retrieve-events store tenant-id))]
      (is (tenant/inactive? tenant))))
  )

(deftest test-tenant-update
  (testing "Tenant name is updated"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          new-name "New tenant"
          store (given [(tenant/provisioned tenant-id name description)])
          _ (identity/setup! store)
          _ (identity/change-tenant-name! tenant-id new-name)
          tenant (load-aggregate (retrieve-events store tenant-id))]
      (is (= new-name (:name tenant)))))
  (testing "Tenant description is updated"
    (let [tenant-id (new-uuid)
          name "A tenant"
          description "The tenant description"
          new-desc "New Desc tenant"
          store (given [(tenant/provisioned tenant-id name description)])
          _ (identity/setup! store)
          _ (identity/change-description! tenant-id new-desc)
          tenant (load-aggregate (retrieve-events store tenant-id))]
      (is (= new-desc (:description tenant))))))

(deftest test-user-registration
  (testing "User can be register to an active tenant"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          store (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc")
                        (tenant/activated tenant-id)])
          _ (identity/setup! store)
          _ (identity/register-user! tenant-id user-id first-name last-name email username password)
          user (load-aggregate (retrieve-events store user-id))]
      (is (= first-name (:first-name user)))
      (is (= last-name (:last-name user)))
      (is (= username (:username user)))
      (is (= email (get-in user [:contact-info :email])))
      (is (= (user/encrypt! password user-id) (:password user)))
      (is (user/enabled? user))))
  (testing "User can not register to an inactive tenant"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          store (given [(tenant/provisioned tenant-id "A Tenant" "A nice desc")
                        (tenant/activated tenant-id)
                        (tenant/deactivated tenant-id)])
          _ (identity/setup! store)
          [status _] (identity/register-user! tenant-id user-id first-name last-name email username password)]
      (is (= status :rejected)))))

(deftest test-user-info
  (testing "User can change the password"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          new-pass "newpass"
          store (given [(user/registered tenant-id user-id first-name last-name email username password)])
          _ (identity/setup! store)
          _ (identity/change-password! user-id password new-pass)
          user (load-aggregate (retrieve-events store user-id))]
      (is (user/valid-credentials? user username new-pass))))
  (testing "User can't change the password if invalid credentials provided"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          new-pass "newpass"
          store (given [(user/registered tenant-id user-id first-name last-name email username password)])
          _ (identity/setup! store)
          [status _] (identity/change-password! user-id "invalid pass" new-pass)
          user (load-aggregate (retrieve-events store user-id))]
      (is (= password (:password user)))
      (is (= status :rejected))))

  (testing "User password can't be blank"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          new-pass "      "
          store (given [(user/registered tenant-id user-id first-name last-name email username password)])
          _ (identity/setup! store)
          [status _] (identity/change-password! user-id password new-pass)
          user (load-aggregate (retrieve-events store user-id))]
      (is (= password (:password user)))
      (is (= status :rejected))))

  (testing "User can change the email"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          new-email "newemail@kaiser.com"
          store (given [(user/registered tenant-id user-id first-name last-name email username password)])
          _ (identity/setup! store)
          _ (identity/change-email! user-id new-email)
          user (load-aggregate (retrieve-events store user-id))]
      (is (= new-email (get-in user [:contact-info :email])))))
  (testing "User email can not be set to blank"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          store (given [(user/registered tenant-id user-id first-name last-name email username password)])
          _ (identity/setup! store)
          [status _] (identity/change-email! user-id " ")
          user (load-aggregate (retrieve-events store user-id))]
      (is (= status :rejected))
      (is (= email (get-in user [:contact-info :email])))))

  (testing "User can change its name"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          new-first-name "Kevin"
          new-last-name "Spacey"
          store (given [(user/registered tenant-id user-id first-name last-name email username password)])
          _ (identity/setup! store)
          _ (identity/change-user-name! user-id new-first-name new-last-name)
          user (load-aggregate (retrieve-events store user-id))]
      (is (= new-first-name (:first-name user))
          (= new-last-name (:last-name user))))))

(deftest test-user-enablement
  (testing "User can be disabled"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          store (given [(tenant/provisioned tenant-id "Tenant" "Desc")
                        (tenant/activated tenant-id)
                        (user/registered tenant-id user-id first-name last-name email username password)
                        (user/enabled user-id)])
          _ (identity/setup! store)
          [_ _] (identity/disable-user! user-id tenant-id)
          user (load-aggregate (retrieve-events store user-id))]
      (is (user/disabled? user))))

  (testing "User can't be disabled if tenant is inactive"
    (let [tenant-id (new-uuid)
          user-id (new-uuid)
          first-name "Kaiser"
          last-name "Sausze"
          email "kaiser@sausze.com"
          username "theuser"
          password "thepass"
          store (given [(tenant/provisioned tenant-id "Tenant" "Desc")
                        (tenant/activated tenant-id)
                        (tenant/deactivated tenant-id)
                        (user/registered tenant-id user-id first-name last-name email username password)
                        (user/enabled user-id)])
          _ (identity/setup! store)
          [status _] (identity/disable-user! user-id tenant-id)
          user (load-aggregate (retrieve-events store user-id))]
      (is (user/enabled? user))
      (is (= status :rejected))))
  )