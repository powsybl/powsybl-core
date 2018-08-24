# loadflowResultsCompletion
The `loadflowResultsCompletion` import post processor (`com.powsybl.loadflow.LoadFlowResultsCompletionPostProcessor` class) aims at completing the loadflow results of a network, computing and assigning, if not already available, the flows at the end of branches.  
The post processor uses Kirchhoff laws, and the estimation of active and reactive power is computed according to the voltages and the characteristics of the branch:

```
(P1calc, Q1calc, P2calc, Q2calc) = f(Voltages, Characteristics)  
```

Please see `com.powsybl.iidm.network.util.BranchData` class for more details. 
  
In order to run this post processor after the import of a network, add `loadflowResultsCompletion` to the list of post processors to be run, in the `postProcessors` tag of the `import` section, in your [configuration file](../configuration/configuration.md)  

```xml
<import>
    <postProcessors>loadflowResultsCompletion</postProcessors>
</import>
```

Some parameters of the post processor can be configured in the [configuration file](../configuration/configuration.md), in the `loadflow-results-completion-parameters` section

```xml
<loadflow-results-completion-parameters>
    <epsilon-x>0.1</epsilon-x>
    <apply-reactance-correction>false</apply-reactance-correction>
</loadflow-results-completion-parameters>   
```

The available parameters are:
* *epsilon-x*: value used to correct the reactance in flows computation, used only if apply-reactance-correction is true; default value is 0.1
* *apply-reactance-correction*: apply reactance correction in flows computation; default value is false