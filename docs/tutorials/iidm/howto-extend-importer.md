# Tutorial - Howto extend the IIDM importer

[IIDM](../../architecture/iidm/README.md) data models can be loaded from files encoded in multiple file formats.
However, powsybl is not limited to the set of currently available formats: XIIDM, UCTE-DEF, Entso-E CGMES, etc. 
In fact, the framework's IIDM importer mechanism is designed to be extended to read other file formats.

To support an additional format, a new `Importer` implementation is needed, so you'll have to:

1. Create a new maven project and add all the required dependencies.
2. Write a new class that implements the `com.powsybl.iidm.import_.Importer` interface. 
3. Compile your project and add the jar to your powsybl installation.

In the following sections you will see how, following these steps, you can implement a new simple importer, able to read some network data from a CSV file.  
The full importer's project code can be found [here](../../samples/csv-importer).  

## Maven dependencies
  
After creating the Maven project, you need to add the necessary framework's dependencies to your pom.xml file.  

```xml
 <dependencies>
       	<dependency>
			<groupId>com.google.auto.service</groupId>
			<artifactId>auto-service</artifactId>
		</dependency>
		<dependency>
			<groupId>com.powsybl</groupId>
			<artifactId>powsybl-iidm-converter-api</artifactId>
		</dependency>
		<dependency>
			<groupId>com.powsybl</groupId>
			<artifactId>powsybl-iidm-impl</artifactId>
			<version>2.1.0-SNAPSHOT</version> 		
		</dependency>		
	</dependencies>
```

Any specific dependencies required by your importer business logic implementation must be declared in this section, too.

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

You have to declare the class as a service implementation, using `@Autoservice` annotation. This will allow you to have the new import format (and the related importer) recognzied and automatically available to the platform.  
The methods of the `Importer` interface to override in your class are: 
 
 - `getComment` method, that returns importer description.
 - `getFormat` method, that returns input file's format.
 - `exists`  method that verify if the input file exists.
 - `importData`: method that implement the new reader business logic
 
For instance in this class, we are loading network data from a CSV file containing two lines. A sample csv file can be found [here](../../samples/resources/test_lines.csv). 


```java
@AutoService(Importer.class)
public class CsvImporter implements Importer {
	
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
        	return datasource.exists(null, EXTENSION );
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource data, Properties props) {
    	Network network = NetworkFactory.create("SamplesNetwork", EXTENSION);
        LOGGER.debug("Start import from file {}",data.getBaseName());
        try {
            CsvReader reader = new CsvReader(data.newInputStream(null,EXTENSION), Charset.defaultCharset());
            reader.readHeaders();
            while(reader.readRecord())
            {
                String id = reader.get("ID");
                Substation s1 = network.newSubstation().setId(reader.get("S1")).setCountry(Country.FR).add();
                Substation s2 = network.newSubstation().setId(reader.get("S2")).setCountry(Country.FR).add();
                VoltageLevel vlhv1 = s1.newVoltageLevel().setId(reader.get("VL1")).setNominalV(220).setTopologyKind(TopologyKind.BUS_BREAKER).add();
                VoltageLevel vlhv2 = s2.newVoltageLevel().setId(reader.get("VL2")).setNominalV(220).setTopologyKind(TopologyKind.BUS_BREAKER).add();
                Bus nhv1 = vlhv1.getBusBreakerView().newBus().setId(reader.get("BUS1")).add();
                Bus nhv2 = vlhv2.getBusBreakerView().newBus().setId(reader.get("BUS2")).add();
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
            LOGGER.debug("Network imported successfully");
            return network;
            
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
            return null;
        }
    }

```

The `importData` method is in charge of executing your import.  

The `data` parameter  provides access to inputStream, fileName and methods to verify if input file exists.

The `prop` parameter can be used to set properties to configure the import.

The rest of the code in our sample class parse a CSV file using CSV Reader, a class belong to [JAVACSV](https://sourceforge.net/projects/javacsv/) and loads network data, using IIDM API.

In order to use `javacsv` library you have to add a new dependency in pom.xml configuration file.
 
```xml
<dependency>
    <groupId>net.sourceforge.javacsv</groupId>
	<artifactId>javacsv</artifactId>
	<version>2.0</version>
</dependency>
```

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
$>  ./itools convert-network --input-file <POWSYBL_SAMPLES>/resources/test_lines.csv  --output-format XIIDM --output-file /tmp/test_csv_to_xiidm

```

In the output folder you can find the network converted from csv to xiidm: 
```bash
$>more /tmp/test_csv_to_xiidm
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<iidm:network xmlns:iidm="http://www.itesla_project.eu/schema/iidm/1_0" id="SamplesNetwork" caseDate="2018-09-06T15:09:13.953Z" forecastDistance="0" sourceFormat
="csv">
    <iidm:substation id="s1" country="FR">
        <iidm:voltageLevel id="v1" nominalV="220.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="b1"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
    </iidm:substation>
    <iidm:substation id="s2" country="FR">
        <iidm:voltageLevel id="v2" nominalV="220.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="b2"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
    </iidm:substation>
    <iidm:substation id="s3" country="FR">
        <iidm:voltageLevel id="v3" nominalV="220.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="b3"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
    </iidm:substation>
    <iidm:substation id="s4" country="FR">
        <iidm:voltageLevel id="v4" nominalV="220.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="b4"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
    </iidm:substation>
    <iidm:line id="1" r="3.0" x="32.0" g1="0.0" b1="1.75E-4" g2="0.0" b2="1.75E-4" bus1="b1" connectableBus1="b1" voltageLevelId1="v1" bus2="b2" connectableBus2=
"b2" voltageLevelId2="v2"/>
    <iidm:line id="2" r="3.0" x="32.0" g1="0.0" b1="1.75E-4" g2="0.0" b2="1.75E-4" bus1="b3" connectableBus1="b3" voltageLevelId1="v3" bus2="b4" connectableBus2=
"b4" voltageLevelId2="v4"/>
</iidm:network>
```


