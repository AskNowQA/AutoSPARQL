For the deployed version to work:

1. Enough RAM for tomcat:
edit catalina.sh and add the following line:
export CATALINA_OPTS="-Xmx4096m"

2. Give tomcat webapps permissions to create and modify files

The following gives permissiosn to all webapps but as the path of autosparql-lite may change I find that simpler. You can of course just use the full path.

Go to your Tomcat base directory.
Find the file 50local.policy ( usually you can find it in "$TOMCAT_BASE/conf/policy.d/" ) If you haven't this directory structure, I'm sure that you can find the file catalina.policy in directory "$TOMCAT_BASE/conf/". (Both file are OK).
You have to edit one of these. At the final of the file you should type this:

grant codeBase "file:${catalina.base}/webapps/-"
{
 permission java.security.AllPermission;
};

If this does not work then (this may be unsafe):
sudo chmod -R a+wr /var/lib/tomcat6/webapps
sudo chmod -R a+wr /var/lib/tomcat6/cache



3. mkdir /var/lib/tomcat6/cache

4. You need the directory /opt/wordnet/dict (which is a copy of src/main/resources/tbsl/dict).
Alternatively modify your local copy of src/main/resources/tbsl/wordnet_properties.xml
and change the following to an absolute path containing those files:
<param name="dictionary_path" value="/opt/wordnet/dict"/> 