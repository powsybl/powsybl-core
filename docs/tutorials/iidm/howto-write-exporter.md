# Tutorial - Howto write an IIDM exporter

Through the powsybl's [IIDM](../../architecture/iidm/README.md) extensible [export](../../architecture/iidm/exporter/README.md) mechanism, it is possible to add new network data serialization formats to the available catalog: [XIIDM](../../architecture/iidm/importer/iidm.md), [UCTE-DEF](../../architecture/iidm/importer/ucte.md), [Entso-E CGMES](../../architecture/iidm/importer/cgmes.md), etc.

To support an additional format, a new `Exporter` java interface implementation is needed, so you'll have to:

1. Write an implementation of `com.powsybl.iidm.export.Exporter` interface and assign it an unique ID format.
2. Declare the new class as a service implementation with `@AutoService` annotation.
2. Put your compiled jar into classpath.

The new ID format will be added to the catalog of available exporters. 

The use of `@AutoService` annotation from google allows you to have the new export format (and the related Exporter implementation) recognized and automatically available from the platform.

In the following sections you will see how you can implement a new simple CSV exporter. The full exporter's project code can be found [here](../../samples/csv-exporter).  

Note that it's beyond the scope of this tutorial explaining how to develop a full network data exporter to `CSV` format: this educational implementation writes in `CSV` format some data about the network's lines.  

A sample xiidm file, that can be used to test the exporter, can be found [here](../../samples/resources/test_lines.xiidm). 

## Dependencies
In order to implement a new Exporter, first of all, you have to add some dependencies to your project:

- `powsybl-iidm-converter-api`: IIDM network import/export APIs.
- `powsybl-iidm-impl`: IIDM network model implementation.
- `com.google.auto.service`: `@Autoservice` support library.

Assuming maven is used, update the project's `pom.xml` file with the following dependencies:

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
		<version>${powsybl.version}</version>
	</dependency>
	<dependency>
	    <groupId>com.powsybl</groupId>
		<artifactId>powsybl-iidm-impl</artifactId>
		<version>${powsybl.version}</version>		
	</dependency>		
</dependencies>
```

Of course, any specific dependencies required by your exporter business logic implementation must be included in this section, too.

## Write an implementation of Exporter interface

You need to implement the `com.powsybl.iidm.export.Exporter` interface.  
Here is an empty class *template* where you will put all the `CSV` related writer code. 
 

```java
public class CsvExporter implements Exporter {

	@Override
	public String getFormat() {
		return null;
	}

	@Override
	public String getComment() {
		return null;
	}

	@Override
	public void export(Network network, Properties parameters, DataSource dataSource) {
	}

}
```

You have to declare the class as a service implementation, using `@Autoservice` annotation.  
The methods of the `Exporter` interface to override in your class are: 

 - `getFormat` method, that get a unique identifier of the format.
 - `getComment` method, that get some information about the exporter.
 - `export`: method that implements the business logic to export a model to a given format.


Here is the simple implementation, where the network's lines data is serialized to a `csv` table.

```java
@AutoService(Exporter.class)
public class CsvLinesExporter implements Exporter {

	private static final Logger LOGGER = LoggerFactory.getLogger(CsvLinesExporter.class);

	private static final String EXTENSION = "csv";
	private static final char CSV_SEPARATOR = ',';

	@Override
	public String getFormat() {
		return "CSV";
	}

	@Override
	public String getComment() {
		return "CSV exporter";
	}

