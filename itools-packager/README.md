itools packager 
---------------
This Maven plugin can be used to generate an itools package with the following layout:
```bash
<package-name>
    bin
        itools
        itools.bat
        powsyblsh
        tools-mpi-task.sh
        <additional binaries>
    lib
        <additional libraries>
    etc
        logback-itools.xml
        itools.conf
        <additional config files>
    share
        java
            <jars of the project classpath>
```

Here is how to configure itools package Maven plugin in your project
```xml
    <build>
        <plugins>
            <plugin>
                <groupId>com.powsybl</groupId>
                <artifactId>powsybl-itools-packager-maven-plugin</artifactId>
                <version>X.X.X</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>package-zip</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <packageName>powsybl</packageName>
                    <archiveName>powsybl-x-y-x</archiveName>
                    <javaXmx>8G</javaXmx>
                    <mpiTasks>2</mpiTasks>
                    <mpiHosts>
                        <param>host1</param>
                        <param>host2</param>
                    </mpiHosts>
                    <copyToBin>
                        <files>
                            <file>...</file>
                        </files>
                    </copyToBin>
                    <copyToLib>
                        <files>
                            <file>...</file>
                        </files>
                    </copyToLib>
                    <copyToEtc>
                        <files>
                            <file>...</file>
                        </files>
                    </copyToEtc>
                </configuration>
            </plugin>
        </plugins>
    </build>
```

- packageName is optional, project final name is used as default value.
- archiveName is optional, packageName is used as default value.
- javaXmx, mpiTasks and mpiHosts are used to generate itools.conf, are all optional and default values are respectively 8G, 2 and localhost.
- additional binaries, libraries and configurations file can be added to the package using optional copyToBin, copyToLib and copyToEtc tags.
- all of the jars with compile and runtime scope will be included in the package
