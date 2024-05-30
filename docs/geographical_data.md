Network geographical data
=========================

Geographical positions extensions
---------------------------------

It is possible to add geographical positions to a network using dedicated extensions.

For these extensions, all coordinates are described in (latitude, longitude).

There are two such extensions :

* [SubstationPosition](https://javadoc.io/doc/com.powsybl/powsybl-iidm-extensions/latest/com.powsybl.iidm.extensions/com/powsybl/iidm/network/extensions/SubstationPosition.html) can be used to add the position of substation :

```java
Substation station = network.getSubstation("P1")
station.newExtension(SubstationPositionAdder.class)
        .withCoordinate(new Coordinate(48, 2))
        .add();
Coordinate stationCoordinate = station.getExtension(SubstationPosition.class)
        .getCoordinate();
```

* [LinePosition](https://javadoc.io/doc/com.powsybl/powsybl-iidm-extensions/latest/com.powsybl.iidm.extensions/com/powsybl/iidm/network/extensions/LinePosition.html) can be used to add the list of coordinates describing the position of a line :

```java
Line line = network.getLine("L1");
line.newExtension(LinePositionAdder.class)
        .withCoordinates(List.of(new Coordinate(48, 2), new Coordinate(48.1, 2.1)))
        .add();
List<Coordinate> lineCoordinates = line.getExtension(LinePosition.class)
        .getCoordinates();
```

Geographical data import post-processor
---------------------------------------

One way to add geographical positions on a network is to use the import post-processor named OdreGeoDataAdderPostProcessor, that will automatically add the geographical positions extensions to the network model.

This processor uses geographical position data formatted in multiple csv files, as it can be obtained on the website [OpenData Réseaux-Energie](https://odre.opendatasoft.com) for the network of the French TSO RTE.
Here are the links to obtain the RTE data CSV files, to be used as reference for input data formatting :

| Network element type | RTE data CSV file link                                                                                 |
|----------------------|--------------------------------------------------------------------------------------------------------|
| Substations          | [https://odre.opendatasoft.com/api/explore/v2.1/catalog/datasets/postes-electriques-rte/exports/csv]()     |
| Aerial lines         | [https://odre.opendatasoft.com/api/explore/v2.1/catalog/datasets/lignes-aeriennes-rte-nv/exports/csv]()    |
| Underground lines    | [https://odre.opendatasoft.com/api/explore/v2.1/catalog/datasets/lignes-souterraines-rte-nv/exports/csv]() |

To use this import post-processor, two things must be added to the PowSyBl configuration file.

Firstly, to activate the post-processor :

```yaml
import:
  postProcessors:
    - odreGeoDataImporter
```
    
Then, to precise where to find the data files :

```yaml
odre-geo-data-importer-post-processor:
  substations: /path/to/substations.csv
  aerial-lines: /path/to/aerial-lines.csv
  underground-lines: /path/to/underground-lines.csv
```

The paths to the different files can be absolute paths or paths relative to the directory where your command is launched.