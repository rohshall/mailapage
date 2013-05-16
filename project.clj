(defproject mailapage "0.1.0-SNAPSHOT"
            :description "A daemon that helps users read a book by mailing them a page of a book a day"
            :dependencies [[org.clojure/clojure "1.5.1"]
                           [org.apache.pdfbox/pdfbox "1.8.1"]
                           [org.clojure/java.jdbc "0.3.0-alpha4"]
                           [org.xerial/sqlite-jdbc "3.7.2"]]
            :main mailapage.app)

