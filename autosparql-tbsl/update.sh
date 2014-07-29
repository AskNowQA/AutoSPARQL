git pull
mvn -o clean package
rm /var/lib/tomcat7/webapps/autosparql-lite.war
cp target/autosparql-lite.war /var/lib/tomcat7/webapps/
service tomcat7 restart
