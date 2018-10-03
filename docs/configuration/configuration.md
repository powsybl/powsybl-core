# Configuration - Module configuration

The configuration mechanism supports YAML and XML file formats. The framework will look inside `$HOME/.itools` for the `config.yml`
YAML configuration file. The `config.xml` XML file will be used only if the YAML configuration file has not been found.

The default configuration folder and the configuration file name can be [configured](itools.md) in the `POWSYBL_HOME/etc/itools.conf`.

## Modules and properties

The configuration file contains a list of modules, that can be required or optional. Each module contains one or
several properties. These properties can also be required or optional.

### Example
```yml
module1:
    property1a: value1
    property1b: value2
    
module2:
    property2a: value3
    property2b: value4
    property2c: value5
```

```xml
<config>
    <module1>
        <property1a>value1</property1a>
        <property1b>value2</property1b>
    </module1>
    <module1>
        <property2a>value3</property2a>
        <property2b>value4</property2b>
        <property2c>value5</property2c>
    </module1>
</config>
```

## Modules list:
- [componentDefaultConfig](modules/componentDefaultConfig.md)
- [computation-local](modules/computation-local.md)
- [default-computation-manager](modules/default-computation-manager.md)
- [external-security-analysis-config](modules/external-security-analysis-config.md)
- [groovy-dsl-contingencies](modules/groovy-dsl-contingencies.md)
- [groovy-post-processor](modules/groovy-post-processor.md)
- [import](modules/import.md)
- [import-export-parameters-default-value](modules/import-export-parameters-default-value.md)
- [javaScriptPostProcessor](modules/javaScriptPostProcessor.md)
- [limit-violation-default-filter](modules/limit-violation-default-filter.md)
- [load-flow-action-simulator](modules/load-flow-action-simulator.md)
- [load-flow-based-phase-shifter-optimizer](modules/load-flow-based-phase-shifter-optimizer.md)
- [load-flow-default-parameters](modules/load-flow-default-parameters.md)
- [loadflow-results-completion-parameters](modules/loadflow-results-completion-parameters.md)
- [loadflow-validation](modules/loadflow-validation.md)
- [local-app-file-system](modules/local-app-file-system.md)
- [mapdb-app-file-system](modules/mapdb-app-file-system.md)
- [security](modules/security.md)
- [simulation-parameters](modules/simulation-parameters.md)
- [table-formatter](modules/table-formatter.md)

