# Getting started with AFS

**AFS** stands for **A**pplication **F**ile**S**ystem.

An AFS is meant to be used to organize your **business** data and store them,
like a file system does for plain files.

The structure of an AFS looks like:

```
   AppData
     +-- FileSystem1
     |   +-- File1
     |   +-- File2
     |   +-- Project1
     |   |   +-- RootFolder
     |   |       +-- ProjectFile1
     |   |       +-- ProjectFolder1
     |   |       |   +-- ProjectFile2
     |   |       +-- ProjectFolder2
     |   |           +-- ProjectFile3
     |   +-- Project2
     |      ...
     |
     +-- FileSystem2
         ...
```
where each "project file" may represent a business object, for instance a network or a list of contingencies, or even a computation.

Below, you will learn:
 - to **use** the AFS API in your application
 - to **extend** and customize AFS for your needs

# 1- Using AFS

### Using AFS in your java application

In order to use AFS in your application, you will first need to add some dependencies to your project:
 - `powsybl-afs-core` to use the core API in your code
 - an actual implementation available at runtime: powsybl comes with `powsyl-afs-mapdb` for prototyping
 - basic file types defined as extensions of the core: `powsybl-afs-ext-base`.

For instance, if you use maven, in the dependencies section:
```xml
<dependency>
  <groupId>com.powsybl</groupId>
  <artifactId>powsybl-afs-core</artifactId>
</dependency>
<dependency>
  <groupId>com.powsybl</groupId>
  <artifactId>powsybl-afs-ext-base</artifactId>
</dependency>
<dependency>
  <groupId>com.powsybl</groupId>
  <artifactId>powsybl-afs-mapdb</artifactId>
  <scope>runtime</scope>
</dependency>
```

By default, powsybl will load application file system depending on your platform configuration (file *${HOME}/.itools/config.yml*). To tell AFS where it should store its data, you will need to configure your mapdb file system:
```yml
mapdb-app-file-system:
  drive-name : my-first-fs
  db-file : /path/to/my/mapdb/file
```
or in XML:
```xml
<mapdb-app-file-system>
  <drive-name>my-first-fs</drive-name>
  <db-file>/path/to/my/mapdb/file</db-file>
</mapdb-app-file-system>
```

Here, we have defined a mapdb based file system, which will be named *my-first-fs* in your application, and stored in the file */path/to/my/mapdb/file*.

Now, from your application, you will be able to interact with that file system. For example, you can create directories and projects:
```java
//Build an instance of AppData
ComputationManager c = LocalComputationManager.getDefault(); //Do not pay attention to this part
AppData appData = new AppData(c, c);

//Get your file system
AppFileSystem myFirstFs = appData.getFileSystem("my-first-fs");
//Create a new folder at the root of you file system, and a new project in that folder.
Project myFirstProject = myFirstFs.getRootFolder()
         .createFolder("my-first-folder")
         .createProject("my-first-project");
```

Everything that we have just created will be persisted to your mapdb file. Your file system tree now looks like:
```
my-first-fs
  +-- my-first-folder
    +--my-first-project
```

### Using AFS from groovy scripts

Your configured AFS is also accessible from groovy. This comes in 2 flavours, either with an interactive console using the powsybl shell `powsyblsh`:
```bash
powsyblsh
groovy:000> :register com.powsybl.scripting.groovy.InitPowsybl
groovy:000> :init_powsybl
groovy:000> import com.powsybl.contingency.*
groovy:000>
```

or using the `itools` command to execute a groovy script:
```bash
itools run-script --file my_script.groovy
```

From groovy code, powsybl provides a variable called `afs` which exposes base methods to access configured file systems. You can then simply perform the same thing as in the java section this way:
```groovy
//Create a new folder at the root of you file system, and a new project in that folder.
myFirstProject = afs.getRootFolder("my-first-fs")
                    .createFolder("my-first-folder")
                    .createProject("my-first-project");
```


### Using actual business objects

All this is fine, but the primary goal of AFS is to manage your **business objects**, which we have not seen so far.

AFS is fully extendable with your own type of files, but it already comes with a few basic types for grid studies. The most basic one may be the `ImportedCase` type, which expose a `Network` object to the API.

Such files may only be created inside a project. Projects may be seen as a kind of workspace for a particular study or computation. Inside of a project, we can import a case from a file representing a network, for example an XIIDM file or a UCTE file.

In java:
```java
ImportedCase myImportedCase = myFirstProject.getRootFolder()
              .fileBuilder(ImportedCaseBuilder.class)
              .withName("my-imported-case")
              .withFile(Paths.get("path/to/network.xiidm"))
              .build();
```

or in groovy with a nice, simplified syntax:
```groovy
myImportedCase = myFirstProject.getRootFolder().buildImportedCase {
              name "my-imported-case"
              file Paths.get("path/to/network.xiidm")
            }
```

Now our tree looks like:
```
my-first-fs
  +-- my-first-folder
    +-- my-first-project
      +-- "my-imported-case"
```

You can then use the methods exposed by your imported case to carry out some business related logic:

```java
//use stored network
Network network = myImportedCase.getNetwork();
//Carry out some computations ...
...
//Query stored network to get all substations names
System.out.println(myImportedCase.queryNetwork(ScriptType.GROOVY,
                            "network.substationStream.map {it.name} collect()"))
```

