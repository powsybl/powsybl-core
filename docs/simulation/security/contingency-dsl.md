# Contingency DSL
The contingency DSL is a domain specific language written in groovy for the creation of a contingency list, used in [security analyses](./security.md) or [sensitivity analyses](../sensitivity/sensitivity.md). At the moment, it's possible to simulate the loss of a generator, a static VAR compensator, a shunt, a power line, a power transformer, a HVDC line or a busbar section. 

## N-1 contingency
A N-1 contingency is a contingency that triggers a single equipment at a time.
```groovy
contingency('contingencyID') {
    equipments 'equipmentID'
}
```
where the `contingencyID` is the identifier of the contingency and the `equipmentID` is the identifier of a supported equipment. If the equipment doesn't exist or is not supported, an error will occur.

## N-K contingency
A N-K contingency is a contingency that triggers several equipments at a time. The syntax is the same as for the N-1 contingencies, except that you have to pass a list of equipments' IDs.
```groovy
contingency('contingencyID') {
    equipments 'equipment1', 'equipment2'
}
``` 

## Manual contingency list
A manual contingency list is a set of contingencies that are explicitly defined. In the following example, the list contains two contingencies that trigger respectively the equipment `equipment1` and `equipment2`:
```groovy
contingency('contingency1') {
    equipments 'equipment1'
}

contingency('contingency2') {
    equipments 'equipment2'
}
``` 

## Automatic contingency list
As the DSL is written in Groovy, it's possible to write more complex script. For example, it's possible to iterate over the equipments of the network to generate the contingency list. The network is accessible using the `network` variable.

The following example creates a N-1 contingency for each line of the network. We use the ID of the lines as identifier for the contingencies. 
```groovy
for (l in network.lines) {
    contingency(l.id) {
        equipments l.id
    }
}
```

It's also possible to filter the lines, for example to consider on the border lines:
```groovy
import com.powsybl.iidm.network.Country

for (l in network.lines) {
    country1 = l.terminal1.voltageLevel.substation.country
    country2 = l.terminal2.voltageLevel.substation.country
    if (country1 != country2) {
        contingency(l.id) {
            equipments l.id
        }
    }
}
```

The following example creates a list of contingencies for all 380 kV lines:
```groovy
for (l in network.lines) {
    nominalVoltage1 = l.terminal1.voltageLevel.nominalV
    nominalVoltage2 = l.terminal2.voltageLevel.nominalV
    if (nominalVoltage1 == 380 || nominalVoltage2 == 380) {
        contingency(l.id) {
            equipments l.id
        }
    }
}
```

In the following example, we use the [Stream API](https://docs.oracle.com/en/java/javase/14/docs/api/java.base/java/util/stream/package-summary.html) to create a list of contingencies with the 3 first 225 kV french lines:  
```groovy
import com.powsybl.iidm.network.Country

network.lineStream
    .filter({l -> l.terminal1.voltageLevel.substation.country == Country.FR})
    .filter({l -> l.terminal2.voltageLevel.substation.country == Country.FR})
    .filter({l -> l.terminal1.voltageLevel.nominalV == 225})
    .filter({l -> l.terminal2.voltageLevel.nominalV == 225})
    .limit(3)
    .forEach({l ->
        contingency(l.id) {
            equipments l.id
        }
    })
```

The following example creates a list of contingencies with the 3 first French nuclear generators with a maximum power greater than 1000 MW.
```groovy
import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.EnergySource

network.generatorStream
    .filter({g -> g.terminal.voltageLevel.substation.country == Country.FR})
    .filter({g -> g.energySource == EnergySource.NUCLEAR})
    .filter({g -> g.maxP > 1000})
    .limit(3)
    .forEach({g ->
        contingency(g.id) {
            equipments g.id
        }
    })
```

## Configuration
To provide contingencies list using this DSL, you have to add the following lines to your configuration file:

**YAML configuration:**
```yaml
componentDefaultConfig:
    ContingenciesProviderFactory: com.powsybl.contingency.dsl.GroovyDslContingenciesProviderFactory

groovy-dsl-contingencies:
    dsl-file: /path/to/contingencies.groovy
```

**XML configuration:**
```xml
<componentDefaultConfig>
    <ContingenciesProviderFactory>com.powsybl.contingency.dsl.GroovyDslContingenciesProviderFactory</ContingenciesProviderFactory>
</componentDefaultConfig>
<groovy-dsl-contingencies>
     <dsl-file>/path/to/contingencies.groovy</dsl-file>
</groovy-dsl-contingencies>
```

## Going further
- [Action DSL](action-dsl.md): Lean how to write scripts for security analyses with remedial actions

