(datasources)=
# Datasources

## Principles

Datasources are Java-objects used to facilitate I/O operations around PowSyBl.
It allows users to read and write files 


## Types of datasources

Multiple types of datasources exist, depending on whether it shall be writable or not, the kind of storage used, 
data location, data compression, etc.


(readonlydatasources)=
### Read-Only DataSource

`ReadOnlyDataSource` is the most basic datasource interface available. As you can tell by the name, it only provides 
reading features.
It has two parameters: a base name (corresponding to the starting part of the files the user wants to consider in the 
datasource) and a main extension (corresponding to the file extension to consider, compression aside).

_**Example:**
For a file named `foo.bar.xiidm.gz`, the base name would be `foo.bar` or `foo` while the main extension would be `xiidm`._

The main methods `ReadOnlyDataSource` provides are:

- `exists(String fileName)` and `exists(String suffix, String ext)` to check if a file exists in the datasource
- `newInputStream(String fileName)` and `newInputStream(String suffix, String ext)` to read a file from the datasource
- `listNames(String regex)` to list the files in the datasource whose names match the regex

The methods with `String suffix, String ext` as parameters look for a file which name will be constructed as
`<basename><suffix>.<ext>`.

The classes inheriting directly `ReadOnlyDataSource` are:
- `ResourceDataSource`: datasource based on a list of resources
- `ReadOnlyMemDataSource`: datasource where data is stored in a `Map<filename, data as bytes>` in memory
- `MultipleReadOnlyDataSource`: datasource grouping multiple user-defined datasources
- `GenericReadOnlyDataSource`: datasource built by creating new datasources of multiple types

(writabledatasources)=
### DataSource

The `DataSource` interface extends `ReadOnlyDataSource` by adding writing features through the methods 
`newOutputStream(String fileName, boolean append)` and `newOutputStream(String suffix, String ext, boolean append)`.
Those methods allow the user to write in a new file (if `append==false`) or at the end of an existing one (if 
`append==true`).

This interface also provides two methods to create a datasource from a file path (`fromPath(Path file)`) or from a
directory and a file name (`fromPath(Path directory, String fileNameOrBaseName)`)

Two classes implement the `DataSource` interface:
- `MemDataSource`: extension of `ReadOnlyMemDataSource` implementing the writing features of `DataSource`
- `AbstractFileSystemDataSource`: datasource based on files present in the file system, either directly or in an archive

(directorydatasources)=
### Directory DataSource

`DirectoryDataSource` are datasources based on files located in a specific directory directly in the file system. 

Files stored and used via this type of datasource may be all compressed or not at all. Compression formats available are
defined in the class `CompressionFormat`. As of today, the following single-file compressions are available:
BZIP2, GZIP, XZ and ZSTD. Each one of those compression format has a corresponding datasource class inheriting
`DirectoryDataSource`: `Bzip2DirectoryDataSource`, `GzDirectoryDataSource`, `XZDirectoryDataSource`,
`ZstdDirectoryDataSource`.

`DirectoryDataSource` integrates the notions of base name and data extension:
- The base name is used to facilitate the access to files that all start with the same String. For example, `foo` would
be a good base name if your files are `foo.xiidm`, `foo_bar.xiidm`, `foo_mapping.csv`, etc.
- The data extension is the last extension of your files, excluding the compression extension if they have one.
It usually corresponds to the data format extension: `csv`, `xml`, `json`, `xiidm`, etc. This extension is mainly used
to identify the files to use in the datasource, for example when importing networks using the Importers implemented in
powsybl. 

Even if `DirectoryDataSource` integrates the notions of base name and data extension, you still have the possibility to
use files that do not correspond to the base name and data extension by directly providing their names, excluding the
compression extension.

(archivedatasources)=
### Archive DataSource

`AbstractArchiveDataSource` are datasources based on files located in a specific archive, in the file system. As of today,
two classes implements `AbstractArchiveDataSource`: `ZipArchiveDataSource` and `TarArchiveDataSource`

While the files located in the archive **may not** be compressed, the archive file itself can be, depending on the
archive format:
- A Zip archive is also already compressed so the compression format for `ZipArchiveDataSource` is always ZIP.
- A Tar archive can be compressed by any compression format, excluding ZIP (since it would create a Zip archive containing
the Tar archive): BZIP2, GZIP, XZ or ZSTD.

Just like `DirectoryDataSource`, the archive datasources integrate the notions of base name and data extension. If not
given as a parameter in the datasource constructor, the archive file name is even defined using the base name and the
data extension, as `<directory>/<basename>.<dataExtension>.<archiveExtension>.<compressionExtension>` with the 
compression extension being optional depending on the archive format.


## Example

Let's consider a directory containing the following files:

```java
/*
    directory              
    ├── foo              
    ├── foo.bar         
    ├── foo.xiidm.gz    
    ├── foo.v3.xiidm.gz
    ├── foo.gz         
    └── toto.xiidm.gz  
 */
```

A datasource on this directory could be used this way:

```java
// Creation of a directory datasource with compression
GzDirectoryDataSource datasource = new GzDirectoryDataSource(testDir, "foo", "xiidm", observer);

// Check if some files exist in the datasource by using the `exists(String fileName)` method
// Since the datasource uses Gzip compression, ".gz" is added to the provided fileName parameter
datasource.exists("test.toto") // Returns false: the file "test.toto.gz" does not exist in the directory
datasource.exists("foo.bar") // Returns false: the file "foo.bar.gz" does not exist
datasource.exists("foo.xiidm") // Returns true: the file "foo.xiidm.gz" exists

// Check if some files exist in the datasource by using the `exists(String fileName)` method
datasource.exists("_bar", "baz") // Returns false: the file "foo_bar.baz.gz" does not exist in the directory
datasource.exists(null, "xiidm") // Returns true: the file "foo.xiidm.gz" exists in the directory
datasource.exists(null, null) // Returns true: the file "foo.gz" exists in the directory

// We can create some a new file "foo_test.txt.gz" and write "line1" inside
try (OutputStream os = dataSource.newOutputStream("_test", "txt", false)) {
    os.write("line1".getBytes(StandardCharsets.UTF_8));
}

// Another line can be added to the same file by setting the `append` boolean parameter to true
try (OutputStream os = dataSource.newOutputStream("_test", "txt", true)) {
    os.write("line2".getBytes(StandardCharsets.UTF_8));
}

// We can read the file
try (InputStream is = dataSource.newInputStream("_test", "txt")) {
    System.out.println(ByteStreams.toByteArray(is)); // Displays "line1" then "line2"
}

// List the files in the datasource
Set<String> files = datasource.listNames(".*") // returns a set containing: "foo", "foo.bar", "foo.xiidm", "foo.v3.xiidm", "foo_test.txt"
```