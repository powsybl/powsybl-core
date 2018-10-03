# loadflowResultsCompletion
The `loadflowResultsCompletion` import post processor (`com.powsybl.loadflow.LoadFlowResultsCompletionPostProcessor` class) aims at completing the loadflow results of a network, computing and assigning, if not already set, the flows at the end of branches.  
The post processor uses Kirchhoff laws, and the estimation of active and reactive power is computed according to the voltages and the characteristics of the branch:

```
(P1calc, Q1calc, P2calc, Q2calc) = f(Voltages, Characteristics)  
```

Please see `com.powsybl.iidm.network.util.BranchData` class for more details. 
  
In order to run this post processor after the import of a network, add `loadflowResultsCompletion` to the list of post processors to be run, in the `postProcessors` tag of the [`import` section](../../../configuration/modules/import.md), in your [configuration file](../../../configuration/configuration.md)  

### YAML
```yaml
import:
    postProcessors: loadflowResultsCompletion
```

### XML
```xml
<import>
    <postProcessors>loadflowResultsCompletion</postProcessors>
</import>
```

The parameters of the post processor can be [configured](../../../configuration/modules/loadflow-results-completion-parameters.md) in the [configuration file](../../../configuration/configuration.md).