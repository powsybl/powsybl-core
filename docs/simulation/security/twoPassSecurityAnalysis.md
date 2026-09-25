# Two-pass security analysis

The two-pass security analysis is intended for studies where a single security analysis engine is not the best choice for all contingencies.

Its purpose is to combine two complementary analyses:

- a first pass that quickly evaluates the full contingency list, using a fast security analysis engine
- an analysis of the security analysis results from the first pass paired with a decision criterion to select contingencies to be sent to the second pass
- a second pass that re-evaluates only the contingencies that need additional attention, using a more detailed security analysis engine

This approach helps keep a broad security study efficient while reserving the more specific or more expensive analysis for the situations where it is really useful.

## When to use it

The two-pass security analysis is useful when:

- most contingencies can be handled correctly by a first security analysis provider
- only a limited number of contingencies require a more advanced or alternative provider
- you want to reduce the total computation effort compared with running the most detailed analysis on the whole contingency list

Typical use cases include studies where the first pass is used as a large-scale screening stage, and the second pass is used as a refinement stage on a restricted subset of post-contingency situations.

## Principle

The analysis is carried out in three successive stages.

### First pass

The first provider is run on the complete contingency list.

This first pass produces the usual security analysis results and is used to identify the contingencies that should be reviewed more carefully.

### Analysis and decision criterion

The security analysis results from the first pass are analysed to produce quantitites used in a decision criterion. 
This decision criterion decides which contingencies are sent forward to the second pass.
Currently, a contingency is selected for the second pass when the first pass:

- does not converge
- or changes a phase tap changer during the computation

### Second pass

The second provider is run only on contingencies selected from the first pass.

The final result keeps the first-pass result for the contingencies that do not need re-evaluation, and replaces it with the second-pass result for the selected contingencies.

## Benefits

The two-pass approach makes it possible to:

- run a first screening on all contingencies with a provider suited to large volumes
- focus the second analysis only on the contingencies that are sensitive or harder to solve
- combine performance and robustness in the same study workflow

## Configuration

To use the two-pass security analysis, two levels of configuration are required:

- select `TwoPassSecurityAnalysis` as the security analysis provider
- declare the providers to use for the first and second passes

### Selecting the provider

If several security analysis implementations are available on the classpath, configure the provider selection accordingly.

**YAML configuration:**

```yaml
componentDefaultConfig:
  SecurityAnalysisProvider: TwoPassSecurityAnalysis
```

**XML configuration:**

```xml
<componentDefaultConfig>
    <SecurityAnalysisProvider>TwoPassSecurityAnalysis</SecurityAnalysisProvider>
</componentDefaultConfig>
```

### Declaring the two-pass parameters

The dedicated configuration module is named `twopass-security-analysis-parameters`.

It defines:

- `firstProviderName`: provider used for the first pass
- `secondProviderName`: provider used for the second pass

**YAML configuration:**

```yaml
twopass-security-analysis-parameters:
  firstProviderName: LoadFlow
  secondProviderName: DynaFlow
```

**XML configuration:**

```xml
<twopass-security-analysis-parameters>
    <firstProviderName>LoadFlow</firstProviderName>
    <secondProviderName>DynaFlow</secondProviderName>
</twopass-security-analysis-parameters>
```

The provider names must match the security analysis implementations available in the execution environment.

## In practice

In a typical workflow, the user prepares:

- the network to analyse
- the contingency list
- the standard security analysis parameters
- the two providers to combine in the two-pass configuration

The two-pass analysis then returns a single security analysis result, while internally using the second provider only for the contingencies identified during the first pass.

## Going further

- [Security analysis](index.md): overview of security analysis inputs, outputs and workflow
- [Configuration](configuration.md): general security analysis configuration parameters
- [Contingency DSL](contingency-dsl.md): how to define the contingency list used by the analysis