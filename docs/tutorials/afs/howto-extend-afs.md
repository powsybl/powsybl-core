## Tutorial - Extending AFS

Howto extend [AFS](../../architecture/afs/README.md) for your needs

### Adding your own file types

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


### Adding your own storage implementation

TODO
