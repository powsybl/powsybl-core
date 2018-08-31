# iTools: the powsybl command line tool.

The **itools** script mechanism provides a common way to interact with powsybl, using the command line.

Here below it is described how to use **itools** and what are the framework's functionalities currently exposed through it (e.g. *convert-network*, *loadflow*, *run-script*, etc. )  

## How to use itools

`itools` commands are structured as shown below:

```
itools command-name [--argument1-name argument1-value --argument2-name argument2-value ...]
```

Not all commands have arguments. The arguments can be mandatory or optional.
Depending on the command and how the arguments are set, its output can be written to files, to the standard output or both.

Executing `itools` without parameters will show you a list of the available commands with a short description for each command.

*Note:* In the following sections [\<POWSYBL_HOME\>](../configuration/directoryList.md) represents powsybl's root installation folder.  
 
```bash
$> cd <POWSYBL_HOME>/bin
$> ./itools
usage: itools [OPTIONS] COMMAND [ARGS]
```

### itools options
Available options are:

| Option | Description |
| ------ | ----------- |
| --config-name <CONFIG_NAME> | Override configuration file name| 
| --parallel | Run command in parallel mode 

### itools commands
The list of commands currently implemented and available in powsybl-core is:  
  
| Theme | Command | Description |
| ----- | ------- | ----------- |
| Application file system | [afs](afs.md) | Application File System command line tool |
| Computation | [action-simulator](action-simulator.md) | Action simulator |
| Computation | [loadflow](loadflow.md) | Run loadflow |
| Computation | [loadflow-validation](loadflow-validation.md) | Validate load-flow results of a network |
| Computation | [run-impact-analysis](run-impact-analysis.md) | Run impact analysis |
| Computation | [security-analysis](security-analysis.md) | Run security analysis |
| Computation | [sensitivity-computation](sensitivity.md) | Run sensitivity computation |
| Data conversion | [convert-network](convert-network.md) | Convert a network from one format to another |
| MPI statistics | [export-tasks-statistics](export-tasks-statistics.md) | Export tasks statistics to CSV |
| Script | [run-script](run-script.md) | Run a script (only Groovy is supported, so far) |

Commands in the list are classified in *themes*, to help identifying their purpose.  
*Note:* If your powsybl installation includes new and different implementations of itools commands, the list shown by itools will be updated accordingly. 

Executing `itools command-name --help` will list all the arguments available for the specific command-name. 


## How to add new itools commands

Powsybl iTools module is designed to be extensible and provides the necessary APIs to [add new commands](../tutorials/itools/howto-extend-itools.md) to the available list.

