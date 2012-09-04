(ns mailapage.models.user
  (:require korma.core
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

(defentity books
           (entity-fields :title :location)
           (has-many bookmarks))

