# iAL Actions

## Syntax

The generic syntax to describe a single or a set of actions is:

```
action('action-id') {
  description "this is a description" // optional
  tasks{
    preDefinedAction1
    ...
    preDefinedActionN
    script{ // optional
      scriptAction1
      ...
      scriptActionN
    }
  }
}
```

## Examples

### Pre-defined tasks

This action consists in 2 manipulations of switches and a composite action (changing a tap position to 25 and switching to 'FIXED_TAP' mode).

```
action('action-id') {
  description "this is a description"
  tasks{
    openSwitch 'switch1-id'
    closeSwitch 'switch2-id'
    PhaseShifterFixedTap('pst-id',25)
  }
}
```

### Actions defined in a script

The same set of actions as above can be described directly in the script. The use of pre-defined tasks is prefered if they exist.

```
import static com.powsybl.iidm.network.PhaseTapChanger.RegulationMode.FIXED_TAP

action('action-id') {
  description "this is a description"
  tasks{
    script{
      // switch is a reserved groovy key word
      switch_('switch1-id').open = true
      _switch('switch2-id').open = false
      transformer('pst-id').phaseTapChanger.regulationMode = FIXED_TAP
      transformer('pst-id').phaseTapChanger.tapPosition = 25
    }
  }
}
```

*Nota Bene*

Because the language is extensible, new pre-defined tasks can be developed as plugins and added to the DSL language, according to specific needs. 
Complex actions can also be described in the "script" part.

## Existing predefined tasks:

### Open a switch
```
openSwitch 'switch-id'
```
### Close a switch
```
closeSwitch 'switch-id'
```
### Set a new tap and switch to fixed tap mode
```
PhaseShifterFixedTap('pst-id',tapPosition)
```

### Set the best optimal tap (loadflow based)
```
PhaseShifterOptimizerTap('pst-id')
```
