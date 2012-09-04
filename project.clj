(defproject mailapage "0.1.0-SNAPSHOT"
            :description "Web application that helps users read a book by mailing them a page of a book a day"
            :dependencies [[org.clojure/clojure "1.4.0"]
                           [org.apache.pdfbox/pdfbox "1.7.1"]
                           [korma "0.3.0-beta7"]
                           [postgresql "9.0-801.jdbc4"]
                           [noir "1.3.0-beta3"]]
            :main mailapage.server)

