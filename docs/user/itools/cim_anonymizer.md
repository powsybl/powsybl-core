---
layout: default
---

# iTools cim-anonymizer

The `cim-anonymizer` command is used to anonymize one or several CIM files. All the identifiers are replaced by new generated identifiers. The mapping between old and new identifiers is exported in a CSV file.

## Usage
```
$> itools cim-anonymizer --help
usage: itools [OPTIONS] cim-anonymizer --cim-path <PATH> [--help] --mapping-file
       <FILE> --output-dir <DIR> [--skip-external-refs]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --cim-path <PATH>       CIM zip file or directory
    --help                  display the help and quit
    --mapping-file <FILE>   File to store the ID mapping
    --output-dir <DIR>      Directory to write anonymized zip files
    --skip-external-refs    Do not anonymize external references
```

### Required arguments

**\-\-cim-path**  
This option defines the CIM file (zip) or a directory where to look for CIM files.

**\-\-mapping-file**  
This option defines the CSV file where the mapping between original and new identifiers is exported.

**\-\-output-dir**  
This option defines the path of the directory where to write the anonymized CIM files. If the output directory doesn't exist, an exception is thrown.

### Optional arguments

**\-\-skip-external-refs**  
This option defines if the XML external references should also be anonymized or not. The default value is `false`, meaning that the external references are also anonymized.
