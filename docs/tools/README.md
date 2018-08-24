# iTools

Powsybl, via the iTools script mechanism, provides a common way to interact with powsybl features using the command line.
 
Among predefined commands, exposing some of the framework's functionalities: convert-network, run-script, loadflow.

Powsybl iTools module is designed and provides the necessary APIs to [extend](../tutorials/itools/howto-extend-itools.md) the set of available commands with new ones.

In this tutorial you are going to see how to use itools, the powsybl command line tool.
  
In the following sections `<POWSYBL_HOME>` represents powsybl's root installation folder.  

## How to use itools
Executing `itools` without parameters will show you the help, with the available commands list: 
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
If your powsybl installation includes new and different implementations of itools commands, the list shown by itools will be updated accordingly. 
