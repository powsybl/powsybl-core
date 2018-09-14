# iTools run-script command

itools `run-script` command allows you to run scripts (only Groovy is supported, so far).

## Running run-script command 
Following is an example of how to use the `run-script` command.    

In this tutorial the installation directory and the sources directories are supposed to be, respectively, the default $HOME/powsybl, and $HOME/powsybl-core.

To show the command help, with its specific parameters and descriptions, enter: 

```shell
$> cd $HOME/powsybl/bin
$> ./itools run-script --help
usage: itools [OPTIONS] run-script --file <FILE> [--help]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name
    --parallel                    Run command in parallel mode

Available arguments are:
    --file <FILE>   the script file
    --help          display the help and quit
```

In order to run the `run-script` command, you have to provide as input the required argument: 
- `file`: Groovy script to run  
  
Create into the `tmp` folder a custom Groovy script: `helloworld.groovy`, with the following content:

```
println('Hello world, this is a Groovy script!')
```
    
Run itools `run-script` command, using the helloworld.groovy script as argument:

```shell
$> cd $HOME/powsybl/bin
$>  ./itools run-script --file /tmp/helloworld.groovy 
```

The following line will be printed in your screen:

```shell
Hello world, this is a Groovy script!
```
