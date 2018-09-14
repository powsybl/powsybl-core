# Tutorial - Howto extend the IIDM importer

[IIDM](../../architecture/iidm/README.md) data models can be loaded from files encoded in multiple file formats.  
powsybl is not limited to the set of currently available formats (XIIDM, UCTE-DEF, Entso-E CGMES, etc): the framework's IIDM importer mechanism is designed to be extended to read a network from other file formats.  

To support an additional format, a new `Importer` implementation is needed, so you'll have to:

1. Create a new maven project and add all the required dependencies.
2. Write a new class that implements the `com.powsybl.iidm.import_.Importer` interface. 
3. Compile your project and add the jar to your powsybl installation.

In the following sections you will see how, following these steps, you can implement a new simple importer, able to read network data from a CSV file.  
The purpose of this tutorial is to provide a simple example of how to implement an IIDM importer, not to actually implement a full network importer from csv data.  
Therefore the csv data to be imported is for demostration purposes only (it just contains some lines data). It's beyond the scope of this tutorial to explain how to represent a full network data in csv files.  
The full importer's project code can be found [here](../../samples/csv-importer).  
A sample csv file, that can be used by the importer, can be found [here](../../samples/resources/test_lines.csv).  
The file contains two lines, as showed in the following table:

| LineId | SubStation Id1 | SubStation Id2 | VoltageLevel Id1 | VoltageLevel Id2 | Bus Id1 | Bus Id2|  R Resistence | X Reactance | G1 First Shunt Conduttance |  B1 first shunt susceptance | G2  second side shunt conductance  | B2 B1 first shunt susceptance |
| ------ | -------------- | -------------- | ---------------- | ---------------- | ------- | ------ | - | - | - | - | - | - |
| NHV1_NHV2_1 | P1 | P2 | VLHV1 | VLHV2 | NHV1 | NHV2 | 3  | 33.0 | 0.0 | 1.93E-4 | 0.0 | 1.93E-4|
| NHV1_NHV2_2 | P1 | P2 | VLHV1 | VLHV2 | NHV1 | NHV2 | 3  | 32.0 | 0.0 | 1.75E-4 | 0.0 | 1.75E-4|


## Maven dependencies
  
After creating the Maven project, you need to add the necessary framework's dependencies to your pom.xml file.  

```xml
<dependencies>
    <dependency>
        <groupId>com.google.auto.service</groupId>
        <artifactId>auto-service</artifactId>
        <version>1.0-rc2</version>
    </dependency>
    <dependency>
        <groupId>com.powsybl</groupId>
        <artifactId>powsybl-iidm-converter-api</artifactId>
        <version>${project.version}</version>
    </dependency>
    <dependency>
        <groupId>com.powsybl</groupId>
        <artifactId>powsybl-iidm-impl</artifactId>
        <version>${project.version}</version>
    </dependency>
</dependencies>
```

Any specific dependencies required by your importer business logic implementation must be declared in this section, too.  
E.g. in our sample implementation we use `javacsv` library, so we have to add a new dependency in pom.xml configuration file.
 
```xml
<dependency>
    <groupId>net.sourceforge.javacsv</groupId>
    <artifactId>javacsv</artifactId>
    <version>2.0</version>
</dependency>
```

## Implement the Importer interface

You need to implement the `com.powsybl.iidm.import_.Importer` interface.  
Here is an empty class *template* where you will put all the `CSV` related reader code. 
 

```java
@AutoService(Importer.class)
public class CsvImporter implements Importer {

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        return false;
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {
        return null;
    }
}
```

You have to declare the class as a service implementation, using `@Autoservice` annotation. This will allow you to have the new import format (and the related importer) recognzied and automatically available in the platform.  
The methods of the `Importer` interface to override in your class are: 
 
 - `getComment` method, that returns the importer description.
 - `getFormat` method, that returns the input file's format.
 - `exists`  method that verify if the input file exists.
 - `importData`: method that implement the new importer business logic
 
For instance in this class, we are loading network lines data from a CSV file.  