Of course, you can later retrieve your imported case from another execution or from another application, once it has been persisted to the underlying storage:
```java
//From another application:
ImportedCase importedCase = appData.getFileSystem("my-first-fs")
                                   .getRootFolder()
                                   .getChild(Project.class, "my-first-folder/my-first-project").get()
                                   .getRootFolder().getChild("my-imported-case")
                                   .orElseThrow(() -> new RuntimeException("Not found"));

Network network = myImportedCase.getNetwork();
//Do some stuff with network
...
```


### Using a remote file system

Powsybl provides a special implementation of application file system storage which forwards calls, through a REST API, to a remote AFS server. The server may use any storage implementation itself, for example the mapdb implementation.

That feature makes it easy to store data on a remote server.

In order to use it you will need to:
 - package in a war and deploy `powsybl-afs-ws-server` in a JEE server, like Wildfly
 - configure app file systems in the server powsybl configuration file, for instance defining a mapdb file system as above. You will need to add support for remote acces by setting the additional `remotely-accessible` parameter to true:
 ```yml
  mapdb-app-file-system:
    drive-name : my-first-fs
    db-file : /path/to/my/mapdb/file
    remotely-accessible: true
 ```
 - add `powsybl-afs-ws-client` to the runtime dependencies of your client application
 - configure in the powsybl configuration of your application the following rest file system:
```yml
  remote-service:
    host-name: my-afs-server
    app-name: my-server-app
    port: 8080
    secure: false
```

Now all file systems defined in the server configurations will be transparently accessible from your client application, without changing any of your code!

This allows for great flexibility in the deployment of your application, for instance to run the same application as standalone or client/server.

**Note:**
The underlying REST API is documented at http://my-afs-server:8080/my-server-app/rest/swagger

### Listeners

TO BE COMPLETED

### Services

TO BE COMPLETED

### Dependencies

TO BE COMPLETED

# 2- Extending AFS for your needs

### 2.1 Adding your own file types

Powsybl-core already comes with a number of project file types:
 - Imported cases and virtual cases
 - Contingency stores
 - Action scripts
 - Security analysis runner: an object from which you can launch a security analysis

However, you will probably want to extend that list with your own type of data or computation. AFS provides an easy way to do so.

Each project file type in AFS relies on the definition of 3 classes:
 - The actual new type, which must extends ProjectFile
 - A builder class for the new type, which must extends ProjectFile
 - An extension class, in charge of registering the new type and its builder to AFS. It must implement the ProjectFileExtension interface, and be registered through the use of `@AutoService` annotation.

First define you new type :
```java
class FooFile extends ProjectFile {

    FooFile(ProjectFileCreationContext context) {
        super(context, 0);
    }

    public void doSomeCoolStuff() { ... }
    public Foo getSomeCoolData() { ... }
    public FooResult runSomeCoolComputation() { ... }
}
```

Then define how to build it. This involves at least creating a new node in the underlying storage, and may involve writing some data blob or defining depencencies to other nodes:
```java
class FooFileBuilder implements ProjectFileBuilder<FooFile> {

    private final ProjectFileBuildContext context;
    private String name;

    FooFileBuilder(ProjectFileBuildContext context) {
        this.context = Objects.requireNonNull(context);
    }

    public FooFileBuilder withName(String name) {
        this.name = name;
        return this;
    }

    //Storeds new node information in underlying storage, and builds actual object
    @Override
    public FooFile build() {
        if (name == null) {
            throw new IllegalArgumentException("name is not set");
        }
        String pseudoClass = "foo";

        //Create new node in storage
        NodeInfo info = context.getStorage().createNode(context.getFolderInfo().getId(), name, pseudoClass, "", 0, new NodeGenericMetadata());

        //Possibly, write data to storage
        try (OutputStream os = context.getStorage().writeBinaryData(info.getId(), "my_data")) {
            os.write(...);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        //Possibly, add dependencies to other nodes
        context.getStorage().addDependency(info.getId(), "my_dependency", dependencyNodeId);

        //Flush modifications
        context.getStorage().flush();

        //Return new object
        return new FooFile(new ProjectFileCreationContext(info,
                                                          context.getStorage(),
                                                          context.getProject()));
    }
}
```

Finally, register your new type and its builder type through and extension class:
```java
@AutoService(ProjectFileExtension.class)
public class FooFileExtension implements ProjectFileExtension<FooFile, FooFileBuilder> {
    @Override
    public Class<FooFile> getProjectFileClass() {
        return FooFile.class;
    }

    //Define the name of this type for AFS
    @Override
    public String getProjectFilePseudoClass() {
        return "foo";
    }

    @Override
    public Class<FooFileBuilder> getProjectFileBuilderClass() {
        return FooFileBuilder.class;
    }

    @Override
    public FooFile createProjectFile(ProjectFileCreationContext context) {
        return new FooFile(context);
    }

    @Override
    public FooFileBuilder createProjectFileBuilder(ProjectFileBuildContext context) {
        return new FooFileBuilder(context);
    }
}
```
Notice the definition of a "pseudo class" name, which is the name of the type of this project file as known by the AFS storage.


And that's it! You will now be able to use this new type of project file as any other.
For instance, to create one in groovy:
```groovy
myFooFile = myFirstProject.getRootFolder().buildFoo { name "my-foo-file" }
//use it!
myFooFile.runSomeCoolComputation()
```


### 2.2 Adding your own storage implementation

TODO
