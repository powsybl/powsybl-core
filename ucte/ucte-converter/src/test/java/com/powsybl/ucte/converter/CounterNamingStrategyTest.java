package com.powsybl.ucte.converter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.ucte.network.UcteNodeCode;
import com.powsybl.ucte.network.UcteElementId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CounterNamingStrategyTest {

    private Network network;
    private CounterNamingStrategy strategy;

    @BeforeEach
    void setUp() {
        ResourceDataSource dataSource = new ResourceDataSource("network", new ResourceSet("/", "network.xiidm"));
        network = Network.read(dataSource);
        strategy = new CounterNamingStrategy();
        strategy.initialiseNetwork(network);
    }

    @Test
    void testName() {
        assertEquals("Counter", strategy.getName());
    }

    @Test
    void testBasicNodeCodeGeneration() {
        Bus genBus = network.getBusBreakerView().getBus("NGEN");
        Bus hv1Bus = network.getBusBreakerView().getBus("NHV1");
        Bus hv2Bus = network.getBusBreakerView().getBus("NHV2");
        Bus loadBus = network.getBusBreakerView().getBus("NLOAD");

        UcteNodeCode genCode = strategy.getUcteNodeCode(genBus);
        UcteNodeCode hv1Code = strategy.getUcteNodeCode(hv1Bus);
        UcteNodeCode hv2Code = strategy.getUcteNodeCode(hv2Bus);
        UcteNodeCode loadCode = strategy.getUcteNodeCode(loadBus);

        assertAll(
                () -> assertEquals(8, genCode.toString().length()),
                () -> assertEquals(8, hv1Code.toString().length()),
                () -> assertEquals(8, hv2Code.toString().length()),
                () -> assertEquals(8, loadCode.toString().length())
        );

        assertAll(
                () -> assertNotEquals(genCode, hv1Code),
                () -> assertNotEquals(genCode, hv2Code),
                () -> assertNotEquals(genCode, loadCode),
                () -> assertNotEquals(hv1Code, hv2Code),
                () -> assertNotEquals(hv1Code, loadCode),
                () -> assertNotEquals(hv2Code, loadCode)
        );
    }

    @Test
    void testTransformerElementIds() {
        Branch<?> transformer1 = network.getBranch("NGEN_NHV1");
        Branch<?> transformer2 = network.getBranch("NHV2_NLOAD");

        UcteElementId id1 = strategy.getUcteElementId(transformer1);
        UcteElementId id2 = strategy.getUcteElementId(transformer2);

        assertAll(
                () -> assertNotNull(id1),
                () -> assertNotNull(id2),
                () -> assertEquals(19, id1.toString().length()),
                () -> assertEquals(19, id2.toString().length()),
                () -> assertNotEquals(id1, id2)
        );
    }

    @Test
    void testParallelLines() {
        Branch<?> line1 = network.getBranch("NHV1_NHV2_1");
        Branch<?> line2 = network.getBranch("NHV1_NHV2_2");

        UcteElementId id1 = strategy.getUcteElementId(line1);
        UcteElementId id2 = strategy.getUcteElementId(line2);

        assertAll(
                () -> assertNotNull(id1),
                () -> assertNotNull(id2),
                () -> assertEquals(19, id1.toString().length()),
                () -> assertEquals(19, id2.toString().length()),
                () -> assertNotEquals(id1, id2)
        );
    }

    @Test
    void testVoltageLevelCodes() {
        Bus genBus = network.getBusBreakerView().getBus("NGEN");     // 27.0 kV
        Bus hv1Bus = network.getBusBreakerView().getBus("NHV1");     // 380.0 kV
        Bus loadBus = network.getBusBreakerView().getBus("NLOAD");   // 150.0 kV

        UcteNodeCode genCode = strategy.getUcteNodeCode(genBus);
        UcteNodeCode hv1Code = strategy.getUcteNodeCode(hv1Bus);
        UcteNodeCode loadCode = strategy.getUcteNodeCode(loadBus);

        assertAll(
                () -> assertEquals('7', genCode.toString().charAt(6)),
                () -> assertEquals('1', hv1Code.toString().charAt(6)),
                () -> assertEquals('3', loadCode.toString().charAt(6))
        );
    }

    @Test
    void testNullAndInvalidIds() {
        assertAll(
                () -> assertThrows(PowsyblException.class, () -> strategy.getUcteNodeCode((String) null)),
                () -> assertThrows(PowsyblException.class, () -> strategy.getUcteElementId((String) null)),
                () -> assertThrows(UcteException.class, () -> strategy.getUcteNodeCode("INVALID_ID")),
                () -> assertThrows(UcteException.class, () -> strategy.getUcteElementId("INVALID_ID"))
        );
    }

    @Test
    void testCountryCode() {
        Bus genBus = network.getBusBreakerView().getBus("NGEN");
        UcteNodeCode code = strategy.getUcteNodeCode(genBus);
        assertEquals('F', code.toString().charAt(0)); // France
    }

}