# groovy-dsl-contingencies
The `groovy-dsl-contingencies` module is used by the `com.powsybl.action.dsl.GroovyDslContingenciesProviderFactory`, which is an implementation of the `com.powsybl.contingency.ContingenciesProviderFactory` used by the [security-analysis](../itools/security-analysis.md) command.

```{warning}
Groovy scripts are executed without any sandbox. Only use scripts from trusted sources. See [Groovy scripts security](../groovy-security.md).
```

## Required properties

**dsl-file**<br>
The `dsl-file` property is a required property that defines the path of the groovy script defining the list of contingencies to simulate. Read the [documentation](../../simulation/security/contingency-dsl.md) page to learn more about the syntax of the `GroovyDslContingenciesProvider`.

## Examples

**YAML configuration:**
```yaml
groovy-dsl-contingencies:
    dsl-file: /home/user/contingencies.groovy
```

**XML configuration:**
```xml
<groovy-dsl-contingencies>
    <dsl-file>/home/user/contingencies.groovy</dsl-file>
</groovy-dsl-contingencies>
```

