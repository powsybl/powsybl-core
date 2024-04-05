package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestIssue2878CGMboundaryWithSubstation {

    private GridModelReferenceResources cgmWithSubstationInsideBoundary() {
        return new GridModelReferenceResources(
                "CGM-with-substation-inside-boundary",
                null,
                new ResourceSet("/issues/CGM-with-substation-inside-BD",
                        "20171002T0930Z_ENTSO-E_EQ_BD_2.xml",
                        "20210325T1530Z_1D_BE_EQ_001.xml",
                        "20210325T1530Z_1D_NL_EQ_001.xml"
                )
        );
    }

    private Set<String> expectedSubstations() {
        return Set.of("PP_Brussels", "Anvers", "PP_Amsterdam", "HVDC Station 2");
    }

    @Test
    void testImportWithSubneworks() {
        Network network = Network.read(cgmWithSubstationInsideBoundary().dataSource());
        assertNotNull(network);
        assertEquals(expectedSubstations(), network.getSubstationStream().map(Substation::getNameOrId).collect(Collectors.toSet()));
    }

    @Test
    void testImportAssembled() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Network.read(cgmWithSubstationInsideBoundary().dataSource(), importParams);
        assertNotNull(network);
        assertEquals(expectedSubstations(), network.getSubstationStream().map(Substation::getNameOrId).collect(Collectors.toSet()));
    }
}
