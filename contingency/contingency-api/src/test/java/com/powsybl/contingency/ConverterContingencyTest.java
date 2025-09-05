package com.powsybl.contingency;

import com.google.common.testing.EqualsTester;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.iidm.modification.tripping.ConverterTripping;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.DcDetailedNetworkFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
class ConverterContingencyTest {

    @Test
    void test() {
        Contingency contingency = Contingency.converter("id");
        assertEquals("id", contingency.getId());
        assertEquals(1, contingency.getElements().size());
        assertEquals(ContingencyElementType.CONVERTER, contingency.getElements().get(0).getType());

        ConverterContingency convContingency = new ConverterContingency("id");
        assertEquals("id", convContingency.getId());
        assertEquals(ContingencyElementType.CONVERTER, convContingency.getType());

        assertNotNull(convContingency.toModification());
        assertInstanceOf(ConverterTripping.class, convContingency.toModification());

        new EqualsTester()
                .addEqualityGroup(new ConverterContingency("conv1"), new ConverterContingency("conv1"))
                .addEqualityGroup(new ConverterContingency("conv2"), new ConverterContingency("conv2"))
                .testEquals();
    }

    @Test
    void test2() {
        Network network = DcDetailedNetworkFactory.createVscSymmetricalMonopole();
        ContingencyList contingencyList = ContingencyList.of(Contingency.converter("VscFr"), Contingency.converter("unknown"));
        List<Contingency> contingencies = contingencyList.getContingencies(network);
        assertEquals(1, contingencies.size());

        ConverterContingency convCtg = (ConverterContingency) contingencies.get(0).getElements().get(0);
        assertEquals("VscFr", convCtg.getId());
    }
}
