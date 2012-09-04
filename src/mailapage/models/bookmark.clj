(ns mailapage.models.user
  (:require korma.core
            [noir.util.crypt :as crypt]
            [noir.validation :as vali]
            [noir.session :as session]))

(defentity bookmarks
           (entity-fields :page)
           (belongs-to book)
           (belongs-to user))


