# Geographical positions extensions

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
