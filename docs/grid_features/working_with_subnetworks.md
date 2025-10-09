# Working with subnetworks

## Concept of subnetwork

It is sometimes useful to work on an aggregated network containing the data of several networks. But you may also want to remember their structure to later work on the individual networks. 

This is the case, for instance, when working with multi-country CGMES archives. They are imported as a single network on which it is possible to run a load flow, but at export, one EQ file per country should be generated.

To manage this, PowSyBl has a concept of subnetwork:
- Subnetworks are also networks (i.e. they are `Network` objects).
- A network can have several subnetworks.
- There is maximum 2 levels of networks: the main network, and possibly some subnetworks directly inside the main network. Subnetworks cannot contain subnetworks.  
- Each network element (substation, voltage level, ...) can be in the main network or in a subnetwork.
- Each network element has 2 methods to retrieve:
  - the main network: `Identifiable.getNetwork()`;
  - the network it is immediately in (the main network or a subnetwork): `Identifiable.getParentNetwork()`.
- When a network element is in a subnetwork `s1`, it is retrievable (for instance by using `network.getIdentifiable(id)`) from the main network and from `s1`, but not from the other subnetworks.
- When a network element is in the main network, it is not retrievable from the subnetworks.


## Merging networks

You can merge several independent networks together using one of the following commands:
- `Network n = Network.merge(id, network1, network2, ...)`
- `Network n = Network.merge(network1, network2, ...)`

These commands create a new `Network` `n` having a subnetwork for each network given as parameter.

For each network `network`*`i`*, a new subnetwork is created in `n` with the same ID. All its content (network element, extensions, properties, ...)
is transferred to the subnetwork. As a result, `network`*`i`* will be empty after the operation.

During this merging operation, unpaired `DanglingLine`s are examined. Two unpaired dangling lines in different networks will be paired (and a `TieLine` will be created) if:
- they have the same non-null pairing key and are the only dangling lines to have this pairing key in their respective networks;
- **OR** they have the same non-null pairing key and are the only **connected** dangling lines to have this pairing key in their respective networks.

If all the `network`*`i`* have the same source format, the resulting network will also receive this format. Else, it will be set to `hybrid`.

There are some restrictions preventing some networks to be merged together. For the merge to work:
- All the networks should be independent: they can't be subnetworks, and they can't contain subnetworks.
- The networks should have only one variant.
- There cannot be duplicates among the network element's ids of the networks.


## Importing a network

The IIDM format supports subnetworks (starting from version 1.11). So if you import an XIIDM (XML), a JIIDM (JSON) or a BIIDM (binary) file,
it could contain subnetworks. In this case, your resulting network will also have subnetworks.

You could also get a network containing subnetworks if you import a multi-country CGMES archive or directory.
The content for each country will be stored as a subnetwork.


## Creating a subnetwork via the API

You can also create subnetworks manually via the API:

```java
Network network = Network.create("Root", "format0");
Network subnetwork1 = network.createSubnetwork("Sub1", "subnetwork #1", "format1");
Network subnetwork2 = network.createSubnetwork("Sub2", "subnetwork #2", "format2");
```

Once created, it is now possible to add substations, voltage levels, lines, ... in the main network ("network"), or in one
of the subnetworks ("subnetwork1" or "subnetwork2") using the same methods of the API: they are all `Network` objects.


## Detaching a subnetwork

It is possible to "detach" a subnetwork from its main network with the following instruction: 

```java
Network n = subnetwork.detach();
```

Note that this operation is destructive: after it the `subnetwork`'s content could not be accessed from the main network anymore,
and `subnetwork` is empty (all its content is transferred into `n`).

The boundary elements, i.e. the elements linking the subnetwork to an external voltage level (it could be lines, HVDC lines or tie lines),
are split if possible. If not, the detach operation will fail.

Some checks are performed which may prevent the detach method to work:   
- Only subnetworks can be detached.
- There should not be un-splittable boundary elements.
- The main network should not be multi-variant.

Before calling the `detach` method, you can manually test if these checks will fail or not. You can also detect the un-splittable boundary elements and remove them if you want:

```java
if (!subnetwork.isDetachable()) {
    Set<Identifiable<?>> boundaryElements = subnetwork.getBoundaryElements();
    // You can for instance check the boundary elements,
    // remove them if they are not needed, ...
    // then detach `subnetwork`
}
```


## Flattening a network

In some particular occasion, the presence of subnetworks in your network may be an obstacle. For instance, it is not
possible to merge networks if one of them has subnetworks. To circumvent this problem, you can "flatten" your network,
i.e. remove its subnetworks' structure. During this operation, all the data contained in the subnetworks (`Identifiable`s,
extensions, and properties) are transferred into the main network and the subnetworks (thus emptied) are removed from it.

```java
// n is a network with subnetworks
n.flatten();
// Now, n has no more subnetworks. Their content was transferred into n.
```

Networks can only have a maximum of one extension of a certain type, and it is also the case for subnetworks.
But since several subnetworks of the same main network may have an extension of the same type, this may be problematic
when flattening the main network: there is no extensions merging generic mechanism, so we cannot automatically keep
all the extensions' content. The same problem may occur with properties since a network can only have one property of a certain name.

To solve this problem, the following policy is applied. Subnetworks are integrated in the whole network following the same order they were merged.
For each one, only the properties and the extensions which are not already present in the currently flattened network
(i.e. same name for properties or same type for extensions) are transferred. If a duplicate is detected,
this latter is not transferred and will remain in its original subnetwork at the end of the flattening operation.
It is thus possible to retrieve potential duplicates and to handle them manually.

For instance, if:
- `n0` has 2 subnetworks `s1` and `s2` (merged in this order).
- `s1` has the property `(key = val1)`.
- `s2` has the property `(key = val2)`.

After `n0.flatten()`:
- `n0` will have the property `(key = val1)` (when "integrating" `s1`, no property of key `key` was found in `n0`).
- `s1` will have no property (it was transferred to `n0`).
- `s2` will have the property `(key = val2)` (the property was *not* transferred because the property `(key = val1)` was already in `n0`).
