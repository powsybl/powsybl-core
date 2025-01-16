# iTools list-dynamic-simulation-models

The `list-dynamic-simulation-models` command lists all models used by the [time domain](../../simulation/dynamic/index.md) simulation for a given provider.

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

By default, all models are displayed, but you can use one of the following options to display specific models:
- `--dynamic-models`: allows to display dynamic models only.

- `--event-models`: allows to display event models only.

## See also
- [Run a dynamic simulation through an iTools command](../../user/itools/dynamic-simulation.md): learn how to perform a dynamic simulation from the command line.
