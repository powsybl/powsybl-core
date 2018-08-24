# powsybl-core configuration file(s)

powsybl-core configuration mechanism supports multiple file formats: YAML, XML and properties files.
YAML and XML are both single-file formats (i.e. all the platform modules configurations are declared in the same file), 
whereas each module's configuration is in a separate file, in case of properties format.

The platform will look inside `$HOME/.itools` for a YAML configuration file named `config.yml`; if not found it will try reading an XML file `config.xml`; otherwise it will try using properties files, one for each module, named `<MODULE_NAME>.properties`.
To specify a different configuration directory (other than the default `$HOME/.itools`), and /or  specify a different configuration file name (other than the default `config`),
please ref. [this page](README.md).

Platform configuration is divided in different sections. Each section includes all the specific properties for the module. 
Example (same configuration) for the three, different, file formats:

## YAML
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
    SecurityAnalysisFactory: com.powsybl.security.SecurityAnalysisFactoryImpl
    LoadFlowFactory: com.powsybl.loadflow.mock.LoadFlowFactoryMock

import:
    postProcessors:
```

## XML
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
<componentDefaultConfig>
<ContingenciesProviderFactory>com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
    <SecurityAnalysisFactory>com.powsybl.security.SecurityAnalysisFactoryImpl</SecurityAnalysisFactory>
    <LoadFlowFactory>com.powsybl.loadflow.mock.LoadFlowFactoryMock</LoadFlowFactory>
  </componentDefaultConfig>

  <import>
    <postProcessors></postProcessors>
  </import>

</config>
```

## Properties

### componentDefaultConfig.properties 
```
ContingenciesProviderFactory=com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory

SecurityAnalysisFactory=com.powsybl.security.SecurityAnalysisFactoryImpl

LoadFlowFactory=com.powsybl.loadflow.mock.LoadFlowFactoryMock
```

### import.properties
```
postProcessors=
```


# The componentDefaultConfig section




