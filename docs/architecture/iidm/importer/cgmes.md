# CGMES Importer

The **C**ommon **G**rid **M**odel **E**xchange **S**pecification (**CGMES**) is an IEC technical specification (TS) based on the IEC Common Information Model (CIM) family of standards.​ It was developed to meet necessary requirements for TSO data exchanges in the areas of system development and system operation.

Current supported version of CGMES is 2.4.15, that is based on CIM 16.

## Use the CGMES importer
To support CGMES files in your project, you have to add the following dependencies to your `pom.xml` file.
```xml
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-cgmes-conversion</artifactId>
    <version>${powsybl.version}</version>
</dependency>
<dependency>
    <groupId>com.powsybl</groupId>
    <artifactId>powsybl-cgmes-model</artifactId>
    <version>${powsybl.version}</version>
</dependency>
```

## References
- [Common Grid Model Exchange Specification (CGMES)](https://www.entsoe.eu/digital/common-information-model/#common-grid-model-exchange-specification-cgmes).
- Sample files from [ENTSO-E Test Configurations for Conformity Assessment Scheme v2.0](https://docstore.entsoe.eu/Documents/CIM_documents/Grid_Model_CIM/TestConfigurations_packageCASv2.0.zip)

