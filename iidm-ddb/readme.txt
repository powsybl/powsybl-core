iTESLA - IIDM-DDB  module
==========================



IIDM-DDB module must run in a jboss application server, coupled with a RDBMS
Provided availability of RDBMS specific JDBC drivers, any RDBMS would be OK; that said, 
in this guide a MYSQL is assumed;

Required software:

- Mysql v 5.x , to be downloaded here: http://dev.mysql.com/downloads/mysql/5.5.html#downloads
   (mysql installation is not covered in this guide)

- JDK 7 (JRE is not enough), to be downloaded here: http://www.oracle.com/technetwork/java/javase/downloads/index.html
   (JDK installation is not covered in this guide)

- apache Maven 3 http://maven.apache.org/
   (maven installation is not covered in this guide)

- Mysql JDBC drivers (latest version, writing time, is available here http://dev.mysql.com/get/Downloads/Connector-J/mysql-connector-java-5.1.23.tar.gz/from/http://cdn.mysql.com )

- Wildfly (JBoss 8) application server v8.1.0 Final, to be downloaded here: http://wildfly.org/downloads/


MYSQL iidm-ddb schema creation
----------------------------------------
execute the following 3-lines DDL script:


CREATE DATABASE itesladdb CHARACTER SET utf8 COLLATE utf8_general_ci;
GRANT ALL ON itesladdb.* TO 'itesla'@'localhost' IDENTIFIED BY 'itesla';
GRANT ALL ON itesladdb.* TO 'itesla'@'%' IDENTIFIED BY 'itesla';


JBoss installation
--------------------------------------

Installing JBoss means unzipping/untgz-ing the jboss archive in a directory  (any folder will do),
from here on let's call <JBOSS_HOME> the JBoss top folder


JBoss configuration 
--------------------

1) JBoss does not come with MYSQL JDBC drivers, to install them, copy  mysql-connector-java-5.1.23-bin.jar file (found in  Mysql JDBC drivers .tar.gz archive )
in JBOSS_HOME/modules/com/mysql/main folder and, in order to make the application server aware of the new connectors, create a new JBOSS_HOME/modules/com/mysql/main/module.xml with the following content

<?xml version="1.0" encoding="UTF-8"?>
<module xmlns="urn:jboss:module:1.0" name="com.mysql">
  <resources>
    <resource-root path="mysql-connector-java-5.1.23-bin.jar"/>
  </resources>
  <dependencies>
    <module name="javax.api"/>
  </dependencies>
</module>




2) edit JBOSS_HOME/standalone/configuration/standalone.xml, and add these new configurations:

in 'drivers' section

<driver name="mysqlDriver" module="com.mysql">
    <xa-datasource-class>com.mysql.jdbc.Driver</xa-datasource-class>
</driver>

in 'datasources' section

 <datasource jndi-name="java:/MySQLDS" pool-name="MySQLDS" enabled="true" use-java-context="true">
     <connection-url>jdbc:mysql://localhost:3306/itesladdb?useUnicode=true&amp;connectionCollation=utf8_general_ci&amp;characterSetResults=utf8&amp;characterEncoding=utf8</connection-url>
     <driver>mysqlDriver</driver>
     <security>
         <user-name>itesla</user-name>
         <password>itesla</password>
     </security>
 </datasource>

in 'subsystem' section, edit property default-missing-method-permissions-deny-access
  <default-missing-method-permissions-deny-access value="false"/>   


3) edit JBOSS_HOME\modules\system\layers\base\sun\jdk\main\module.xlm
   add  <path name="com/sun/jndi/url/rmi"/>


4) defining two example users (application and management domains, in the default JBoss security domain)

  shell in JBOSS_HOME/bin

   ./add-user.sh

  select "b) Application User"
  press enter (leave default (ApplicationRealm) )
  insert 'user' as 'Username' prompt
  insert 'password' as 'password' prompt
  reinsert 'password' as 'Re-enter Password' prompt
  insert 'user' at 'What roles ...' prompt
  enter 'yes' at prompt 'Is this correct yes/no' ?



  ./add-user.sh

  select "a) Management User"
  press enter (leave default (ApplicationRealm) )
  insert 'admin' as 'Username' prompt
  insert 'password' as 'password' prompt
  reinsert 'password' as 'Re-enter Password' prompt
  enter 'yes' at prompt 'Is this correct yes/no' ?
  
  note: 
   application users and roles new data are written to: 
    JBOSS_HOME/standalone/configuration/application-users.properties
    JBOSS_HOME/standalone/configuration/application-roles.properties
   management users data are written to: 
    JBOSS_HOME/standalone/configuration/mgmt-users.properties
    

Starting the application server
-------------------------------
shell in JBOSS_HOME/bin
./standalone.sh


The JBOSS default web based console is available at http://JBOSS_IP:8080/  (admin/password)


IIDM-DDB Converter configuration
---------------------------------------

The eurostag to modelica converter uses a folder to temporarily store the files to be converted.
The folder, set by default to /tmp, is configured in the converter.properties file 
(see the resource folder in the iidm-ddb-eurostag-modelica-converter project).
Before building and deploying the project make sure to check this configuration 
(and the permission on the selected folder)




IIDM-DDB module building and deploying
---------------------------------------

1) module building: 
shell in the iidm-ddb folder

mvn clean install


2) module deploying:

copy the iidm-ddb/iidm-ddb-ear/target/iidm-ddb-ear.ear to the JBOSS_HOME/standalone/deployments


3) After a successful deployment the ddb web-ui (in the current version, shows only the list of all the equipment ids) is available at

  http://JBOSS_IP:8080/iidm-ddb-web     ( enter 'user', 'password' to log in)


4) module undeploying:
remove iidm-ddb-ear.ear from  JBOSS_HOME/standalone/deployments
