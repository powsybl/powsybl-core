# powsybl-core configuration file(s)

powsybl-core configuration mechanism supports the following file formats: YAML, XML.

The platform will look inside `$HOME/.itools` for a YAML configuration file named `config.yml`; if not found it will try reading an XML file `config.xml`;
To specify a different configuration directory (other than the default `$HOME/.itools`), and /or specify a different configuration file name (other than the default `config`),
please ref. [this page](README.md).
Platform configuration is divided in different sections, as shown here 

### YAML config file structure
```yaml
componentDefaultConfig:
   ...
module1:
   ...
module2:
   ...

...

moduleN:
   ...   
```

### XML config file structure
```xml
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <componentDefaultConfig>
     ... 
    </componentDefaultConfig>
    <module1>
    ...
    </module1>
    <module2>
    ...
    </module2>
    ...
    <moduleN>
    ...
    </moduleN>
</config>
```

Usually, each section lists all the properties for a specific module. 
componentDefaultConfig differs from the other parts because it is very generic. 
It lists the elementary functionalities (interfaces) needed to perform a computation and the implementation chosen or available.

The generic syntax is:

### YAML componentDefaultConfig section structure
```yaml
componentDefaultConfig:
  firstInterfaceFactory: firstFactoryImplementation
  secondInterfaceFactory: secondFactoryImplementation
  ...
  lastInterfaceFactory: lastFactoryImplementation
```

### XML componentDefaultConfig section structure
```xml
<componentDefaultConfig>
  <firstInterfaceFactory>firstFactoryImplementation</firstInterfaceFactory>
  <secondInterfaceFactory>secondFactoryImplementation</secondInterfaceFactory>
  ...
  <lastInterfaceFactory>lastFactoryImplementation</lastInterfaceFactory>
</componentDefaultConfig>
```

**Important:** the other modules in the configuration are related to this first part of the configuration. 
A specific implementation may and usually requires a dedicated module configuration section. 


Example: In the configuration below, we define these functionalities:
 - A security analysis
 - A description of contingencies
 - A loadflow
         
The chosen implementations are:
 - "slow" security analysis (for a few contingencies), post-contingency LF based implementation
 - the contingencies expressed in Groovy DSL language
 - the 'mock' loadflow (a loadflow implementation that does nothing on the network: for demonstration purposes, only)
 
The same configuration expressed in the two file formats (note that here no specific modules configurations are defined):

## YAML
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory
    SecurityAnalysisFactory: com.powsybl.security.SecurityAnalysisFactoryImpl
    LoadFlowFactory: com.powsybl.loadflow.mock.LoadFlowFactoryMock

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
</config>
```

