/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.config.InMemoryPlatformConfig;
import com.powsybl.commons.datasource.*;
import com.powsybl.iidm.IidmImportExportType;
import com.powsybl.iidm.import_.ImportOptions;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.network.test.HvdcTestNetwork;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;


/**
 * @author Chamseddine BENHAMED  <chamseddine.benhamed at rte-france.com>
 */

public class IncrementalUpdateTest extends AbstractConverterTest {
    private Network importNetworkFromRessources(String file, String ext) {
        XMLImporter importer = new XMLImporter(new InMemoryPlatformConfig(fileSystem));
        ReadOnlyDataSource dataSource = new ResourceDataSource(file, new ResourceSet("/", file + "." + ext));
        final Network network = importer.importData(dataSource, new Properties());
        return network;
    }

    private Network getEurostagLfNetwork() {
        return importNetworkFromRessources("eurostag-tutorial1-lf", "xml");
    }

    private Network getEurostagNetworkWithWrongControlValues() {
        return importNetworkFromRessources("eurostag-with-wrong-control-values", "xml");
    }

    private Network getHvdcTestNetworkWithWrongControlValues() {
        return importNetworkFromRessources("vsc-with-wrong-control-values", "xiidm");
    }

    private Network getHvdcTestNetworkWithWrongTopoValues() {
        return importNetworkFromRessources("vsc-with-wrong-topo-values", "xiidm");
    }

    private Network getEurostagNetworkWithWrongTopoValues() {
        return importNetworkFromRessources("eurostag-tutorial-example1-with-wrong-topo-values", "xml");
    }

