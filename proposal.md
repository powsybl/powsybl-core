Hi! Some of you may already know it but one objective for the next release is to homogeneize the naming strategy for **powsybl-core** modules and packages.

This first table sums up the rules of my proposed new naming strategy according to standards I have found.

| Type | ArtifactId | Automatic module name | Package name |
| - | - | - | - |
| API | powsybl-<API_NAME>-api | com.powsybl.<API_NAME> | com.powsybl.<API_NAME> |
| Network model | powsybl-<MODEL_NAME>-network | com.powsybl.<MODEL_NAME>.network | com.powsybl.<MODEL_NAME>.network |
| Converter IIDM-<MODEL_NAME> (import and/or export) | powsybl-<MODEL_NAME>-(optional)<FILE_FORMAT>-converter | com.powsybl.<MODEL_NAME>.(optional)<FILE_FORMAT>.converter | com.powsybl.<MODEL_NAME>.(optional)<FILE_FORMAT>.converter | 
| Model integration | powsybl-<MODEL_NAME>-integration | com.powsybl.<MODEL_NAME> | com.powsybl.<MODEL_NAME> |

In this other table, I listed all the **powsybl-core** modules and the proposed changes according to these rules. I put changes I am not really sure about in **bold** in the table. Please do not hesitate to give me feedback about it, the goal is to start a discussion!

