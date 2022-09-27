/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagJsonTest extends AbstractXmlConverterTest {

    @Test
    @Ignore
    public void loadFlowResultsTest() throws IOException {
        ExportOptions exportOptions = new ExportOptions().setFormat(IidmFormat.JSON);
        ImportOptions importOptions = new ImportOptions().setFormat(IidmFormat.JSON);
        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        NetworkXml.write(network, exportOptions, System.out);
//        roundTripTest(network,
//            (n, xmlFile) -> NetworkXml.write(n, exportOptions, xmlFile),
//            xmlFile -> {
//                return network;
//               // return NetworkXml.read(xmlFile, importOptions);
//            },
//            getVersionedNetworkPath("eurostag-tutorial1-lf.json", CURRENT_IIDM_XML_VERSION));
    }
}
