# itools

powsybl-core, via the itools script mechanism, provides a common way to interact with PowSyBl using the command line.  
Among predefined commands, exposing some of the framework's functionalities: convert-network, run-script, loadflow.  
powsybl-core itools module is designed and provides the necessary APIs to extend the set of available commands with new ones.  
  
In this tutorial you are going to see how to work with itools, the powsybl command line tool, learning:
 - How to use itools
 - [How to extend itools for your needs](extend-itools.md)
  
In the following sections `<POWSYBL_HOME>` represents powsybl's root installation folder.  

## How to use itools
Executing *itools* without parameters will show you the help, with the available commands list: 

```
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
| ------ | ------ | ----------- |
| Application file system | afs | Application File System command line tool |
| Computation | action-simulator | Action simulator |
| Computation | [loadflow](../loadflow/loadflow-command.md) | Run loadflow |
| Computation | loadflow-validation | Validate load-flow results of a network |
| Computation | run-impact-analysis | Run impact analysis |
| Computation | security-analysis | Run security analysis |
| Data conversion | [convert-network](../converter/convert-network-command.md) | Convert a network from one format to another |
| MPI statistics | export-tasks-statistics | Export tasks statistics to CSV |
| Script | [run-script](run-script-command.md) | Run a script (only Groovy is supported, so far) |

Commands in the list are classified in *themes*, to help identifying their purpose.  
If your powsybl installation includes new and different implementations of itools commands, the list shown by itools will be updated accordingly. 
