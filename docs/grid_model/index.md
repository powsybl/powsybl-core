# Grid model

```{toctree}
:hidden:
network_subnetwork.md
additional.md
extensions.md
going_further.md
```

Powsybl features are strongly based on an internal grid model initially developed under the iTesla project, a research project funded by the [European Union 7th Framework programme](https://cordis.europa.eu/project/id/283012) (FP7). The grid model is known as `iidm` (iTesla Internal Data Model). One of the iTesla outputs was a toolbox designed to support the decision-making process of power system operation from two-days ahead to real time. The `iidm` grid model was at the center of the toolbox.

The equipment of a substation (busbar sections, switches, buses, loads, generators, shunt compensators, static VAR compensators, HVDC converters stations, etc.) is grouped in voltage levels. Transformers present in a substation connect its different voltage levels. Transmission lines (AC and DC) connect the substations.

To build an electrical network model, the common way is to define the substations first, then to define their voltage levels. But for some specific cases, it is also possible to create voltage levels without a substation.

The grid model allows a full representation of the substation connectivity where all the switching devices and busbar sections are defined, this topology is called node/breaker view. Automated topology calculation allows for the calculation of the network bus/breaker view as well as the network bus view.

Different states of the network can be efficiently stored together with the power system model. The set of attributes that define a given state of the network (both steady state hypothesis and state variables) are collectively organized in variants. The user can create and remove variants as needed. Setting and getting variant dependent attributes on network objects use the current variant.

A set of networks can be merged together in a single network. The initial subnetworks are kept and can be easily retrieved or detached if needed.

Almost all the elements modeled in the network are identified through a unique `id`, and optionally described by a `name` that is easier to interpret for a human. Almost all components can be [extended](extensions.md) by the user to incorporate additional structured data.
