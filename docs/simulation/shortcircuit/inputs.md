# Inputs

The API takes as inputs:

**A network**

It is the network on which the computation will be done.

**A list of faults**

The API takes as input a list of faults on which the calculation should be done. Faults on buses and on lines are supported.
Each fault can either be an instance of `com.powsybl.shortcircuit.BusFault` or `com.powsybl.shortcircuit.BranchFault`.

The attributes to fill of a `BusFault` are:

| Attribute  | Type           | Unit | Required | Default value           | Description                                                                                             |
|------------|----------------|------|----------|-------------------------|---------------------------------------------------------------------------------------------------------|
| id         | String         | -    | yes      | -                       | The id of the fault                                                                                     |
| elementId  | String         | -    | yes      | -                       | The id of the bus on which the fault will be simulated (bus/view topology)                                                 |
| r          | double         | Ω    | no       | 0                       | The fault resistance to ground                                                                          |
| x          | double         | Ω    | no       | 0                       | The fault reactance to ground                                                                           |
| connection | ConnectionType | -    | no       | `ConnectionType.SERIES` | The way the resistance and reactance of the fault are connected to the ground: in series or in parallel |
| faultType  | FaultType      | -    | no       | `FaultType.THREE_PHASE` | The type of fault simulated: can be three-phased or single-phased                                       |

The attributes to fill of a `BranchFault` are:

| Attribute            | Type           | Unit | Required | Default value           | Description                                                                                             |
|----------------------|----------------|------|----------|-----------------------  |---------------------------------------------------------------------------------------------------------|
| id                   | String         | -    | yes      | -                       | The id of the fault                                                                                     |
| elementId            | String         | -    | yes      | -                       | The id of the branch on which the fault will be simulated                                               |
| r                    | double         | Ω    | no       | 0                       | The fault resistance to ground                                                                          |
| x                    | double         | Ω    | no       | 0                       | The fault reactance to ground                                                                           |
| connection           | ConnectionType | -    | no       | `ConnectionType.SERIES` | The way the resistance and reactance of the fault are connected to the ground: in series or in parallel |
| faultType            | FaultType      | -    | no       | `FaultType.THREE_PHASE` | The type of fault simulated: can be three-phased or single-phased                                       |
| proportionalLocation | double         | %    | yes      | -                       | The position where the fault should be simulated, in percent of the line                                |

**A list of FaultParameters**

Optionally, it is possible to specify a list of `FaultParameters`. Each `FaultParameter` will override the default parameters for a given fault.
For more information on parameters, see [above](#faultparameters).
