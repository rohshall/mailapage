(ns mailapage.models.bookmark
  (:require [mailapage.models.user :as user]
            [mailapage.models.book :as book]
            [noir.validation :as vali])
  (:use     [korma.core]))

(defentity bookmarks
           (entity-fields :page)
           (belongs-to book/books)
           (belongs-to user/users))

;; Gets

(defn all []
  (select* bookmarks))

(defn get-bookmark [username title]
  (let [user_id (-> username user/get-by-name .id)
        book_id (-> title book/get-by-title .id)]
    (select bookmarks
            (where {:user_id user_id
                    :book_id book_id}))))

;; Validations

(defn valid-page? [page]
  (vali/rule (vali/rule number? page)
             [:page "Page must be a number."])
  (not (vali/errors? :page)))

;; Operations

(defn- insert! [user_id book_id]
  (insert bookmarks
          (values {:user_id user_id
                   :book_id book_id
                   :page 0})))

(defn add! [user book]
  (insert! (.id user) (.id book)))

(defn remove! [user book]
  (delete bookmarks
          (where {:user_id (.id user)
                   :book_id (.id book)})))
