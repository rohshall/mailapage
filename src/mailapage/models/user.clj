(ns mailapage.models.user
  (:require [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session])
  (:use     [korma.core]
            [mailapage.models.common :only (mailapage-db)]))

(def bookmarks)

;; entity
(defentity users
           (entity-fields :username :password)
           (has-many bookmarks)
           (database mailapage-db))

;; Gets

(defn all []
  (select* users))

(defn get-by-name [username]
  (select users
          (where {:username username})))
    
(defn admin? []
  (session/get :admin))

(defn me []
  (session/get :username))

;; Validations

(defn valid-username? [username]
  (vali/rule (not (get-by-name username))
             [:username "That username is already taken"])
  (vali/rule (vali/min-length? username 3)
             [:username "Username must be at least 3 characters."])
  (not (vali/errors? :username)))

(defn valid-psw? [password]
  (vali/rule (vali/min-length? password 5)
             [:password "Password must be at least 5 characters."])
  (not (vali/errors? :password)))

;; Operations

(defn- insert! [{:keys [username password]}]
  (insert users
          (values {:username username :password password})))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (get-by-name username)]
    (if (and stored-pass 
             (crypt/compare password stored-pass))
      (do
        (when (= username "admin")
          (session/put! :admin true))
        (session/put! :username username))
      (vali/set-error :username "Invalid username or password"))))

(defn add! [{:keys [username password] :as user}]
  (when (and (valid-username? username) (valid-psw? password))
      (insert! user)))

(defn remove! [username]
  (delete users
          (where {:username username})))

(defn init! []
    (insert! {:username "admin" :password "admin"}))
