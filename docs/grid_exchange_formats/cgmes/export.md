# Export

There are two main use-cases supported:
 * Export IGM (Individual Grid Model) instance files. There is a single network and a unique CGMES modelling authority. 
 * Export CGM (Common Grid Model) instance files. A network composed of multiple subnetworks, where each subnetwork is an IGM. 

In both cases, the metadata model information in the exported files is built from metadata information read from the input files and stored in IIDM or received through parameters. 
Information received through parameters takes precedence over information available from original metadata models.

For a quick CGM export, the user may rely on the parameter **iidm.export.cgmes.cgm_export** to write in a single export multiple updated SSH files (one for each IGM) and a single SV for the whole common grid model. Specifics about this option are explained in the section [below](#cgm-common-grid-model-quick-export).
If you need complete control over the exported files in a CGM scenario, you may prefer to iterate through the subnetworks and make multiple calls to the export function. This is described in detail in the section [below](#cgm-common-grid-model-manual-export).    

Please note that when exporting equipment, PowSyBl always use the CGMES node/breaker level of detail, without considering the topology
level of the PowSyBl network.

The user can specify the profiles to be exported using the parameter **iidm.export.cgmes.profiles**. The list of currently supported export instance files are: EQ, SSH, SV, TP. 

If the IIDM network has at least one voltage level with node/breaker topology level, and the SSH or SV is requested in the export, and the TP is not requested, an error will be logged, as there could be missing references in the SSH, SV files to Topological Nodes calculated automatically by IIDM that are not present in the output.

If the dependencies have to be updated automatically (see parameter **iidm.export.cgmes.update-dependencies** below), the exported instance files will contain metadata models where:
* TP and SSH depend on EQ.
* SV depends on TP and SSH.
* EQ depends on EQ_BD (if present). EQ_BD is the profile for the boundary equipment definitions.
* SV depends on TP_BD (if present). TP_BD is the profile for the boundary topology. Only for CGMES 2.4.

The output filenames will follow the pattern `<baseName>_<profile>.xml`. The basename is determined from the parameters, or the basename of the export data source or the main network name. 

## CGM (Common Grid Model) quick export

When exporting a CGM, we need an IIDM network (CGM) that contains multiple subnetworks (one for each IGM). 
Only the CGMES instance files corresponding to SSH and SV profiles are exported:
an updated SSH file for every subnetwork (for every IGM) and a single SV file for the main network that represents the CGM.

When exporting, it is verified that the main network and all subnetworks have the same scenario time (network case date). If they are different, an error is logged.

If no version is provided as a parameter for the exported files, the output version is determined based on the max version of input CGM SV metadata and IGM SSH versions.

If the dependencies have to be updated automatically (see parameter **iidm.export.cgmes.update-dependencies** below), the exported instance files will contain metadata models where:
* Updated SSH for IGMs supersede the original ones.
* Updated SV for the CGM depends on the updated SSH from IGMs and on the original TP from IGMs.

The filenames of the exported instance files will follow the pattern:
* For the CGM SV: `<basename>_SV.xml`.
* For the IGM SSHs: `<basename>_<IGM name>_SSH.xml`. The IGM name is built from the country code of the first substation or the IIDM name if no country is present.

The basename is determined from the parameters, or the basename of the export data source or the main network name.

As an example, you can export one of the test configurations that has been provided by ENTSO-E. It is available in the cgmes-conformity module of the powsybl-core repository. If you run the following code:

```java
Network cgmNetwork = Network.read(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource());

Properties exportParams = new Properties();
exportParams.put(CgmesExport.PROFILES, List.of("SV", "SSH"));
exportParams.put(CgmesExport.EXPORT_BOUNDARY_POWER_FLOWS, true);
exportParams.put(CgmesExport.NAMING_STRATEGY, "cgmes");
exportParams.put(CgmesExport.CGM_EXPORT, true);
exportParams.put(CgmesExport.UPDATE_DEPENDENCIES, true);
exportParams.put(CgmesExport.MODELING_AUTHORITY_SET, "MAS1");

cgmNetwork.write("CGMES", exportParams, new FileDataSource(Path.of("/exampleFolder"), "exampleBase"));
```

You will obtain the following files in your `exampleFolder`:

```
exampleBase_BE_SSH.xml
exampleBase_NL_SSH.xml
exampleBase_SV.xml
```

where the updated SSH files will supersede the original ones, and the SV will contain the correct dependencies of new SSH and original TPs.

## CGM (Common Grid Model) manual export

If you want to intervene in how the updated IGM SSH files or the CGM SV are exported, you can make multiple calls to the CGMES export function.

You can use following code for reference:

```java
Network cgmNetwork = Network.read(CgmesConformity1Catalog.microGridBaseCaseAssembled().dataSource());

// We decide which version we want to export
int exportedVersion = 18;

// Common export parameters
Properties exportParams = new Properties();
exportParams.put(CgmesExport.EXPORT_BOUNDARY_POWER_FLOWS, true);
exportParams.put(CgmesExport.NAMING_STRATEGY, "cgmes");
// We do not want a quick CGM export
exportParams.put(CgmesExport.CGM_EXPORT, false);
exportParams.put(CgmesExport.UPDATE_DEPENDENCIES, false);

// For each subnetwork, prepare the metadata for SSH and export it
for (Network n : cgmNetwork.getSubnetworks()) {
    String country = n.getSubstations().iterator().next().getCountry().orElseThrow().toString();
    CgmesMetadataModel sshModel = n.getExtension(CgmesMetadataModels.class).getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS).orElseThrow();
        sshModel.clearDependencies()
                .addDependentOn("myDependency")
                .addSupersedes("mySupersede")
                .setVersion(exportedVersion)
                .setModelingAuthoritySet("myModellingAuthority");
        exportParams.put(CgmesExport.PROFILES, List.of("SSH"));
    n.write("CGMES", exportParams, new FileDataSource(Path.of("/manualFolder"), "manualBase_" + country));
}

// In the main network, CREATE the metadata for SV and export it
cgmNetwork.newExtension(CgmesMetadataModelsAdder.class)
    .newModel()
        .setSubset(CgmesSubset.STATE_VARIABLES)
        .addProfile("http://entsoe.eu/CIM/StateVariables/4/1")
        .setId("mySvId")
        .setVersion(exportedVersion)
        .setModelingAuthoritySet("myModellinAuthority")
        .addDependentOn("mySvDependency1")
        .addDependentOn("mySvDependency2")
    .add()
    .add();
exportParams.put(CgmesExport.PROFILES, List.of("SV"));
cgmNetwork.write("CGMES", exportParams, new FileDataSource(Path.of("/manualFolder"), "manualBase"));
```

The file `manualBase_BE_SSH.xml` inside `/manualFolder` will have the following contents for the metadata:

```xml
...
<md:Model.description>CGMES Conformity Assessment ...</md:Model.description>
<md:Model.version>18</md:Model.version>
<md:Model.DependentOn rdf:resource="myDependency"/>
<md:Model.Supersedes rdf:resource="mySupersede"/>
<md:Model.profile>http://entsoe.eu/CIM/SteadyStateHypothesis/1/1</md:Model.profile>
<md:Model.modelingAuthoritySet>myModellingAuthority</md:Model.modelingAuthoritySet>
...
```

And the file `manualBase_SV.xml` will contain:

```xml
...
<md:Model.version>18</md:Model.version>
<md:Model.DependentOn rdf:resource="mySvDependency1"/>
<md:Model.DependentOn rdf:resource="mySvDependency2"/>
<md:Model.profile>http://entsoe.eu/CIM/StateVariables/4/1</md:Model.profile>
<md:Model.modelingAuthoritySet>myModellinAuthority</md:Model.modelingAuthoritySet>
...
```

Remember that, in addition to setting the info for metadata models in the IIDM extensions, you could also rely on parameters passed to the export methods.

## Conversion from PowSyBl grid model to CGMES

The following sections describe in detail how each supported PowSyBl network model object is converted to CGMES network components.

### Battery

PowSyBl [`Battery`](../../grid_model/network_subnetwork.md#battery) is exported as `SynchronousMachine` with `HydroGeneratingUnit`.

<span style="color: red">TODO details</span>

### BusbarSection

PowSyBl [`BusbarSection`](../../grid_model/network_subnetwork.md#busbar-section) is exported as CGMES `BusbarSection`.

<span style="color: red">TODO details</span>

### DanglingLine

PowSyBl [`DanglingLine`](../../grid_model/network_subnetwork.md#dangling-line) is exported as several CGMES network objects.
Each dangling line will be exported as one `EquivalentInjection` and one `ACLineSegment`.

<span style="color: red">TODO details</span>

### Generator

PowSyBl [`Generator`](../../grid_model/network_subnetwork.md#generator) is exported as CGMES `SynchronousMachine`.

<span style="color: red">TODO details</span>

### HVDC line and HVDC converter stations

A PowSyBl [`HVDCLine`](../../grid_model/network_subnetwork.md#hvdc-line) and its two [`HVDCConverterStations`](../../grid_model/network_subnetwork.md#hvdc-converter-station) are exported as a `DCLineSegment` with two `DCConverterUnits`.

<span style="color: red">TODO details</span>

### Line

PowSyBl [`Line`](../../grid_model/network_subnetwork.md#line) is exported as `ACLineSegment`.

<span style="color: red">TODO details</span>

### Load

PowSyBl [`Load`](../../grid_model/network_subnetwork.md#load) is exported as `ConformLoad`, `NonConformLoad` or `EnergyConsumer` depending on the extension [`LoadDetail`](../../grid_model/extensions.md#load-detail).

<span style="color: red">TODO details</span>

### Shunt compensator

PowSyBl [`ShuntCompensator`](../../grid_model/network_subnetwork.md#shunt-compensator) is exported as `LinearShuntCompensator` or `NonlinearShuntCompensator` depending on their models.

<span style="color: red">TODO details</span>

### StaticVarCompensator

PowSyBl [`StaticVarCompensator`](../../grid_model/network_subnetwork.md#static-var-compensator) is exported as `StaticVarCompensator`.

<span style="color: red">TODO details</span>

### Substation

PowSyBl [`Substation`](../../grid_model/network_subnetwork.md#substation) is exported as `Substation`.

<span style="color: red">TODO details</span>

### Switch

PowSyBl [`Switch`](../../grid_model/network_subnetwork.md#breakerswitch) is exported as CGMES `Breaker`, `Disconnector` or `LoadBreakSwitch` depending on its `SwitchKind`.

<span style="color: red">TODO details</span>

### ThreeWindingsTransformer

PowSyBl [`ThreeWindingsTransformer`](../../grid_model/network_subnetwork.md#three-windings-transformer) is exported as `PowerTransformer` with three `PowerTransformerEnds`.
<span style="color: red">TODO details</span>

### TwoWindingsTransformer

PowSyBl [`TwoWindingsTransformer`](../../grid_model/network_subnetwork.md#two-windings-transformer) is exported as `PowerTransformer` with two `PowerTransformerEnds`.

<span style="color: red">TODO details</span>

### Voltage level

PowSybl [`VoltatgeLevel`](../../grid_model/network_subnetwork.md#voltage-level) is exported as `VoltageLevel`.

<span style="color: red">TODO details</span>

## Extensions

### Control areas

PowSyBl [`ControlAreas`](import.md#cgmes-control-areas) are exported as several `ControlArea`.

<span style="color: red">TODO details</span>

## Options

These properties can be defined in the configuration file in the [import-export-parameters-default-value](../../user/configuration/import-export-parameters-default-value.md#import-export-parameters-default-value) module.

Note that if you are exporting a network that does not come from CGMES, you can use the [`iidm.import.cgmes.boundary-location`](#options) property to define the location of the boundary files to use as reference.

**iidm.export.cgmes.base-name**  
Optional property that defines the base name of the exported files. Exported CGMES files' names will look like this:
```
<base_name>_EQ.xml
<base_name>_TP.xml
<base_name>_SSH.xml
<base_name>_SV.xml
```
By default, the base name is the network's name if it exists, or else the network's ID.

**iidm.export.cgmes.boundary-eq-id**  
Optional property that defines the ID of the EQ-BD model if there is any.
Its default value is `null`: we consider there is no EQ-BD model to consider.
If this property is defined, then this ID will be written in the header of the exported EQ file.

**iidm.export.cgmes.boundary-tp-id**  
Optional property that defines the ID of the TP-BD model if there is any.
Its default value is `null`: we consider there is no TP-BD model to consider.
If this property is defined, then this ID will be written in the header of the exported SV file.

**iidm.export.cgmes.cim-version**  
Optional property that defines the CIM version number in which the user wants the CGMES files to be exported.
CIM versions 14, 16 and 100 are supported i.e. its valid values are `14`, `16` and `100`.
If not defined, and the network has the extension `CimCharacteristics`, the CIM version will be the one indicated in the extension. If not, its default value is `16`.
CIM version 16 corresponds to CGMES 2.4.15.
CIM version 100 corresponds to CGMES 3.0.

**iidm.export.cgmes.encode-ids**  
Optional property that must be used if IIDM IDs that are not compliant with CGMES requirements are to be used as CGMES IDs. `true` by default. Used for debugging purposes.

**iidm.export.cgmes.export-boundary-power-flows**  
Optional property that defines if power flows at boundary nodes are to be exported in the SV file or not. `true` by default.

**iidm.export.cgmes.export-power-flows-for-switches**  
Optional property that defines if power flows of switches are exported in the SV file. `true` by default.

**idm.export.cgmes.naming-strategy**  
Optional property that defines which naming strategy is used to transform IIDM identifiers to CGMES identifiers.
It can be:
- `identity`: CGMES IDs are the same as IIDM IDs.
- `cgmes`: new CGMES IDs (new master resource identifiers, cim:mRID) are created for IIDM `Identifiables` if the IIDM IDs are not compliant with CGMES requirements.
- `cgmes-fix-all-invalid-ids`: ensures that all CGMES IDs in the export will comply with CGMES requirements, for IIDM `Identifiables`and also for its related objects (tap changers, operational limits, regulating controls, reactive capability curves, ...).
  Its default value is `identity`.

**iidm.export.cgmes.uuid-namespace**  
Optional property related to the naming strategy specified in `iidm.export.cgmes.naming-strategy`. When new CGMES IDs have to be generated, a mechanism that ensures creation of new, stable identifiers based on IIDM IDs is used (see [RFC 4122](https://datatracker.ietf.org/doc/html/rfc4122)). These new IDs are guaranteed to be unique inside a namespace given by this UUID. By default, it is the name-based UUID fo the text "powsybl.org" in the empty namespace.

**iidm.export.cgmes.profiles**  
Optional property that determines which instance files will be exported.
By default, it is a full CGMES export: the instance files for the profiles EQ, TP, SSH and SV are exported.

**iidm.export.cgmes.modeling-authority-set**  
Optional property allowing to write a custom modeling authority set in the exported file headers. `powsybl.org` by default.
If a Boundary set is given with the property `iidm.import.cgmes.boundary-location` and the network sourcing actor is found inside it, then the modeling authority set will be obtained from the boundary file without the need to set this property.
The sourcing actor can be specified using the parameter `iidm.export.cgmes.sourcing-actor`.

**iidm.export.cgmes.model-description**  
Optional property allowing to write a custom model description in the file headers.
By default, the model description is `EQ model` for the EQ file, `TP model` for the TP file, `SSH model` for the SSH
file and `SV model` for the SV file.

**iidm.export.cgmes.export-transformers-with-highest-voltage-at-end1**  
Optional property defining whether the transformers should be exported with the highest voltage at end 1, even if it might not be the case in the IIDM model.
This property is set to `false` by default.

**iidm.export.cgmes.export-load-flow-status**  
Optional property that indicates whether the loadflow status (`converged` or `diverged`) should be
written for the `TopologicalIslands` in the SV file. If `true`, the status will be computed by checking, for every bus,
if the voltage and angle are valid, and if the bus is respecting Kirchhoff's first law. For the latter, we check that
the sums of active power and reactive power at the bus are higher than a threshold defined by the properties
`iidm.export.cgmes.max-p-mismatch-converged` and `iidm.export.cgmes.max-q-mismatch-converged`.
This property is set to `true` by default.

**iidm.export.cgmes.max-p-mismatch-converged**  
Optional property that defines the threshold below which a bus is considered to be balanced for the load flow status of the `TopologicalIsland` in active power. If the sum of all the active power of the terminals connected to the bus is greater than this threshold, then the load flow is considered to be divergent. Its default value is `0.1`, and it should be used only if the `iidm.export.cgmes.export-load-flow-status` property is set to `true`.

**iidm.export.cgmes.max-q-mismatch-converged**  
Optional property that defines the threshold below which a bus is considered to be balanced for the load flow status of the `TopologicalIsland` in reactive power. If the sum of all the reactive power of the terminals connected to the bus is greater than this threshold, then the load flow is considered to be divergent. Its default value is `0.1`, and it should be used only if the `iidm.export.cgmes.export-load-flow-status` property is set to `true`.

**iidm.export.cgmes.export-sv-injections-for-slacks**  
Optional property to specify if the total mismatch left after power flow calculation at IIDM slack buses should be exported as an SvInjection.
This property is set to `true` by default.

**iidm.export.cgmes.sourcing-actor**  
Optional property allowing to specify a custom sourcing actor. If a Boundary set with reference data is provided for the export through the parameter `iidm.import.cgmes.boundary-location`, the value of this property will be used to look for the modelling authority set and the geographical region to be used in the export.
No default value is given.
If this property is not given, the export process will still try to determine the sourcing actor from the IIDM network if it only contains one country.

**iidm.export.cgmes.model-version**  
Optional property defining the version of the exported CGMES file. It will be used if the version is not already available in the network.
The version will be written in the header of each exported file and will also be used to generate a unique UUID for the `FullModel` field.
Its default value is 1.

**iidm.export.cgmes.business-process**  
The business process in which the export takes place. This is used to generate unique UUIDs for the EQ, TP, SSH and SV file `FullModel`.
Its default value is `1D`.

**iidm.export.cgmes.cgm_export**
Optional property to specify the export use-case: IGM (Individual Grid Model) or CGM (Common Grid Model).
To export instance files of a CGM, set the value to `True`. The default value is `False` to export network as an IGM.

**iidm.export.cgmes.update-dependencies**
Optional property to determine if dependencies in the exported instance files should be managed automatically. The default value is `True`.