    @Test
    public void updateStateValues() throws IOException {
        //load networks
        //network without state values
        Network network = EurostagTutorialExample1Factory.create();
        //network with state values
        Network networkLf = getEurostagLfNetwork();
        //set the same case date
        network.setCaseDate(networkLf.getCaseDate());

        assertNotEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getP(), network.getLine("NHV1_NHV2_2").getTerminal1().getP(), 0);
        assertNotEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ(), 0);
        assertNotEquals(networkLf.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getV(), network.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getV(), 0);
        assertNotEquals(networkLf.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getAngle(), network.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getAngle(), 0);

        //To create a data source that contains TOPO.xiidm, STATE.xiidm et CONTROL.xiidm
        MemDataSource dataSource = new MemDataSource();
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        //Incremental export for the second network : eurostag with loadflow
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        new XMLExporter().export(networkLf, properties, dataSource);

        //Update the first network using the state file recently exported
        NetworkXml.update(network, new ImportOptions().setControl(false).setTopo(false).setState(true), dataSource);
        assertEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getP(), network.getLine("NHV1_NHV2_2").getTerminal1().getP(), 0);
        assertEquals(networkLf.getLine("NHV1_NHV2_2").getTerminal1().getQ(), network.getLine("NHV1_NHV2_2").getTerminal1().getQ(), 0);
        assertEquals(networkLf.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getV(), network.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getV(), 0);
        assertEquals(networkLf.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getAngle(), network.getVoltageLevel("VLHV2").getBusBreakerView().getBus("NHV2").getAngle(), 0);
    }

    @Test
    public void updateControlValues1() throws IOException {
        //testing twoWindingTransformer and generator control values update.
        //load networks
        Network network = EurostagTutorialExample1Factory.create();
        //network without control values
        Network network2 = getEurostagNetworkWithWrongControlValues();
        //set the same case date
        network.setCaseDate(network2.getCaseDate());

        assertNotEquals(network.getGenerator("GEN").getTargetP(), network2.getGenerator("GEN").getTargetP());
        assertNotEquals(network.getGenerator("GEN").getTargetV(), network2.getGenerator("GEN").getTargetV());
        assertNotEquals(network.getGenerator("GEN").getTargetQ(), network2.getGenerator("GEN").getTargetQ());
        assertNotEquals(network.getGenerator("GEN").isVoltageRegulatorOn(), network2.getGenerator("GEN").isVoltageRegulatorOn());

        //To create a data source that contains TOPO.xiidm, STATE.xiidm et CONTROL.xiidm
        MemDataSource dataSource = new MemDataSource();
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.TOPO, "false");
        properties.put(XMLExporter.STATE, "false");
        new XMLExporter().export(network, properties, dataSource);
        //Incremental export
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        new XMLExporter().export(network, properties, dataSource);

        //Update the network
        NetworkXml.update(network2, new ImportOptions().setControl(true).setTopo(false).setState(false), dataSource);
        assertEquals(network.getGenerator("GEN").getTargetP(), network2.getGenerator("GEN").getTargetP(), 0);
        assertEquals(network.getGenerator("GEN").getTargetV(), network2.getGenerator("GEN").getTargetV(), 0);
        assertEquals(network.getGenerator("GEN").getTargetQ(), network2.getGenerator("GEN").getTargetQ(), 0);
        assertEquals(network.getGenerator("GEN").isVoltageRegulatorOn(), network2.getGenerator("GEN").isVoltageRegulatorOn());
    }

    @Test
    public void updateControlValues2() throws IOException {
        //testing VscConverterStation and HvdcLine control values update.
        //load networks
        Network network = HvdcTestNetwork.createVsc();
        //network without control values
        Network network2 = getHvdcTestNetworkWithWrongControlValues();
        //set the same case date
        network.setCaseDate(network2.getCaseDate());

        assertNotEquals(network.getVscConverterStation("C1").getVoltageSetpoint(), network2.getVscConverterStation("C1").getVoltageSetpoint());
        assertNotEquals(network.getVscConverterStation("C2").getReactivePowerSetpoint(), network2.getVscConverterStation("C2").getReactivePowerSetpoint());
        assertNotEquals(network.getHvdcLine("L").getActivePowerSetpoint(), network2.getHvdcLine("L").getActivePowerSetpoint());

        //To create a data source that contains TOPO.xiidm, STATE.xiidm et CONTROL.xiidm
        MemDataSource dataSource = new MemDataSource();
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.TOPO, "false");
        properties.put(XMLExporter.STATE, "false");
        new XMLExporter().export(network, properties, dataSource);
        //Incremental export
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        new XMLExporter().export(network, properties, dataSource);

        //Update the network
        NetworkXml.update(network2, new ImportOptions().setControl(true).setTopo(false).setState(false), dataSource);
        assertEquals(network.getVscConverterStation("C1").getVoltageSetpoint(), network2.getVscConverterStation("C1").getVoltageSetpoint(), 0);
        assertEquals(network.getVscConverterStation("C2").getVoltageSetpoint(), network2.getVscConverterStation("C2").getVoltageSetpoint(), 0);
        assertEquals(network.getHvdcLine("L").getActivePowerSetpoint(), network2.getHvdcLine("L").getActivePowerSetpoint(), 0);
    }

    @Test
    public void updateTopoValues1() throws IOException {
        //testing twoWindingTransformer and generator control values update.
        //load networks
        Network network = EurostagTutorialExample1Factory.create();
        //network without control values
        Network network2 = getEurostagNetworkWithWrongTopoValues();
        //set the same case date
        network.setCaseDate(network2.getCaseDate());

        assertNotEquals(network.getGenerator("GEN").getTerminal().getBusBreakerView().getConnectableBus().getName(),
                network2.getGenerator("GEN").getTerminal().getBusBreakerView().getConnectableBus().getName());

        assertNotEquals(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().getBusBreakerView().getConnectableBus().getName(),
                network2.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().getBusBreakerView().getConnectableBus().getName());

        assertNotEquals(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().getBusBreakerView().getConnectableBus().getName(),
                network2.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().getBusBreakerView().getConnectableBus().getName());

        //To create a data source that contains TOPO.xiidm, STATE.xiidm et CONTROL.xiidm
        MemDataSource dataSource = new MemDataSource();
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.CONTROL, "false");
        properties.put(XMLExporter.STATE, "false");
        new XMLExporter().export(network, properties, dataSource);
        //Incremental export
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        new XMLExporter().export(network, properties, dataSource);

        //Update the network
        NetworkXml.update(network2, new ImportOptions().setControl(false).setTopo(true).setState(false), dataSource);

        assertEquals(network.getGenerator("GEN").getTerminal().getBusBreakerView().getConnectableBus().getName(),
                network2.getGenerator("GEN").getTerminal().getBusBreakerView().getConnectableBus().getName());
        assertEquals(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().getBusBreakerView().getConnectableBus().getName(),
                network2.getTwoWindingsTransformer("NGEN_NHV1").getTerminal1().getBusBreakerView().getConnectableBus().getName());

        assertEquals(network.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().getBusBreakerView().getConnectableBus().getName(),
                network2.getTwoWindingsTransformer("NGEN_NHV1").getTerminal2().getBusBreakerView().getConnectableBus().getName());
    }

    @Test
    public void updateTopoValues2() throws IOException {
        //testing VscConverterStation and HvdcLine topo values update.
        //load networks
        Network network = HvdcTestNetwork.createVsc();
        //network without control values
        Network network2 = getHvdcTestNetworkWithWrongTopoValues();
        //set the same case date
        network.setCaseDate(network2.getCaseDate());

        //To create a data source that contains TOPO.xiidm, STATE.xiidm et CONTROL.xiidm
        MemDataSource dataSource = new MemDataSource();
        Properties properties = new Properties();
        properties.put(XMLExporter.ANONYMISED, "false");
        properties.put(XMLExporter.CONTROL, "false");
        properties.put(XMLExporter.STATE, "false");
        new XMLExporter().export(network, properties, dataSource);
        //Incremental export
        properties.put(XMLExporter.IMPORT_EXPORT_TYPE, String.valueOf(IidmImportExportType.INCREMENTAL_IIDM));
        new XMLExporter().export(network, properties, dataSource);

        assertNotEquals(network.getVscConverterStation("C1").getTerminal().getBusBreakerView().getConnectableBus().getName(),
                network2.getVscConverterStation("C1").getTerminal().getBusBreakerView().getConnectableBus().getName());
        assertNotEquals(network.getSwitch("DISC_BBS1_BK1").isOpen(),
                network2.getSwitch("DISC_BBS1_BK1").isOpen());
        assertNotEquals(network.getSwitch("BK1").isOpen(),
                network2.getSwitch("BK1").isOpen());

        //Update the network
        NetworkXml.update(network2, new ImportOptions().setControl(false).setTopo(true).setState(false), dataSource);

        assertEquals(network.getVscConverterStation("C1").getTerminal().getBusBreakerView().getConnectableBus().getName(),
                network2.getVscConverterStation("C1").getTerminal().getBusBreakerView().getConnectableBus().getName());
        assertEquals(network.getSwitch("DISC_BBS1_BK1").isOpen(),
                network2.getSwitch("DISC_BBS1_BK1").isOpen());
        assertEquals(network.getSwitch("BK1").isOpen(),
                network2.getSwitch("BK1").isOpen());
    }

}
