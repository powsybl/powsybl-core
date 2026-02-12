package com.powsybl.powerfactory.converter;

// import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.powerfactory.converter.AbstractConverter.NodeRef;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.dgs.DgsStudyCaseLoader;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.PowerFactoryDataLoader;
import com.powsybl.powerfactory.model.StudyCase;

import static org.junit.jupiter.api.Assertions.*;

public class HvdcDetailedConverterTest {

    @Mock
    ImportContext importContext;

    //     @Test
    // void testHvdcDetailedConverter() {
    //     Network network = Network.create("test1", "test");

    //     List<DataObject> elmTerms = new ArrayList<>();
    //     List<DataObject> elmVscs = new ArrayList<>();

    //     Mockito.when(importContext.elmTermIdToNode.get(1L)).thenReturn(new NodeRef("vl1", 1, 1));
    //     Mockito.when(importContext.elmTermIdToNode.get(2L)).thenReturn(new NodeRef("vl1", 2, 2));

    //     DataObject elmTerm = new DataObject(0, null, null);

    //     HvdcDetailedConverter detailedConverter = new HvdcDetailedConverter(importContext, network, elmTerms, elmVscs);

    //     boolean isDcLink = detailedConverter.isDcLink(elmTerm);
    //     assertEquals(false, isDcLink);
    // }

    @Test
    void testHvdcDetailedConverter() throws Exception {
        ImportedData importedData = importDgs("hvdc-2-VSC.dgs");
    }

    @Test
    void testCreate1() throws Exception {
        ImportedData importedData = importDgs("hvdc-2-VSC.dgs");

        importedData.converter.create();
        Network network = importedData.converter.getNetwork();

        final double NOMINAL_DC_V = 320.0;
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(2, network.getDcNodeCount());
        assertEquals(NOMINAL_DC_V, network.getVoltageSourceConverter("HVDC Converter 1").getTargetVdc());
        assertEquals(NOMINAL_DC_V, network.getVoltageSourceConverter("HVDC Converter 2").getTargetVdc());
        assertEquals(600.0, network.getVoltageSourceConverter("HVDC Converter 1").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getReactivePowerSetpoint());
        assertEquals(100.0, network.getVoltageSourceConverter("HVDC Converter 2").getReactivePowerSetpoint());
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 1").getIdleLoss());
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 2").getIdleLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getSwitchingLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getSwitchingLoss());

        for (DcNode node : network.getDcNodes()) {
            assertEquals(NOMINAL_DC_V, node.getNominalV());
        }

    }

    @Test
    void testCreate2() throws Exception {
        ImportedData importedData = importDgs("hvdc-2-VSC-ACDC-links.dgs");

        importedData.converter.create();
        Network network = importedData.converter.getNetwork();

        final double NOMINAL_DC_V = 320.0;
        assertEquals(2, network.getVoltageSourceConverterCount());
        assertEquals(1, network.getDcGroundCount());
        assertEquals(4, network.getDcNodeCount());
        assertEquals(2, network.getDcLineCount());
        assertEquals(NOMINAL_DC_V, network.getVoltageSourceConverter("HVDC Converter 1").getTargetVdc());
        assertEquals(NOMINAL_DC_V, network.getVoltageSourceConverter("HVDC Converter 2").getTargetVdc());
        assertEquals(600.0, network.getVoltageSourceConverter("HVDC Converter 1").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getTargetP());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getReactivePowerSetpoint());
        assertEquals(100.0, network.getVoltageSourceConverter("HVDC Converter 2").getReactivePowerSetpoint());
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 1").getIdleLoss());
        assertEquals(10000.0, network.getVoltageSourceConverter("HVDC Converter 2").getIdleLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getResistiveLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 1").getSwitchingLoss());
        assertEquals(0.0, network.getVoltageSourceConverter("HVDC Converter 2").getSwitchingLoss());

        for (DcNode node : network.getDcNodes()) {
            assertEquals(NOMINAL_DC_V, node.getNominalV());
        }

        final double RESISTANCE_DC_LINE = 50.0 * 0.1;

        for (DcLine line : network.getDcLines()) {
            assertEquals(RESISTANCE_DC_LINE, line.getR());
        }

        DcLine dcLine1 = network.getDcLine("DC-Line_pos");
        VoltageSourceConverter vsc1 = network.getVoltageSourceConverter("HVDC Converter 1");
        VoltageSourceConverter vsc2 = network.getVoltageSourceConverter("HVDC Converter 2");
        assertSame(dcLine1.getDcTerminal1().getDcNode(), vsc1.getDcTerminal1().getDcNode());
        assertSame(dcLine1.getDcTerminal2().getDcNode(), vsc2.getDcTerminal1().getDcNode());

    }

