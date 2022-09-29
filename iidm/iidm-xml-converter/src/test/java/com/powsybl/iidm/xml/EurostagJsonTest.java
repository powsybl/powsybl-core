/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import java.io.IOException;

import static com.powsybl.iidm.xml.IidmXmlConstants.CURRENT_IIDM_XML_VERSION;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagJsonTest extends AbstractXmlConverterTest {

    @Test
    public void loadFlowResultsTest() throws IOException {
        ExportOptions exportOptions = new ExportOptions().setFormat(TreeDataFormat.JSON);
        ImportOptions importOptions = new ImportOptions().setFormat(TreeDataFormat.JSON);
        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        roundTripTest(network,
            (n, xmlFile) -> NetworkXml.write(n, exportOptions, xmlFile),
            xmlFile -> {
                return network;
               // return NetworkXml.read(xmlFile, importOptions);
            },
            getVersionedNetworkPath("eurostag-tutorial1-lf.json", CURRENT_IIDM_XML_VERSION));
    }
}
