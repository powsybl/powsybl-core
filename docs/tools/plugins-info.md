# iTools plugins-info command

Powsybl modular design easily allows extending the platform with new functionalities (e.g. new IIDM importer [post-processors](../architecture/iidm/post-processor/README.md) ).  
Sometimes it is needed to specify some functionality to use, either declaring their IDs in a [configuration](../configuration/configuration.md) file or as some [itools](README.md) command line's parameters:  
the `plugins-info` command can be used to retrieve and display the currently available components IDs.   
                                        
To run the `plugins-info` command, enter:

```bash
$> cd <POWSYBL_HOME>/bin
$> ./itools plugins-info
```

The command's output shows, for each kind of `plugin`, the currently available implementations IDs.  

Output example: 

```bash
Plugins:
+-----------------------+-----------------------------------------------------+
| Plugin type name      | Available plugin IDs                                |
+-----------------------+-----------------------------------------------------+
| exporter              | AMPL, XIIDM                                         |
| import-post-processor | groovyScript, javaScript, loadflowResultsCompletion |
| importer              | CIM1, UCTE, XIIDM                                   |
+-----------------------+-----------------------------------------------------+
```

<!-- MRA: OK for me -->

