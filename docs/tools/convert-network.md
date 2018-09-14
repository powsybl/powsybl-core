# iTools convert-network command

itools `convert-network` command allows you to convert a network, imported from a case file, to a specified output format, exporting it to a file.  
In powsyble-core, supported output formats are:
- XIIDM:  XML serialization of IIDM (iTesla Internal Data Model) format
- AMPL: serialization of AMPL (A Mathematical Programming Language) format

## Running convert-network command 
Following is an example of how to use the `convert-network` command.    
  
To show the command help, with its specific parameters and descriptions, enter: 

```shell
$> cd <POWSYBL_HOME>/bin
$>  ./itools  convert-network --help
usage: itools [OPTIONS] convert-network [-E <property=value>]
       [--export-parameters <EXPORT_PARAMETERS>] [--groovy-script <FILE>]
       [--help] [-I <property=value>] [--import-parameters <IMPORT_PARAMETERS>]
       --input-file <INPUT_FILE> --output-file <OUTPUT_FILE> --output-format
       <OUTPUT_FORMAT>

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
 -E <property=value>                          use value for given exporter
                                              parameter
    --export-parameters <EXPORT_PARAMETERS>   the exporter configuration file
    --groovy-script <FILE>                    Groovy script to change the
                                              network
    --help                                    display the help and quit
 -I <property=value>                          use value for given importer
                                              parameter
    --import-parameters <IMPORT_PARAMETERS>   the importer configuation file
    --input-file <INPUT_FILE>                 the input file
    --output-file <OUTPUT_FILE>               the output file
    --output-format <OUTPUT_FORMAT>           the output file format

Where OUTPUT_FORMAT is one of [XIIDM, AMPL]

```

In order to run the `convert-network` command, you have to provide at least the 3 required arguments: 
- `input-file`: path of the input network file to convert
- `output-format`: allowed values are XIIDM, AMPL
- `output-file`: file where to store the converted network

In the following example, we will see how to convert an UCTE network model to XIIDM format.  
Please change the parameters in the command below to reflect your development/installation scenario.

```shell
$> cd <POWSYBL_HOME>/bin
$>  ./itools convert-network --input-file $HOME/case-file.uct  --output-format XIIDM --output-file $HOME/case-file.xiidm
```

After running the command, you will find in your `$HOME` folder a case-file.xiidm file, containing the XML IIDM serialization of the input network. 