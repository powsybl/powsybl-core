# powsybl configuration

Powsybl-core implements a highly modular configuration mechanism. 

Being [\<POWSYBL_HOME\>](directoryList.md) the powsybl root installation directory,  `<POWSYBL_HOME>`/etc/itools.conf declares the actual configuration files' parent folders and names. 

`<POWSYBL_HOME>/etc/itools.conf` example:
```properties
# configuration's file parent directory
itools_config_dir=<CONFIG_DIR>
# configuration's file basename
itools_config_name=<CONFIG_NAME>
# powsybl's cache path (for modules requiring cache functionalities)
itools_cache_dir=<CACHE_DIR>
```

If `<POWSYBL_HOME>/etc/itools.conf` does not exist, or some properties are not defined, the default values are:

| property | default value |
| -------- | ------------- |
| itools_config_dir | `$HOME/.itools`|
| itools_config_name | `config` |
| itools_cache_dir | `$HOME/.cache/itools` |


You can also set or override `itools_config_name` at the itools command line, by setting the `--config-name` parameter (ref. [itools](../tools/README.md)) 


## Powsybl configuration file
[Configuring powsybl](configuration.md) explains how to configure the framework's modules. 

## Logging configuration file
[Configuring logging](logger.md) explains how to customize the platform's logger.