    @Test
    void testIsDcLink() throws Exception {
        ImportedData importedData = importDgs("hvdc-2-VSC-ACDC-links.dgs");

        DataObject DCline = importedData.studyCase.getIndex().getDataObjectById(4).get();
        DataObject ACline = importedData.studyCase.getIndex().getDataObjectById(4).get();

        assertTrue(importedData.converter.isDcLink(DCline));
        assertFalse(importedData.converter.isDcLink(ACline));
    }

    @Test
    void testIsDcNode() throws Exception{
        ImportedData importedData = importDgs("hvdc-2-VSC.dgs");

        DataObject elmTerm12 = importedData.studyCase.getIndex().getDataObjectById(12).get();
        DataObject elmTerm14 = importedData.studyCase.getIndex().getDataObjectById(14).get();
        assertFalse(importedData.converter.isDcNode(elmTerm12));
        assertTrue(importedData.converter.isDcNode(elmTerm14));
    }

    private HvdcDetailedConverter createGridDataNodeBreaker() {

        Network network = Network.create("test1", "test");
        Substation s1 = network.newSubstation()
            .setId("S1")
            .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(400)
                .setTopologyKind(TopologyKind.NODE_BREAKER)
                .add();

        VoltageLevel.NodeBreakerView bv1 = vl1.getNodeBreakerView();
        bv1.newBusbarSection().setId("BBS1")
                .setNode(1)
                .add();

        VoltageLevel.NodeBreakerView bv2 = vl1.getNodeBreakerView();
        bv2.newBusbarSection().setId("BBS2")
                .setNode(2)
                .add();

        ArrayList<DataObject> elmTerms = new ArrayList<>();
        ArrayList<DataObject> elmVscs = new ArrayList<>();

        elmTerms.add(new DataObject(1, null, null));

        Mockito.when(importContext.elmTermIdToNode.get(1L)).thenReturn(new NodeRef("vl1", 1, 1));
        Mockito.when(importContext.elmTermIdToNode.get(2L)).thenReturn(new NodeRef("vl1", 2, 2));

        HvdcDetailedConverter detailedConverter = new HvdcDetailedConverter(importContext, network, elmTerms, elmVscs);

        return detailedConverter;
    }

    private record ImportedData(HvdcDetailedConverter converter, StudyCase studyCase){}

    /**
     * Load DGS file into an HvdcDetailedConverter
     * @param fileName Name of the test case, with .dgs extension.
     * @return HvdcDetailedConverter constructed from there.
     * @throws FileNotFoundException
     */
    private ImportedData importDgs(String fileName) throws FileNotFoundException {

        // Load file and redo what is in PowerFactoryImporter.createNetwork and is strictly necessary to
        // produce and instance of HvdcDetailedConverter
        Path file = Path.of("src","test","resources",fileName);
        PowerFactoryDataLoader<StudyCase> studyCaseLoader = new DgsStudyCaseLoader();
        InputStream inputStream = new FileInputStream(file.toFile());
        StudyCase studyCase = studyCaseLoader.doLoad(fileName, inputStream);

        Network network = Network.create(fileName, "test");

        List<DataObject> elmTerms = studyCase.getElmNets().stream()
                .flatMap(elmNet -> elmNet.search(".*.ElmTerm").stream())
                .collect(Collectors.toList());

        List<DataObject> elmVscs = studyCase.getElmNets().stream()
            .flatMap(elmNet -> elmNet.search(".*.ElmVsc").stream())
            .collect(Collectors.toList());

        ContainersMapping containerMapping = ContainersMappingHelper.create(studyCase.getIndex(), elmTerms);
        ImportContext importContext = new ImportContext(containerMapping);

        HvdcDetailedConverter detailedConverter = new HvdcDetailedConverter(importContext, network, elmTerms, elmVscs);

        return new ImportedData(detailedConverter, studyCase);

    }

}
