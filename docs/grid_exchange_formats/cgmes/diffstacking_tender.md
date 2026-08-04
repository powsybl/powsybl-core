# Tender: Partial CGMES SSH Export From Network Changes

When building a service mesh of multiple services operating grid analysis software, a quick exchange of grid models is desireable. For this, we want to avoid writing and reading full grid files. Hence, a minimal difference format is desireable that can communicate *changes* instead of full grid models. This way, both the sender and receiver process of an IPC can pre-load a common grid model and later communicate only the difference of the current business process. 

The CGMES ecosystem offers two options how to communicate such a change
- Partial .ssh files with only those values set that have changed
- CGMES Difference Models encoding explicit forward and backward differences.

PowSyBl already supports importing partial CGMES SSH updates into an IIDM network loaded in memory, meaning the importer side is already present.
The goal of this tender is to implement the sender-side functionality: export a partial SSH/CGMES Diff file containing only steady-state changes made to an existing network, so that another process can apply the update without reloading the full network.

The implementation shall target an existing change log of the in-memory network state, typically recorded using `NetworkEventRecorder`, and shall generate a valid update file that can be applied through the existing network update workflow.
The feature shall also be exposed through pypowsybl.


# WP1 - NetworkEventRecorder to partial .SSH

The following changes are deemed sensible and shall be supported.

## Must Have

- AC switch open or closed state
- Active and reactive setpoints for loads
- Generator target active power/reactive power/voltage.
- Phase/ratio tap changer tap position.
- Shunt Section count/target voltage
- SVC Reactive power/voltage setpoint
- HVDC line active power setpoint.
- DC switch open or closed state (currently resolves to ACDCTerminal.connected, optional if difficult to implement)
- VSC converter voltage/reactive power setpoint.

## Nice to have

- Generator voltage regulation enabled or disabled state.
- Generator participation factor and reference priority
- EquivalentInjection, ExternalNetworkInjection operating values
- Boundary Line operating values
- Tap changer regulating enabled or disabled state.
- Tap changer regulation value / target deadband
- Shunt target deadband/voltage regulation enabled
- SVC Regulating mode/enabled or disabled state.
- HVDC converters mode.
- LCC converter power factor where modeled in SSH.
- VSC converter voltage-regulation enabled or disabled state.
- Branch current limits
- Bus voltage limits

Note: Changes to operational limits are not part of the official .ssh standard, however import is implemented in powsybl. Implement an export such that it works with the current import.

## Scope Out

The following items are explicitly out of scope.

- Terminal connectivity state changes (except for DC switches)
- Structural model changes (creation/removal of equipment)
- Rewiring or topology structure changes requiring EQ or TP updates.
- State Variables updates

## Non-functional requirements

- The performance of the exporter is critical, small changes <1000 events should export extremely quickly (<100ms). Part of the development process shall be a performance profiling session with benchmark results for small and medium change logs.
- Writing to .ssh file and in-memory string buffer should be supported.
- Sensible xml header tags such as model identifier, creation time, scenario time, model version, supersedes and dependson tags should be supported.
- If unsupported changes are within the log, the user should be able to choose whether the export shall raise or ignore them.
- If the same element was changes multiple times, retain only the last change (compaction).
- Tests verify roundtrip correctness for all supported elements (.ssh export, .ssh import)

# WP2 - NetworkEventRecorder to CGMES Diff

The cgmes difference format is a richer format, allowing additions/removals/changes and is in full too powerful for the scope of work intended here. However, it provides two some advantages over .ssh diffs that are relevant for this project:

- Forward and backward differences
- Support for operational limit changes as .eq profile can be diff target

This work package aims to mirror the export functionality that exists to .ssh, but to cgmes diff. Explicitely, this work package shall not 
- include any new element exports in comparison to the .ssh exporter
- build an importer for cgmes diffs 
- validate correctness through roundtrip tests.

Instead, correctness of the generated cgmes difference model shall be only checked syntactically in unit test and with some manual regression tests against an established cgmes difference model importer such as powerfactory or OpenCGMES.


# WP3 - pypowsybl integration

- The feature shall be exposed through the pypowsybl Python API.
- The preferred user workflow is a recorder context manager attached to a Python `Network` object.
- The user shall be able to choose if the system ignores unsupported updates (e.g. addition of a line) or raises on export
- The target usage shall be equivalent to the following pattern:

```python
with network.event_recorder() as recorder:
    network.update_switches(...)
    network.update_ratio_tap_changers(...)
    network.update_phase_tap_changers(...)
    network.update_generators(...)
    network.update_loads(...)
    with open("output.ssh", "w") as f:
        recorder.to_ssh(f)
```




