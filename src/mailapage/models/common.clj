(use 'korma.db)

(defdb mailapage (postgres {:db "mailapage"
                       :user "salil"
                       :password "mua12NU"
                       ;;OPTIONAL KEYS
                       :host "localhost"
                       :port "5432"
                       :delimiters "" ;; remove delimiters
                       :naming {:keys string/lower-case
                                ;; set map keys to lower
                                :fields string/lower-case}}))
                                ;; And field names are lower too
