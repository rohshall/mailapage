(ns mailapage.models.user
  (:require korma.core
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

;; entity
(defentity users
           (entity-fields :username :password)
           (has-many bookmarks))

;; Gets

(defn all []
  (select users)

(defn get-username [username]
  (select users
    (where {:username username})))
    
(defn admin? []
  (session/get :admin))

(defn me []
  (session/get :username))

;; Mutations and Checks

(defn prepare [{password :password :as user}]
  (assoc user :password (crypt/encrypt password)))

(defn valid-user? [username]
  (vali/rule (not (get-username username))
             [:username "That username is already taken"])
  (vali/rule (vali/min-length? username 3)
             [:username "Username must be at least 3 characters."])
  (not (vali/errors? :username :password)))

(defn valid-psw? [password]
  (vali/rule (vali/min-length? password 5)
             [:password "Password must be at least 5 characters."])
  (not (vali/errors? :password)))

;; Operations

(defn- insert! [{:keys [username password]}]
  (insert users
          (values {:username username :password password})))

(defn login! [{:keys [username password] :as user}]
  (let [{stored-pass :password} (get-username username)]
    (if (and stored-pass 
             (crypt/compare password stored-pass))
      (do
        (session/put! :admin true)
        (session/put! :username username))
      (vali/set-error :username "Invalid username or password"))))

(defn add! [{:keys [username password] :as user}]
  (when (valid-user? username)
    (when (valid-psw? password)
      (-> user (prepare) (insert!)))))

(defn remove! [username]
  (delete users
          (where {:username [= username]})))

(defn init! []
    (insert! (prepare {:username "admin" :password "admin"})))
