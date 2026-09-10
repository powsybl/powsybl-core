# Configuration

```{toctree}
---
maxdepth: 1
hidden: true
---
modules.md
```

The configuration mechanism supports YAML and XML file formats. The framework looks inside all the folders specified to the [powsybl_config_dirs](../itools/index.md#configuration) property in the [itools.conf](../itools/index.md#configuration) file for configuration files. The framework uses the [powsybl_config_name](../itools/index.md#configuration) property as the basename of the configuration files. It looks for a YAML file first, then for an XML file. The XML file will be used only if the YAML configuration file has not been found.

The configuration can also be configured using the system's environment variables. These variables should respect the
following format: `MODULE_NAME__PROPERTY_NAME`. Note that these variables will overload the XML/YAML configuration files.

The default configuration folder and the configuration file name can be configured in the `POWSYBL_HOME/etc/itools.conf`.

## Properties
The configuration file contains a list of modules that can be required or optional. Each module contains one or
several properties. These properties can also be required or optional. Names in configuration files are case-sensitive.

### Example

**YAML configuration**
```yaml
module1:
    property1a: value1
    property1b: value2

module2:
    property2a: value3
    property2b: value4
    property2c: value5
```

**XML configuration**
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

### System's environment variables
Configuration properties can also be overridden using system's environment variables. The module and the property are separated using two underscores. The table below gives examples on the way to declare environment variables for PowSyBl:

| Environment variable      | Module name  | Property name |
|---------------------------|--------------|---------------|
| MODULE1__PROPERTY1=1      | module1      | property1     |
| LOWER_HYPHEN__PROPERTY2=2 | lower-hyphen | property2     |
| LOWER_CAMEL__PROPERTY3=3  | lowerCamel   | property3     |
| UPPER_CAMEL__PROPERTY4=4  | UpperCamel   | property4     |
| SNAKE_CASE__PROPERTY5=5   | snake_case   | property5     |

This mechanism *replaces* a whole property from outside the configuration file, whatever its format (XML or YAML). To instead *inject* an environment variable inside a value of a YAML file, see [Environment variables inside YAML values](#environment-variables-inside-yaml-values) below.

### Environment variables inside YAML values
Unlike the [System's environment variables](#systems-environment-variables) mechanism above, which overrides a whole property from outside the file, the `${VAR}` syntax lets you reference an environment variable *within* a value. It applies only to YAML files.

In a YAML configuration file, values can also reference environment variables using the `${VAR}` syntax. A default value
can be provided with `${VAR:-default}`, which is used when the variable is not set. A placeholder whose variable is not
set and which has no default is left unchanged. The substitution is performed while the file is loaded, before values
are parsed, so it applies to every property type (integer, boolean, path, list, ...). To use an environment variable in
a non-string value, quote it so that YAML keeps it as text:

```yaml
module1:
    directory: ${MY_HOME}/data
    max-iterations: "${MAX_ITERATIONS:-30}"
```

With `MY_HOME=/opt/powsybl` and `MAX_ITERATIONS` unset, `directory` resolves to `/opt/powsybl/data` and
`max-iterations` falls back to `30`.

#### Referencing the configuration file location
In addition to environment variables, the reserved placeholder `${config_dir}` resolves to the absolute path of the
directory containing the YAML configuration file. It allows resources to be located relatively to the configuration
file, independently of the working directory. It takes precedence over an environment variable of the same name.

```yaml
module1:
    my-resource: ${config_dir}/resources/data.txt
```

If the configuration file is `/etc/powsybl/config.yml`, then `my-resource` resolves to
`/etc/powsybl/resources/data.txt`.

## Modules

The module list is available [here](modules.md).
