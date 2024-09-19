(datasources)=
# Datasources

## Principles

Datasources are Java-objects used to facilitate I/O operations around PowSyBl.
It allows users to read and write files. It is for example used under the hood by Importers to access the filesystem
during Network imports when using `Network.read()` methods.

For importers and exporters, datasources are used to access files corresponding to a single network

## Types of datasources

Multiple types of datasources exist, depending on whether it shall be writable or not, the kind of storage used, 
data location, data compression, etc.


(readonlydatasources)=
### Read-Only DataSource

`ReadOnlyDataSource` is the most basic datasource interface available. As you can tell by the name, it only provides 
reading features.
It has two parameters:
- a base name (corresponding to the common prefix in the names of multiple related files the user wants to 
consider in the datasource),
- (optionally) a data extension, mainly used to disambiguate identically named data of different type. 
Note: this does not apply to compression extensions.

_**Example:**
For a file named `europe.west.xiidm.gz`, the base name could be `europe.west` for instance (or `europe` or `europe.w` or ...), while the data extension would be `xiidm`._

The main methods `ReadOnlyDataSource` provides are:

- `exists(String fileName)` and `exists(String suffix, String ext)` to check if a file exists in the datasource
- `newInputStream(String fileName)` and `newInputStream(String suffix, String ext)` to read a file from the datasource
- `listNames(String regex)` to list the files in the datasource whose names match the regex

The methods with `String suffix, String ext` as parameters look for a file which name will be constructed as
`<basename><suffix>.<ext>`.

The classes inheriting directly `ReadOnlyDataSource` are:
- `ResourceDataSource`: datasource based on a list of java classpath resources
- `ReadOnlyMemDataSource`: datasource where data is stored in a `Map<filename, data as bytes>` in memory
- `MultipleReadOnlyDataSource`: datasource grouping multiple user-defined datasources
- `GenericReadOnlyDataSource`: datasource used to read data from any known compressed format

(writabledatasources)=
### DataSource

The `DataSource` interface extends `ReadOnlyDataSource` by adding writing features through the methods 
`newOutputStream(String fileName, boolean append)` and `newOutputStream(String suffix, String ext, boolean append)`.
Those methods allow the user to write in a new file (if `append==false`) or at the end of an existing one (if 
`append==true`).

This interface also provides two static convenience methods (`fromPath(Path file)` and
`fromPath(Path directory, String fileNameOrBaseName)`) for the different use cases like reading data from the local
filesystem, and ensuring that the target file exists. These methods have their opposite in the class `Exporters`
named `createDataSource(Path file)` and used to write data on the local filesystem, while ensuring that the target file
given as parameter is not a directory. All those methods then make use of `DataSourceUtil.createDataSource` to build
the datasource.

Two classes implement the `DataSource` interface:
- `MemDataSource`: extension of `ReadOnlyMemDataSource` implementing the writing features of `DataSource`
- `AbstractFileSystemDataSource`: abstract class used to define datasources based on files present in the file system,
either directly or in an archive.

(directorydatasources)=
### Directory DataSource

`DirectoryDataSource` are datasources based on files located in a specific directory directly in the file system. 

Files stored and used via this type of datasource may be all compressed or not at all. Compression formats available are
defined in the class `CompressionFormat`. As of today, the following single-file compressions are available:
BZIP2, GZIP, XZ and ZSTD. Each one of those compression format has a corresponding datasource class inheriting
`DirectoryDataSource`: `Bzip2DirectoryDataSource`, `GzDirectoryDataSource`, `XZDirectoryDataSource`,
`ZstdDirectoryDataSource`.

`DirectoryDataSource` integrates the notions of base name and data extension:
- The base name is used to access files that all start with the same String. For example, `network` would
be a good base name if your files are `network.xiidm`, `network_mapping.csv`, etc.
- The data extension is the last extension of your main files, excluding the compression extension if they have one.
It usually corresponds to the data format extension: `csv`, `xml`, `json`, `xiidm`, etc. This extension is mainly used
to disambiguate the files to use in the datasource, for example when you have files that differ only by the data
extension (e.g. `network.xiidm` and `network.xml` in the same folder representing two different networks). 

Even if `DirectoryDataSource` integrates the notions of base name and data extension in the methods with
`(String suffix, String ext)` as parameters, you still have the possibility to use files that do not correspond to the 
base name and data extension by using the methods with `(String filename)` as parameter, excluding the compression 
extension if there is one.

(archivedatasources)=
### Archive DataSource

`AbstractArchiveDataSource` are datasources based on files located in a specific archive, in the file system. As of today,
two classes implements `AbstractArchiveDataSource`: `ZipArchiveDataSource` and `TarArchiveDataSource`

While the files located in the archive **have to be uncompressed**, the archive file itself can be compressed, depending
on the archive format:
- A Zip archive is also already compressed so the compression format for `ZipArchiveDataSource` is always ZIP.
- A Tar archive can be compressed by: BZIP2, GZIP, XZ or ZSTD. It can also not be compressed.

Just like `DirectoryDataSource`, the archive datasources integrate the notions of base name and data extension. If not
given as a parameter in the datasource constructor, the archive file name is even defined using the base name and the
data extension, as `<directory>/<basename>.<dataExtension>.<archiveExtension>.<compressionExtension>` with the 
compression extension being optional depending on the archive format. For example `network.xiidm.zip` contains
`network.xiidm`.


## Example

Let's consider a directory containing the following files:

```
directory              
├── network              
├── network.south              
├── network.xiidm.gz    
├── network.v3.xiidm.gz
├── network_mapping.csv.gz
├── network.gz         
└── toto.xiidm.gz  
```

A datasource on this directory could be used this way:

```java
// Creation of a directory datasource with compression
GzDirectoryDataSource datasource = new GzDirectoryDataSource(testDir, "network", "xiidm", observer);

// Check if some files exist in the datasource by using the `exists(String fileName)` method
// Since the datasource uses Gzip compression, ".gz" is added to the provided fileName parameter
datasource.exists("test.toto") // Returns false: the file "test.toto.gz" does not exist in the directory
datasource.exists("network.south") // Returns false: the file "network.south.gz" does not exist
datasource.exists("network.xiidm") // Returns true: the file "network.xiidm.gz" exists

// Check if some files exist in the datasource by using the `exists(String fileName)` method
datasource.exists("_south", "reduced") // Returns false: the file "network_south.reduced.gz" does not exist in the directory
datasource.exists(null, "xiidm") // Returns true: the file "network.xiidm.gz" exists in the directory
datasource.exists("_mapping", "csv") // Returns true: the file "network_mapping.csv.gz" exists in the directory

// We can create some a new file "network_test.txt.gz" and write "line1" inside
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
Set<String> files = datasource.listNames(".*") // returns a set containing: "network", "network.south", "network.xiidm", "network.v3.xiidm", "network_test.txt"
```