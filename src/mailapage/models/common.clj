(ns mailapage.models.common
  (:use korma.db))

(defdb mailapage-db (postgres 
                      {:db "mailapage"
                       :user "salil"
                       :password "mua12NU"
                       ;;OPTIONAL KEYS
                       :host "localhost"
                       :port "5432"
                       :delimiters "" })) ;; remove delimiters
