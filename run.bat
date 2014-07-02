cd commons
mvn install -N -DskipTests=true
cd ../algorithm-tbsl
mvn install -N -DskipTests=true
cd ../autosparql-tbsl
mvn jetty:run
