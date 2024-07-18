(datasources)=
# Datasources

## Principles

Datasources are Java-objects used to facilitate I/O operations around PowSyBl.
It allows users to read and write files 


## Types of datasources

Multiple types of datasources exist, depending on whether it shall be writable or not, the kind of storage used, 
data location, data compression, etc.


(readonlydatasources)=
### ReadOnlyDataSource

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
### DirectoryDataSource

