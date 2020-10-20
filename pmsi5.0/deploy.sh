#!/bin/bash

cd ${WORKSPACE}

cat > ${WORKSPACE}/target/jet/app/version.txt <<- _EOF_
Version: ${POM_VERSION}, Build ID: ${BUILD_NUMBER}-${GIT_COMMIT}
_EOF_

cat > ${WORKSPACE}/target/jet/bin.xml <<- _EOF_
<?xml version="1.0" encoding="UTF-8"?>
<assembly
  xmlns="http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="
    http://maven.apache.org/plugins/maven-assembly-plugin/assembly/1.1.2
      http://maven.apache.org/xsd/assembly-1.1.2.xsd"
>
  <id>sample</id>
  <formats>
    <format>zip</format>
  </formats>
  <fileSets>
    <fileSet>
      <outputDirectory>/</outputDirectory>
      <directory>app</directory>
    </fileSet>
  </fileSets>
  <dependencySets>
    <dependencySet>
      <useStrictFiltering>true</useStrictFiltering>
      <useProjectArtifact>false</useProjectArtifact>
      <scope>runtime</scope>
    </dependencySet>
  </dependencySets>
</assembly>

_EOF_

cat > ${WORKSPACE}/target/jet/pom.xml <<- _EOF_

<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                      http://maven.apache.org/xsd/maven-4.0.0.xsd">

	<modelVersion>4.0.0</modelVersion>
	<groupId>com.digivalet</groupId>
	<artifactId>digivalet-pmsi</artifactId>
	<version>${POM_VERSION}</version>
	<description>DigiValet PMSI application.</description>

	<properties>
		<jdk.version>1.7</jdk.version>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
	</properties>


	<repositories>
		<repository>
			<id>digivalet-repo</id>
			<name>DigiValet Maven Repository</name>
			<url>http://\${source_server}:8081/artifactory/digivalet-repo/</url>
		</repository>
	</repositories>

	<distributionManagement>
		<repository>
    		<id>digivalet-global-repo</id>
		    <name>DigiValet Artifactory</name>
   			<url>http://\${destination_server}:8081/artifactory/libs-release-local/</url>
		</repository>
		<snapshotRepository>
    		<id>digivalet-global-repo</id>
		    <name>DigiValet Artifactory</name>
   			<url>http://\${destination_server}:8081/artifactory/libs-snapshot-local/</url>
		</snapshotRepository>		
	</distributionManagement>



	<build>
		<plugins>
	        <plugin>
    	        <artifactId>maven-assembly-plugin</artifactId>
				<version>2.4.1</version>            
            	<configuration>
                        <appendAssemblyId>false</appendAssemblyId>
                        <finalName>\${project.artifactId}-\${project.version}</finalName>
                        <descriptor>bin.xml</descriptor>
	            </configuration>
    	        <executions>
        	        <execution>
            	        <id>create-distribution</id>
                	    <phase>install</phase>
	                    <goals>
    	                    <goal>single</goal>
        	            </goals>
            	    </execution>
	            </executions>
    	    </plugin>
        </plugins>
    </build>

</project>

_EOF_

cd ${WORKSPACE}/target/jet/
#mvn clean deploy -Dddm_server=${ddm_server}