| ArtifactId |  Proposed changes | Reason |
| - | - | - |
| powsybl-action-dsl | None | N/A |
| powsybl-action-dsl-spi | None | N/A |
| powsybl-action-simulator | None | N/A |
| powsybl-action-util | None | N/A |
| powsybl-afs-core | None | N/A |
| powsybl-afs-ext-base | None | N/A |
| powsybl-afs-local | None | N/A |
| powsybl-afs-mapdb | None | N/A |
| powsybl-afs-mapdb-storage | None | N/A |
| powsybl-afs-network-client | None | N/A |
| powsybl-afs-network-server | None | N/A |
| powsybl-afs-storage-api | automatic module name: com.powsybl.afs.storage.api -> com.powsybl.afs.storage | API naming strategy |
| powsybl-afs-ws-client | None | N/A |
| powsybl-afs-ws-client-utils | None | N/A |
| powsybl-afs-ws-server | None | N/A |
| powsybl-afs-ws-server-utils | None | N/A |
| powsybl-afs-ws-storage | None | N/A |
| powsybl-afs-ws-utils | None | N/A |
| powsybl-ampl-converter | moved in iidm module, artifactId: powsybl-ampl-converter -> powsybl-iidm-dsv-converter, automatic module name: com.powsybl.ampl.converter -> com.powsybl.iidm.dsv.converter, package: com.powsybl.ampl.converter -> com.powsybl.iidm.dsv.converter | This module does not converts IIDM networks into AMPL networks, it converts them in Delimiter-separated values (DSV) files compatible with AMPL processing |
| **powsybl-cgmes-conformity** | **package: com.powsybl.cgmes.conformity.test -> com.powsybl.cgmes.conformity OR artifactId: powsybl-cgmes-conformity -> powsybl.cgmes-conformity-test and automatic module name: com.powsybl.cgmes.conformity -> com.powsybl.cgmes.conformity.test** | **Good practice is to have the automatic module name identical to root package name** |
| powsybl-cgmes-conversion | artifactId: powsybl-cgmes-conversion -> powsybl-cgmes-converter, automatic module name: com.powsybl.cgmes.conversion -> com.powsybl.cgmes.converter, package: com.powsybl.cgmes.conversion -> com.powsybl.cgmes.converter | Converter naming strategy |
| powsybl-cgmes-model | artifactId: powsybl-cgmes-model -> powsybl-cgmes-network, automatic module name: com.powsybl.cgmes.model -> com.powsybl.cgmes.network, package: com.powsybl.cgmes.model -> com.powsybl.cgmes.network | Network model naming strategy |
| **powsybl-cgmes-model-alternatives** | **package: com.powsybl.cgmes.alternatives.test -> com.powsybl.cgmes.alternatives OR artifactId: powsybl-cgmes-model-alternatives -> powsybl.cgmes-model-alternatives-test and automatic module name: com.powsybl.cgmes.alternatives -> com.powsybl.cgmes.alternatives.test** | **Good practice is to have the automatic module name identical to root package name** |
| powsybl-cim-anonymiser | package: com.powsybl.cim -> com.powsybl.cim.anonymiser | Good practice is to have the automatic module name identical to root package name and com.powsybl.cim is too generic regarding what the module does |
| powsybl-commons | None | N/A |
| powsybl-computation | artifactId: powsybl-computation -> powsybl-computation-api, automatic module name: com.powsybl.computation.api -> com.powsybl.computation | API naming strategy |
| powsybl-computation-local | None | N/A |
| powsybl-computation-mpi | None | N/A |
| powsybl-contingency-api | automatic module name: com.powsybl.contingency.api -> com.powsybl.contingency | API naming strategy |
| powsybl-contingency-dsl | None | N/A |
| powsybl-dsl | None | N/A |
| powsybl-entsoe-util | None | N/A |
| **powsybl-iidm-api** | **artifactId: powsybl-iidm-api -> keep OR powsybl-iidm-network, automatic module name: com.powsybl.iidm.api -> com.powsybl.iidm OR com.powsybl.iidm.network, package: com.powsybl.iidm.network -> keep OR com.powsybl.iidm** | **Network model naming strategy OR API naming strategy** |
| powsybl-iidm-converter-api | Deleted and merged into powsybl-iidm-api | Issue   [#683](https://github.com/powsybl/powsybl-core/issues/683) |
| **powsybl-iidm-impl** | **package: com.powsybl.iidm.network.impl -> com.powsybl.iidm.impl OR automatic module name: com.powsybl.iidm.impl -> com.powsybl.iidm.network.impl** | **Good practice is to have the automatic module name identical to root package name** |
| powsybl-iidm-reducer | None | N/A |
| **powsybl-iidm-test** | **package: com.powsybl.iidm.network.test -> com.powsybl.iidm.test OR automatic module name: com.powsybl.iidm.test -> com.powsybl.iidm.network.test** | **Good practice is to have the automatic module name identical to root package name** |
| **powsybl-iidm-util** | **package: com.powsybl.iidm.network.util -> com.powsybl.iidm.util OR automatic module name: com.powsybl.iidm.util -> com.powsybl.iidm.network.util** | **Good practice is to have the automatic module name identical to root package name** |
| powsybl-iidm-xml-converter | package: com.powsybl.iidm.xml -> com.powsybl.iidm.xml.converter, automatic module name: com.powsybl.iidm.xml -> com.powsybl.iidm.xml.converter | Converter naming strategy |
| powsybl-itools-packager | None | N/A |
| powsybl-loadflow-api | automatic module name: com.powsybl.loadflow.api -> com.powsybl.loadflow | API naming strategy |
| powsybl-loadflow-results-completion | package: com.powsybl.loadflow.resultscompletion -> com.powsybl.loadflow.results.completion | Good practice is to have the automatic module name identical to root package name |
| powsybl-loadflow-validation | None | N/A | 
| powsybl-math | None | N/A |
| powsybl-scripting | None | N/A |
| powsybl-security-analysis-afs | None | N/A | 
| powsybl-security-analysis-afs-local | None | N/A | 
| powsybl-security-analysis-api | None | N/A |
| powsybl-sensitivity-api | automatic module name: com.powsybl.sensitivity.api -> com.powsybl.sensitivity | API naming strategy |
| powsybl-simulation-api | automatic module name: com.powsybl.simulation.api -> com.powsybl.simulation | API naming strategy |
| powsybl-time-series-api | automatic module name: com.powsybl.timeseries.api -> com.powsybl.timeseries | API naming strategy |
| powsybl-tools | None | N/A |
| powsybl-triple-store-api | automatic module name: com.powsybl.triplestore.api -> com.powsybl.triplestore, package: com.powsybl.triplestore.api -> com.powsybl.triplestore | API naming strategy |
| powsybl-triple-store-impl-blazegraph | None | N/A |
| powsybl -triple-store-impl-jena | None | N/A |
| powsybl-triple-store-impl-rdf4j | None | N/A |
| powsybl-triple-store-test | None | N/A |
| powsybl-ucte-converter | None | N/A |
| powsybl-ucte-network | None | N/A |
