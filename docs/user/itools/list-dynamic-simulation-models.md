# iTools list-dynamic-simulation-models

The `list-dynamic-simulation-models` command list all models used by the [time domain](../../simulation/dynamic/index.md) simulation for a given provider.

## Usage
```
usage: itools [OPTIONS] list-dynamic-simulation-models [--dynamic-models]
       [--event-models] [--help]

Available options are:
    --config-name <CONFIG_NAME>   Override configuration file name

Available arguments are:
    --dynamic-models   display implemented dynamic models
    --event-models     display implemented event models
    --help             display the help and quit

```

### Optional options

`--dynamic-models`  
This option allows to display dynamic models only. (by default all models will be displayed)

`--event-models`  
This option allows to display event models only. (by default all models will be displayed)

## See also
- [Run a dynamic simulation through an iTools command](../../user/itools/dynamic-simulation.md): Learn how to perform a dynamic simulation from the command line.