	@Override
	public void export(Network network, Properties parameters, DataSource dataSource) {
		Objects.requireNonNull(network);
		Objects.requireNonNull(dataSource);
		try {
			long startTime = System.currentTimeMillis();
			OutputStream outputStream = dataSource.newOutputStream(null, EXTENSION, false);
			Writer writer = new OutputStreamWriter(outputStream);
			CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
			TableFormatterConfig tfc = new TableFormatterConfig(Locale.getDefault(), CSV_SEPARATOR, "",	true, false);

			try (TableFormatter formatter = csvTableFormatterFactory.create(writer, "", tfc,
					new Column("LineId"),
                    new Column("SubstationId1"),
                    new Column("SubstationId2"),
                    new Column("VoltageLevelId1"),
                    new Column("VoltageLevelId2"),
                    new Column("BusId1"),
                    new Column("BusId2"),
                    new Column("R"),
                    new Column("X"),
                    new Column("G1"),
                    new Column("B1"),
                    new Column("G2"),
                    new Column("B2"))) {

				for (Line line : network.getLines()) {
					String id = line.getId();
					Bus bus1 = line.getTerminal1().getBusBreakerView().getBus();
					String bus1Id = (bus1 != null) ? bus1.getId() : "";
					VoltageLevel vhl1 = (bus1 != null) ? bus1.getVoltageLevel() : null ;
					String vhl1Id = (vhl1 != null) ? vhl1.getId() : "";
					String substationId1 = (vhl1 != null) ? vhl1.getSubstation().getId() : "";
					Bus bus2 = line.getTerminal2().getBusBreakerView().getBus();
					String bus2Id = (bus2 != null) ? bus2.getId() : "";
					VoltageLevel vhl2 = (bus2 != null) ? bus2.getVoltageLevel() : null;					
					String vhl2Id = (vhl2 != null) ? vhl2.getId() : "";
					String substationId2 = (vhl2 != null) ? vhl2.getSubstation().getId() : "";
					double r = line.getR();
					double x = line.getX();
					double b1 = line.getB1();
					double b2 = line.getB2();
					double g1 = line.getG1();
					double g2 = line.getG2();
					LOGGER.debug("export lineID {} ", id);
					formatter.writeCell(id);
					formatter.writeCell(substationId1);
					formatter.writeCell(substationId2);
					formatter.writeCell(vhl1Id);
					formatter.writeCell(vhl2Id);
					formatter.writeCell(bus1Id);
					formatter.writeCell(bus2Id);
					formatter.writeCell(r);
					formatter.writeCell(x);
					formatter.writeCell(g1);
					formatter.writeCell(b1);
					formatter.writeCell(g2);
					formatter.writeCell(b2);
				}
				LOGGER.info("CSV export done in {} ms", System.currentTimeMillis() - startTime);
			}
		} catch (IOException e) {
			LOGGER.error(e.toString(), e);
			throw new UncheckedIOException(e);
		}

	}
```

The `export` method exports network data model to csv file.

The `network` input parameter of this method identifies the network data model to be exported.

The `dataSource` input parameter provides access to ouputStream.

The `parameters` input parameter can contains properties specific to this exporter (it can be used, when calling the exporter, to set properties to configure it).

The `CSV` header will be:
Line_Id,SubStation_Id1,SubStation_Id2,VoltageLevel_id1,VoltageLevel_id2,Bus_Id1,Bus_Id2,R,X,G1,B1,G2,B2

To serialize the model network to `CSV` we are using the utility class `com.powsybl.commons.io.table.CsvTableFormatter`, also provided by the Powsybl framework. 


## Update your installation with the new exporter

In the following sections we refer to installation and sample directories as:

* [\<POWSYBL_HOME\>](../configuration/directoryList.md)
* [\<POWSYBL_SAMPLES\>](../configuration/directoryList.md)

Run the following command to create your project jar:

```bash
$> cd <PROJECT_HOME>
$> mvn install
```
 
Copy the generated jar (in your project's target folder) to `<POWSYBL_HOME>/share/java/` folder (you might need to copy in this directory other dependencies jars, specific to your new exporter).


`CSV` will be listed among the available formats, as reported by the [convert-network](../../tools/convert-network.md) help command:

```bash
$> cd <POWSYBL_HOME>/bin
$>  ./itools convert-network --help
```
  
To test the new exporter with the above mentioned xiidm sample file, run this [convert-network](../../tools/convert-network.md) itools command: 

```bash
$> cd <POWSYBL_HOME>/bin
$>  ./itools convert-network --input-file <POWSYBL_SAMPLES>/resources/test_lines.xiidm --output-format CSV  --output-file /tmp/test_lines_exported.csv

```

In the output folder you can find the network converted to csv from xiidm: 
```bash
$>cat /tmp/test_lines_exported.csv
```

```csv
LineId,SubstationId1,SubstationId2,VoltageLevelId1,VoltageLevelId2,BusId1,BusId2,R,X,G1,B1,G2,B2
NHV1_NHV2_1,P1,P2,VLHV1,VLHV2,NHV1,NHV2,3.00000,33.0000,0.00000,0.000193000,0.00000,0.000193000
NHV1_NHV2_2,P1,P2,VLHV1,VLHV2,NHV1,NHV2,3.00000,32.0000,0.00000,0.000175000,0.00000,0.000175000

```



