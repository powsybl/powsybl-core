# Action DSL
The action DSL is a domain specific language written in groovy for the creation of a strategy to solve violations, used in [security analyses with remedial actions](security.md#operator-strategies). This strategy is constituted of a set of [contingencies](contingency-dsl.md), a set of modification on the network called **actions**, and a set of rules to determine in which circumstances to apply the actions.

## Contingencies
The contingencies are defined in the same way that is described in the [contingency DSL](contingency-dsl.md). Are supported:
- N-1 contingencies that triggers a single equipment at a time
- N-K contingencies that triggers multiple equipments at a time
- busbar contingencies that triggers a [busbar section](../../grid_model/network_subnetwork.md#busbar-section), a N-K contingency that triggers all the equipments connected to that busbar section.

## Actions
The actions are modifications that could be made on the network to solve a security issue. It could be topology modification like opening or closing a switch, setpoints modification, tap position modification. PowSyBl provides a set of predefined actions, but this possibilities are infinite as you can create custom actions or groovy scripts.

An action is constituted of:
- an unique identifier to reference this action in the [rules](#rules)
- an optional description to explain the purpose of the action
- a list of tasks that are executed when the action is applied

The following snippet shows how to create an action:
```groovy
action('actionID') {
    description "A short description for this actions"
    tasks {
        // Put here the tasks to execute when the action is applied
    }
}
```

### Opening / Closing a switch
Topology changes are usually used to reduce the intensity on a AC branch. The following snippet shows how to create an action that opens the switch `SW1` and another one to close it.
```groovy
action('open-switch-SW1') {
    description "Open switch SW1"
    tasks {
        openSwitch 'SW1'
    }
}

action('close-switch-SW1') {
    description "Close switch SW1"
    tasks {
        closeSwitch 'SW1'
    }
}
``` 

Note that it's possible to open or close several switch at a time:
```groovy
action('open-SW1-and-SW2') {
    tasks {
        openSwitch 'SW1'
        openSwitch 'SW2'
    }
}
```

### Generator modification
The `generatorModification` task is a task that can modify the setpoints and the regulation mode of a [generator](../../grid_model/network_subnetwork.md#generator). It supports the modification of:
- the active power limits
- the active power setpoints as an absolute value or with an increment
- the voltage setpoint, the reactive power setpoint and the regulation mode
- the connection status

```groovy
action('change-active-power-limits') {
    description "Change the active power limits of generator GEN to [0, 100]"
    tasks {
        generatorModification('GEN') {
            minP 0
            maxP 100
        }
    }
}

action('change-active-power-setpoint') {
    description "Change the active power setpoint of generator GEN to 100"
    tasks {
        generatorModification('GEN') {
            targetP 100
        }
    }
}

action('increment-active-power-setpoint') {
    description "Increment the active power setpoint of generator GEN by 10"
    tasks {
        generatorModification('GEN') {
            deltaTargetP 10
        }
    }
}

action('change-regulation-mode') {
    description "Change the regulation's mode to voltage and change the setpoint"
    tasks {
        generatorModification('GEN') {
            voltageRegulatorOn true
            targetV 400
            targetQ 0
        }
    }
}

action('disconnect-generator') {
    description "Disconnect the generator GEN"
    tasks {
        generatorModification('GEN') {
            connected false
        }
    }
}
``` 

### Changing a phase tap changer position
Changing the tap position of a phase tap changer is really useful to change how the active power is spread over parallel branches.

#### phaseShifterFixedTap
The `phaseShifterFixedTap` task is used to set the tap position to a fixed value. As power flow simulator could change the tap position during the simulation, it's necessary to also change the regulation's mode of the tap changer to `FIXED_TAP`.
```groovy
action('fix-tap-position') {
    description "Set the tap position of TWT to tap 10"
    tasks {
        phaseShifterFixedTap('TWT', 10)
    }
}
```

#### phaseShifterTap
The `phaseShifterTap` task is used to increment or decrement the tap position. As power flow simulator could change the tap position during the simulation, it's necessary to also change the regulation's mode of the tap changer to `FIXED_TAP`. If the new tap position is lower than the minimal tap position, or greater than the maximal tap position, the tap position is adjusted to be in the bounds.
```groovy
action('increment-tap-position') {
    description "Increment the tap position of TWT by 4"
    tasks {
        phaseShifterTap('TWT', 4)
    }
}

action('decrement-tap-position') {
    description "Decrement the tap position of TWT by 4"
    tasks {
        phaseShifterTap('TWT', -4)
    }
}
```

#### optimizePhaseShifterTap
The `optimizePhaseShifterTap` task is used to change the tap position of a phase tap changer until the intensity is closest to the limit but does not exceed it. This task runs a [load flow](../loadflow/index) each time the tap is changed to compute the new intensity value of the PST.
```groovy
action('optimize-tap-position') {
    description "Find the tap position to be closest to the limit"
    tasks {
        optimizePhaseShifterTap 'TWT'
    }
}
```

As this task relies on a power flow simulator, this task needs to be configured in the configuration. If the simulator's name is not specified, the default one is used.

**Example in YAML**
```yaml
load-flow-based-phase-shifter-optimizer:
    load-flow-name: Default
```

**Example in XML**
```xml
<load-flow-based-phase-shifter-optimizer>
    <load-flow-name>Default</load-flow-name>
</load-flow-based-phase-shifter-optimizer>
```

### Scripting
The `script` task allow you to execute groovy code to modify the network. You can access to the network and the computation manager, using the `network` and `computationManager` variables. With this task, possibilities are unlimited as you have a complete access to the IIDM API.
```groovy
action('custom-action') {
    description "Disconnect LOAD1 and LOAD2, open the coupler COUPLER and change the setpoints of LOAD3"
    tasks {
        script {
            network.getLoad("LOAD1").getTerminal().disconnect()
            network.getLoad("LOAD2").getTerminal().disconnect()

            network.getSwitch("COUPLER").setOpen(true)

            network.getLoad("LOAD3").setP0(100).setQ0(60)
        }
    }
}
```

## Rules
The rules are the most important in this DSL: they define the activation criteria of the actions. A rule is constituted of:
- a unique identifier
- an optional description to explain the purpose of the rule
- an activation criteria
- a list of actions to applied if the activation criteria is verified
- an optional life count to limit the number of times a rule can be verified and its actions applied

### Activation criteria
The activation criteria is a logical expression using the network's API and arithmetic operations on its variables. To create actions scripts more easily, a set of predefined functions can be used, but it's also possible to create custom ones.
```groovy
rule('rule-ID') {
    description "A short description"
    when contingencyOccured()
    apply 'action1', 'action2'
    life 1
}
```

**Note:** The activation criteria is evaluated during the simulation. If you want to save the initial value of a network's variable, you should declare a global variable at the beginning of your script.

#### Network binding
The network binding adds keywords in the DSL to get equipments by their IDs. At the moment, the following keywords are supported:
- `line`: to retrieve a [line](../../grid_model/network_subnetwork.md#line)
- `transformer`: to retrieve a [two windings transformer](../../grid_model/network_subnetwork.md#two-windings-transformer)
- `branch`: to retrieve a [line](../../grid_model/network_subnetwork.md#line), a [tie line](../../grid_model/network_subnetwork.md#tie-line) or a [two windings transformer](../../grid_model/network_subnetwork.md#two-windings-transformer)
- `generator`: to retrieve a [generator](../../grid_model/network_subnetwork.md#generator)
- `load`: to retrieve a [load](../../grid_model/network_subnetwork.md#load)
- `_switch` and `switch_` to retrieve a [switch](../../grid_model/network_subnetwork.md#breakerswitch)

**Note:** the `switch` keyword is reserved in Groovy, so pay attention to prefix or postfix with an underscore.

**Note:** if you try to access to an undefined characteristic, a property is automatically created. Be very careful to typography mistakes because it could lead to unexpected results of your simulation.

#### Predefined functions
The following predefined functions are available and can be used in the `when` statement:
- `actionTaken`: returns `true` if the given action has already be applied
- `contingencyOccured`: returns `true` if a contingency is currently simulated, and `false` if the N state is simulated
- `loadingRank`: returns the rank of a given branch among a list of branches regarding their overload level
- `mostLoaded`: returns the ID of the most loaded branch among a list of branches
- `isOverloaded`: returns `true` if at least one of the given branches is overloaded
- `allOverloaded`: returns `true` if all the given branches are overloaded

**Examples**
In this first example, when a contingency has occurred and `action1` has already been applied, we apply `actions2`:
```groovy
action('action1') {
}

action('action2') {
}

rule('example1') {
    description "Apply action2 if a contingency has occured, and action1 has been applied"
    when contingencyOccured() and actionTaken('action1')
    apply 'action2'
}
```

In this example, we disconnect the line `LINE1` if it's the most loaded of `LINE1`, `LINE2` and `LINE3`:
```groovy
action('disconnect-line1') {
    tasks {
        script {
            line('LINE1').getTerminal1().disconnect()
            line('LINE1').getTerminal2().disconnect()
        }
    }
}

rule('example2') {
    description "Disconnect the line LINE1 if it is the most loaded"
    when contingencyOccured() and mostLoaded("LINE1", ["LINE1", "LINE2", "LINE3"])
    apply 'disconnect-line1'
}
```

In this second example, when both `LINE1` and `LINE2` are overloaded, we change the tap position of the PST `TWT`:
```groovy
action('change-tap-position') {
    tasks {
        phaseShifterFixedTap('TWT', 10)
    }
}

rule('example3') {
    description "If LINE1 and LINE2 are overloaded then change the PST tap position"
    when allOverloaded(["LINE1", "LINE2"])
    apply 'change-tap-postition'
}
```

### Dry-run mode
<span style="color: red">TODO</span>

## Configuration
<span style="color: red">TODO</span>

## Going further
<span style="color: red">TODO</span>

