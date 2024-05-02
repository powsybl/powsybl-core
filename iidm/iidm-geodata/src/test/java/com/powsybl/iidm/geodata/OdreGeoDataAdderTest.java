
package com.powsybl.iidm.geodata;

import com.powsybl.iidm.geodata.dto.LineGeoData;
import com.powsybl.iidm.geodata.dto.SubstationGeoData;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.LinePosition;
import com.powsybl.iidm.network.extensions.SubstationPosition;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Hugo Kulesza <hugo.kulesza at rte-france.com>
 */
public class OdreGeoDataAdderTest {

    private Network network;

    @BeforeEach
    public void setUp() {
        network = EurostagTutorialExample1Factory.create();
    }

    @Test
    public void addSubstationsPosition() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        SubstationGeoData p1GeoData = new SubstationGeoData("P1", "FR", coord1);
        SubstationGeoData p2GeoData = new SubstationGeoData("P2", "BE", coord2);
        List<SubstationGeoData> substationsGeoData = List.of(p1GeoData, p2GeoData);

        OdreGeoDataAdder.fillNetworkSubstationsGeoData(network, substationsGeoData);

        Substation station1 = network.getSubstation("P1");
        SubstationPosition position1 = station1.getExtension(SubstationPosition.class);
        assertNotNull(position1);
        assertEquals(coord1, position1.getCoordinate());

        Substation station2 = network.getSubstation("P2");
        SubstationPosition position2 = station2.getExtension(SubstationPosition.class);
        assertNotNull(position2);
        assertEquals(coord2, position2.getCoordinate());
    }

    @Test
    public void addLinesGeoData() {
        Coordinate coord1 = new Coordinate(1, 2);
        Coordinate coord2 = new Coordinate(3, 4);
        LineGeoData position = new LineGeoData("NHV1_NHV2_2", "FR", "BE",
                "P1", "P2", List.of(coord1, coord2));

        OdreGeoDataAdder.fillNetworkLinesGeoData(network, List.of(position));

        Line line = network.getLine("NHV1_NHV2_2");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(coord1, coord2), linePosition.getCoordinates());
    }

    @Test
    public void addSubstationsGeoDataFromFile() throws URISyntaxException {
        Path substationsPath = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/postes-electriques-rte.csv").toURI());

        OdreGeoDataAdder.fillNetworkSubstationsGeoDataFromFile(network, substationsPath);

        Coordinate coord1 = new Coordinate(2, 1);
        Substation station1 = network.getSubstation("P1");
        SubstationPosition position1 = station1.getExtension(SubstationPosition.class);
        assertNotNull(position1);
        assertEquals(coord1, position1.getCoordinate());

        Coordinate coord2 = new Coordinate(4, 3);
        Substation station2 = network.getSubstation("P2");
        SubstationPosition position2 = station2.getExtension(SubstationPosition.class);
        assertNotNull(position2);
        assertEquals(coord2, position2.getCoordinate());
    }

    @Test
    void addLinesGeoDataFromFile() throws URISyntaxException {
        Path substationsPath = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/postes-electriques-rte.csv").toURI());
        Path aerialLinesFile = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/lignes-aeriennes-rte-nv.csv").toURI());
        Path undergroundLinesFile = Paths.get(getClass()
                .getClassLoader().getResource("eurostag-test/lignes-souterraines-rte-nv.csv").toURI());

        OdreGeoDataAdder.fillNetworkLinesGeoDataFromFiles(network, aerialLinesFile,
                undergroundLinesFile, substationsPath);

        Line line = network.getLine("NHV1_NHV2_2");
        LinePosition<Line> linePosition = line.getExtension(LinePosition.class);
        assertNotNull(linePosition);
        assertEquals(List.of(new Coordinate(4, 4), new Coordinate(5, 5)),
                linePosition.getCoordinates());

        Line line2 = network.getLine("NHV1_NHV2_1");
        LinePosition<Line> linePosition2 = line2.getExtension(LinePosition.class);
        assertNotNull(linePosition2);
        assertEquals(List.of(new Coordinate(1, 1),
                        new Coordinate(2, 2),
                        new Coordinate(3, 3),
                        new Coordinate(4, 4),
                        new Coordinate(5, 5)),
                linePosition2.getCoordinates());
    }
}
