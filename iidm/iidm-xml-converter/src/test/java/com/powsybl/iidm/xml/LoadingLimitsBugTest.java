package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.io.IOException;

public class LoadingLimitsBugTest extends AbstractConverterTest {

    @Test
    public void test() throws IOException {
        var network = EurostagTutorialExample1Factory.create();
        network.setCaseDate(DateTime.parse("2020-07-16T10:08:48.321+02:00"));
        // to reproduce the bug we need a 2 windings transformer without any data
        // that could create a sub element in the XML except apparent and active power limits
        // (tap changer, properties, current limits)
        var twt = network.getSubstation("P1").newTwoWindingsTransformer()
                .setId("TWT")
                .setVoltageLevel1("VLGEN")
                .setBus1("NGEN")
                .setConnectableBus1("NGEN")
                .setRatedU1(24.0)
                .setVoltageLevel2("VLHV1")
                .setBus2("NHV1")
                .setConnectableBus2("NHV1")
                .setRatedU2(400.0)
                .setR(1)
                .setX(1)
                .setG(0.0)
                .setB(0.0)
                .add();
        twt.newApparentPowerLimits1()
                .setPermanentLimit(100)
                .add();
        // export in 1.5 version, in 1.5 there is only current limits suppported
        // new limits (apparent power and active power) have been added from 1.6 and so
        // event if present in the network model, when asking for a <= 1.5 version export
        // these new limits should not be in the XIIDM.
        ExportOptions options = new ExportOptions()
                .setVersion(IidmXmlVersion.V_1_5.toString("."));
        roundTripTest(network, (n, path) -> NetworkXml.write(n, options, path), NetworkXml::read, "/loading-limits-bug.xml");
    }
}
