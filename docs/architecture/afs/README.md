# AFS: the Application FileSystem

**AFS** stands for **A**pplication **F**ile**S**ystem.

An AFS is a kind of object database aimed at storing powsybl-related **business** data in a hierarchical way, like a file system does for plain files.

The data in an AFS consists of a tree of node objects. An application, using the AFS APIs, can access several AFS instances, each identified by an unique name.  
  
Each node may be a **folder** or a **file** or a complex node abstraction type, either provided by Powsybl or added through the AFS APIs extension mechanism.  
For example, **Project** is a special predefined type which represents a workspace to carry out a study or a computation, while **Case** is another kind of file which represents a Network model.

Data in an AFS is backed by a storage implementation. Multiple storage implementations are available, providing specific functionalities: **in-memory** storage, **database** storage, **remote** storage, etc.

The structure of an AFS looks like this example: 


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


In the [Tutorials](../../tutorials/README.md) section you will learn howto
 - [use](../../tutorials/afs/howto-use-afs.md) the AFS API in your application.
 - [extend](../../tutorials/afs/howto-extend-afs.md) and customize AFS, for your needs.

# TODO:
- explain the concepts of Node, File, Folder, ProjectFile and ProjectFolder
- explain the storage / business layers
- explain each different storage implementation
