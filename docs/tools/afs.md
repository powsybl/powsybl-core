# iTools afs command

itools `afs` command performs some basic actions on a configured [AFS (Application File System)](../../afs/README.md) instance. In the specifics,
- it can list the contents of an AFS folder:
- it can archive/restore an AFS content from/to a directory for backup purposes.

## Running afs command 
*Note:* In the following sections [\<POWSYBL_HOME\>](../configuration/directoryList.md) represents powsybl's root installation folder.

To show the command help, with its specific parameters and descriptions, enter:

```shell
$> cd <POWSYBL_HOME>/bin
$>  ./itools  afs --help
usage: itools [OPTIONS] afs [--archive <FILE_SYSTEM_NAME>] [--dir <DIR>]
       [--help] [--ls <PATH>] [--unarchive <FILE_SYSTEM_NAME>]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --archive <FILE_SYSTEM_NAME>     archive file system
    --dir <DIR>                      directory
    --help                           display the help and quit
    --ls <PATH>                      list files
    --unarchive <FILE_SYSTEM_NAME>   unarchive file system

```

- `<FILE_SYSTEM_NAME>` is an AFS `drive-name`, as described in [AFS (Application File System)](../../afs/README.md) and configured in  [powsybl configuration file](../configuration/configuration.md)
- `<PATH>` is an AFS filesystem path (*Note:* to refer a specific AFS, use a `drive-name:/` prefix in the path)
- `<DIR>` is local filesystem path

## Examples
Here below we assume that an afs named `my-first-fs` is configured and populated as described in [AFS (Application File System)](../../afs/README.md), section *Using AFS from groovy scripts*.

### Example 1: list an afs root folder contents
```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools afs --ls "my-first-fs"
my-first-folder
```

### Example 2: list an afs specific folder contents
```shell
$> cd <POWSYBL_HOME>/bin
$> ./itools afs --ls "my-first-fs:/my-first-folder"
my-first-project
```

### Example 3: export the afs content to a directory
The target directory, e.g. `/tmp/archive_my_first_fs` must exist

```shell 
$> cd <POWSYBL_HOME>/bin
$> mkdir /tmp/archive_demo
$> ./itools afs --archive my-first-fs --dir ./archive_my_first_fs
```
*Note*: The `archive` command exports both the afs' contents and its structure. 

### Example 4: restore an afs with a previously archived content directory
Let's assume `/tmp/archive_my_first_fs` contains an afs archived content, previously created using the `--archive` argument (ref. Example3, above)
```shell 
$> cd <POWSYBL_HOME>/bin
$> ./itools afs --unarchive my-first-fs --dir ./tmp/archive_my_first_fs
```
*Note*: the command fails when the content to restore already exist in the target afs.

