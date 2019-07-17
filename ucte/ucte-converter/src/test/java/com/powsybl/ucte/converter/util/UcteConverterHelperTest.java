package com.powsybl.ucte.converter.util;

import static com.powsybl.ucte.converter.util.UcteConverterHelper.*;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.ucte.converter.UcteImporter;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class UcteConverterHelperTest {

    @Test
    public void calculatePhaseDuTest() {
        Network transfomerRegulationNetwork = loadNetworkFromResourceFile("/transformerRegulation.uct");
        assertEquals(
                2.0000, calculatePhaseDu(transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")),
                0.00001);
        assertNotEquals(
                2.0001, calculatePhaseDu(transfomerRegulationNetwork.getTwoWindingsTransformer("0BBBBB5  0AAAAA2  1")),
                0.00001);
    }

    /**
     * Utility method to load a network file from resource directory without calling
     * @param filePath path of the file relative to resources directory
     * @return imported network
     */
    private static Network loadNetworkFromResourceFile(String filePath) {
        ReadOnlyDataSource dataSource = new ResourceDataSource(FilenameUtils.getBaseName(filePath), new ResourceSet(FilenameUtils.getPath(filePath), FilenameUtils.getName(filePath)));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), null);
    }

}
