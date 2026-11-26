(groovy-security)=
# Groovy scripts security

Several PowSyBl features evaluate [Groovy](https://groovy-lang.org/) scripts:
- the [run-script](itools/run-script.md) command and the `GroovyScripts` helpers,
- the [Groovy post-processor](../grid_features/import_post_processor.md#groovy-post-processor),
- the [contingency DSL](../simulation/security/contingency-dsl.md) and the [action DSL](../simulation/security/action-dsl.md),
- the [dynamic simulation](../simulation/dynamic/index.md) models, events and output variables suppliers,
- the [calculated time series DSL](../data/timeseries.md).

```{warning}
PowSyBl does **not** sandbox the execution of Groovy scripts. A script is evaluated as-is, without any `SecureASTCustomizer`,
class restriction or method blacklist, and has the same privileges as the JVM running it: it can read and write files,
open network connections, execute system commands, or stop the JVM.
```

## Why PowSyBl does not provide a sandbox
- PowSyBl is a **library**, not an application. It does not know the trust model, the deployment environment or the threat
  model of the applications embedding it, so sandboxing script execution is out of its perimeter.
- Restricting Groovy with a `SecureASTCustomizer` (or any similar blacklist/whitelist at compilation level) does not give
  a reliable protection: Groovy is highly dynamic (reflection, metaprogramming, dynamic dispatch, string-built calls, etc.) and such
  restrictions can be easily bypassed. A false sense of security is worse than no protection.

## Recommendations for applications
- Only execute scripts coming from **trusted sources**, and protect the script files and the configuration files that reference them
  against unauthorized modification.
- Never evaluate scripts received from untrusted users (for instance through a web service) without isolation.
- If untrusted scripts must be supported, sandbox them at the application or infrastructure level: run them in a dedicated
  process, container or virtual machine with minimal privileges, no access to secrets or sensitive files, restricted network access,
  and CPU, memory and time limits.
- Disable the features that are not needed: for instance, the Groovy post-processor is only active if it is explicitly configured.