```java
@AutoService(Importer.class)
public class CsvLinesImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvImporter.class);

    private static final String EXTENSION = "csv";

    @Override
    public String getFormat() {
        return "CSV";
    }
    
    @Override
    public String getComment() {
        return "CSV importer";
    }
    
    @Override
    public boolean exists(ReadOnlyDataSource datasource) {
        try {
            return datasource.exists(null, EXTENSION);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource data, Properties props) {
        Network network = NetworkFactory.create("Network_2Lines_Example", EXTENSION);
        LOGGER.debug("Start import from file {}", data.getBaseName());
        long startTime = System.currentTimeMillis();
        try {
            CsvReader reader = new CsvReader(data.newInputStream(null, EXTENSION), Charset.defaultCharset());
            reader.readHeaders();
            while(reader.readRecord()) {
                String id = reader.get("LineId");   
                LOGGER.info("import lineID {} ", id);
                Substation s1 = getSubStation(reader.get("SubStationId1"), network, Country.FR);
                Substation s2 = getSubStation(reader.get("SubStationId2"), network, Country.FR);
                VoltageLevel vlhv1 = getVoltageLevel(reader.get("VoltageLevelId1"), network, s1, 220, TopologyKind.BUS_BREAKER);
                VoltageLevel vlhv2 = getVoltageLevel(reader.get("VoltageLevelId2"), network, s2, 220, TopologyKind.BUS_BREAKER);
                Bus nhv1 = getBus(vlhv1, reader.get("BusId1")) ;
                Bus nhv2 = getBus(vlhv2, reader.get("BusId2")) ;
                network.newLine()
                       .setId(id)
                       .setVoltageLevel1(vlhv1.getId())
                       .setVoltageLevel2(vlhv2.getId())
                       .setBus1(nhv1.getId())
                       .setConnectableBus1(nhv1.getId())
                       .setBus2(nhv2.getId())
                       .setConnectableBus2(nhv2.getId())
                       .setR(Double.valueOf(reader.get("R")))
                       .setX(Double.valueOf(reader.get("X")))
                       .setG1(Double.valueOf(reader.get("G1")))
                       .setB1(Double.valueOf(reader.get("B1")))
                       .setG2(Double.valueOf(reader.get("G2")))
                       .setB2(Double.valueOf(reader.get("B2")))
                       .add();
            }
            LOGGER.debug("{} import done in {} ms", EXTENSION, System.currentTimeMillis() - startTime);
            return network;
            
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
            return null;
        }
    }    
        
    private Substation getSubStation(String id, Network network, Country country) {
        return (network.getSubstation(id) == null) ? network.newSubstation().setId(id).setCountry(country).add() : network.getSubstation(id);    
    }
    
    private Bus getBus(VoltageLevel vlhv, String id) {
        return (vlhv.getBusBreakerView().getBus(id) == null) ? vlhv.getBusBreakerView().newBus().setId(id).add() : vlhv.getBusBreakerView().getBus(id);
    }
    
    private VoltageLevel getVoltageLevel(String id, Network network, Substation s, double nominalVoltage,TopologyKind topologyKind ) {
        return (network.getVoltageLevel(id) == null) ? s.newVoltageLevel().setId(id).setNominalV(nominalVoltage).setTopologyKind(topologyKind).add() : network.getVoltageLevel(id);    
    }
}
```

The `importData` method is in charge of executing your import.  

The `data` parameter  provides access to inputStream, fileName and methods to verify if input file exists.

The `prop` parameter can be used to set properties to configure the import.

The rest of the code in our sample class parse a CSV file using CSV Reader, a class belong to [JAVACSV](https://sourceforge.net/projects/javacsv/) and loads network data, using IIDM API.

## Update your installation with the new importer

Run the following command to create your project jar:

```bash
$> cd <PROJECT_HOME>
$> mvn install
```

Copy the generated jar (in your project's target folder) and javacsv.jar to `<POWSYBL_HOME>/share/java/` folder (you might need to copy in this directory other dependencies jars, specific to your new importer).  
  
To test the new importer, run an itools command that involve a network import. 
For instance [convert-network](../../tools/convert-network.md):

```bash
$> cd <POWSYBL_HOME>/bin
$> ./itools convert-network --input-file <POWSYBL_SAMPLES>/resources/test_lines.csv  --output-format XIIDM --output-file /tmp/test_csv_to_xiidm

```

In the output folder you can find the network converted from csv to xiidm: 
```bash
$> more /tmp/test_csv_to_xiidm
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<iidm:network xmlns:iidm="http://www.itesla_project.eu/schema/iidm/1_0" id="Network_2Lines_Example" caseDate="2018-09-10T14:38:11.828Z" forecastDistance="0" sour
ceFormat="csv">
    <iidm:substation id="P1" country="FR">
        <iidm:voltageLevel id="VLHV1" nominalV="220.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="NHV1"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
    </iidm:substation>
    <iidm:substation id="P2" country="FR">
        <iidm:voltageLevel id="VLHV2" nominalV="220.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="NHV2"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
    </iidm:substation>
    <iidm:line id="NHV1_NHV2_1" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" bus1="NHV1" connectableBus1="NHV1" voltageLevelId1="VLHV1" bus2="NHV
2" connectableBus2="NHV2" voltageLevelId2="VLHV2"/>
    <iidm:line id="NHV1_NHV2_2" r="3.0" x="32.0" g1="0.0" b1="1.75E-4" g2="0.0" b2="1.75E-4" bus1="NHV1" connectableBus1="NHV1" voltageLevelId1="VLHV1" bus2="NHV
2" connectableBus2="NHV2" voltageLevelId2="VLHV2"/>
</iidm:network>

```


