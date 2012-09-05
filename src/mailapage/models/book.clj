(ns mailapage.models.book
  (:require [noir.validation :as vali])
  (:use     [korma.core]
            [mailapage.models.common :only (mailapage-db)])
  (:import  java.io.File))

(def bookmarks)

(defentity books
           (entity-fields :title :location)
           (has-many bookmarks)
           (database mailapage-db))

;; Gets
(defn all []
  (select* books))

(defn get-by-title [title]
  (select books
          (where {:title title})))

;; Validations

(defn valid-title? [title]
  (vali/rule (vali/has-value? title)
             [:title "Title must be at least 1 character long."])
  (not (vali/errors? :title)))


(defn is-file? [filename]
  (let [f (File. filename)]
    (.isFile f)))

(defn valid-location? [location]
  (vali/rule (vali/rule is-file? location)
             [:location "Location must be a valid file."])
  (not (vali/errors? :location)))

;; Operations

(defn- insert! [{:keys [title location]}]
  (insert books
          (values {:title title :location location})))

(defn add! [{:keys [title location] :as book}]
  (when (and (valid-title? title) (valid-location? location))
    (insert! book)))

(defn remove! [title]
  (delete books
          (where {:title title})))

