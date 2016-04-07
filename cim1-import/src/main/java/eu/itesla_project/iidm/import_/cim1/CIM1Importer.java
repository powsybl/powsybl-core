/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.import_.cim1;

import cim1.CIMModelFactory;
import cim1.model.CIMModel;
import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.gdata.util.io.base.UnicodeReader;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.parameters.Parameter;
import eu.itesla_project.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
@AutoService(Importer.class)
public class CIM1Importer implements Importer, CIM1Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(CIM1Importer.class);

    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String CIM14_NS = "http://iec.ch/TC57/2009/CIM-schema-cim14#";
    private static final String PTI_CIM14_NS = "http://www.pti-us.com/PTI_CIM-schema-cim14#";

    private static final String EQ_BOUNDARY_FILE_NAME = "ENTSO-E_Boundary_Set_EU_EQ.xml";
    private static final String TP_BOUNDARY_FILE_NAME = "ENTSO-E_Boundary_Set_EU_TP.xml";

    private static final Supplier<XMLInputFactory> XML_INPUT_FACTORY_SUPPLIER = Suppliers.memoize(XMLInputFactory::newInstance);

    private static final Parameter INVERT_VOLTAGE_STEP_INCREMENT_OUT_OF_PHASE_PARAMETER
            = new Parameter("invertVoltageStepIncrementOutOfPhase", ParameterType.BOOLEAN, "Invert VoltageStepIncrementOutOfPhase attribute", Boolean.FALSE);

    private static final Parameter DEFAULT_COUNTRY_PARAMETER
            = new Parameter("defaultCountry", ParameterType.STRING, "Default country", Country.values()[0].name());

    private static final Parameter USE_PSSE_NAMING_STRATEGY_PARAMETER
            = new Parameter("usePsseNamingStrategy", ParameterType.BOOLEAN, "Use PSS/E naming strategy", Boolean.TRUE);

    private static final Parameter SUBSTATION_ID_EXCLUDED_FROM_MAPPING
            = new Parameter("substationIdExcludedFromMapping", ParameterType.STRING_LIST, "Substation IDs excluded from mapping", Collections.<String>emptyList());

    private static final List<Parameter> PARAMETERS = Arrays.asList(
            INVERT_VOLTAGE_STEP_INCREMENT_OUT_OF_PHASE_PARAMETER,
            DEFAULT_COUNTRY_PARAMETER,
            USE_PSSE_NAMING_STRATEGY_PARAMETER,
            SUBSTATION_ID_EXCLUDED_FROM_MAPPING);

    private enum Packaging {
        MERGED,
        SPLIT
    }

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public InputStream get16x16Icon() {
        return CIM1Importer.class.getResourceAsStream("/icons/entsoe16x16.png");
    }

    @Override
    public List<Parameter> getParameters() {
        return PARAMETERS;
    }

    @Override
    public String getComment() {
        return "CIM ENTSOE profile V1";
    }

    private Packaging detectPackaging(DataSource dataSource) throws IOException {
        if (dataSource.exists("_ME", "xml")) {
            return Packaging.MERGED;
        }
        if (dataSource.exists("_EQ", "xml") && dataSource.exists("_TP", "xml") && dataSource.exists("_SV", "xml")) {
            return Packaging.SPLIT;
        }
        return null;
    }

    private static boolean isCim14(InputStream is) throws XMLStreamException {
        // check the first root element is RDF and the second one belongs to CIM 14 namespace
        XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
        try {
            int elemIndex = 0;
            while (xmlsr.hasNext()) {
                int eventType = xmlsr.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    String ns = xmlsr.getNamespaceURI();
                    if (elemIndex == 0) {
                        if (!RDF_NS.equals(ns)) {
                            return false;
                        }
                    } else if (elemIndex == 1) {
                        return CIM14_NS.equals(ns);
                    } else {
                        throw new AssertionError();
                    }
                    elemIndex++;
                }
            }
        } finally {
            xmlsr.close();
        }
        return false;
    }

    private static boolean isPtiCim14(InputStream is) throws XMLStreamException {
        // check the first root element is RDF and the second one belongs to CIM 14 namespace
        XMLStreamReader xmlsr = XML_INPUT_FACTORY_SUPPLIER.get().createXMLStreamReader(is);
        try {
            while (xmlsr.hasNext()) {
                int eventType = xmlsr.next();
                if (eventType == XMLEvent.START_ELEMENT) {
                    return PTI_CIM14_NS.equals(xmlsr.getNamespaceURI("pti"));
                }
            }
        } finally {
            xmlsr.close();
        }
        return false;
    }

    @Override
    public boolean exists(DataSource dataSource) {
        try {
            Packaging packaging = detectPackaging(dataSource);
            if (packaging != null) {
                switch (packaging) {
                    case MERGED:
                        try (InputStream is = dataSource.newInputStream("_ME", "xml")) {
                            return isCim14(is);
                        }
                    case SPLIT:
                        try (InputStream eqIs = dataSource.newInputStream("_EQ", "xml")) { // just test eq file to save time
                            return isCim14(eqIs);
                        }
                    default:
                        throw new AssertionError();
                }
            }
            return false;
        } catch (XMLStreamException e) {
            return false;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CIMModel loadMergedModel(DataSource dataSource, Reader bseqr, Reader bstpr) throws Exception {
        CIMModel model = new CIMModel();
        try (Reader mer = new UnicodeReader(dataSource.newInputStream("_ME", "xml"), null)) {

            long startTime2 = System.currentTimeMillis();

            CIMModelFactory.read(null, null, null, null, null, null, null, null, mer, "ME",
                    bseqr, "Boundary EQ", bstpr, "Boundary TP",
                    model, new CIMModel(), true);

            LOGGER.debug("CIM model (ME) loaded in "
                    + (System.currentTimeMillis() - startTime2) + " ms");
        }
        return model;
    }

    private CIMModel loadSplitModel(DataSource dataSource, Reader bseqr, Reader bstpr) throws Exception {
        CIMModel model = new CIMModel();
        try (Reader eqr = new UnicodeReader(dataSource.newInputStream("_EQ", "xml"), null);
             Reader tpr = new UnicodeReader(dataSource.newInputStream("_TP", "xml"), null);
             Reader svr = new UnicodeReader(dataSource.newInputStream("_SV", "xml"), null)) {

            long startTime2 = System.currentTimeMillis();

            CIMModelFactory.read(eqr, "EQ", null, null, tpr, "TP", svr, "SV", null, null,
                    bseqr, "Boundary EQ", bstpr, "Boundary TP",
                    model, new CIMModel(), true);

            LOGGER.debug("CIM model ({EQ, TP, SV}) loaded in "
                    + (System.currentTimeMillis() - startTime2) + " ms");
        }
        return model;
    }

    private CIMModel loadModel(DataSource dataSource, Reader bseqr, Reader bstpr) throws Exception {
        Packaging packaging = detectPackaging(dataSource);
        if (packaging != null) {
            switch (packaging) {
                case MERGED:
                    return loadMergedModel(dataSource, bseqr, bstpr);
                case SPLIT:
                    return loadSplitModel(dataSource, bseqr, bstpr);
                default:
                    throw new AssertionError();
            }
        } else {
            throw new CIM1Exception("ME file and {EQ,TP,SV} file set not found");
        }
    }

    private InputStream getEqBoundaryFile(DataSource dataSource) throws IOException {
        if (dataSource.exists(EQ_BOUNDARY_FILE_NAME)) {
            LOGGER.debug("Using custom EQ boundary file");
            return dataSource.newInputStream(EQ_BOUNDARY_FILE_NAME);
        } else {
            Path path = PlatformConfig.CONFIG_DIR.resolve(EQ_BOUNDARY_FILE_NAME);
            if (Files.exists(path)) {
                return Files.newInputStream(path);
            } else {
                throw new RuntimeException("EQ boundary file not found");
            }
        }
    }

    private InputStream getTpBoundaryFile(DataSource dataSource) throws IOException {
        if (dataSource.exists(TP_BOUNDARY_FILE_NAME)) {
            LOGGER.debug("Using custom TP boundary file");
            return dataSource.newInputStream(TP_BOUNDARY_FILE_NAME);
        } else {
            Path path = PlatformConfig.CONFIG_DIR.resolve(TP_BOUNDARY_FILE_NAME);
            if (Files.exists(path)) {
                return Files.newInputStream(path);
            } else {
                throw new RuntimeException("TP boundary file not found");
            }
        }
    }

    @Override
    public Network import_(DataSource dataSource, Properties parameters) {

        Network network;

        long startTime = System.currentTimeMillis();

        try {
            try (Reader bseqr = new UnicodeReader(getEqBoundaryFile(dataSource), null);
                 Reader bstpr = new UnicodeReader(getTpBoundaryFile(dataSource), null)) {

                CIMModel model = loadModel(dataSource, bseqr, bstpr);

                boolean invertVoltageStepIncrementOutOfPhase = (Boolean) Importers.readParameter(FORMAT, parameters, INVERT_VOLTAGE_STEP_INCREMENT_OUT_OF_PHASE_PARAMETER);
                Country defaultCountry = Country.valueOf((String) Importers.readParameter(FORMAT, parameters, DEFAULT_COUNTRY_PARAMETER));
                boolean usePsseNamingStrategy = (Boolean) Importers.readParameter(FORMAT, parameters, USE_PSSE_NAMING_STRATEGY_PARAMETER);
                List<String> substationIdExcludedFromMapping = (List<String>) Importers.readParameter(FORMAT, parameters, SUBSTATION_ID_EXCLUDED_FROM_MAPPING);

                if (invertVoltageStepIncrementOutOfPhase) {
                    LOGGER.warn("Voltage step increment out of phase has been inverted!");
                }

                CIM1NamingStrategyFactory namingStrategyFactory;
                try (InputStream eqIs = dataSource.newInputStream("_EQ", "xml")) { // just test eq file to save time
                    namingStrategyFactory = usePsseNamingStrategy && isPtiCim14(eqIs) ? new CIM1PSSENamingStrategyFactory()
                                                                                      : new CIM1DefaultNamingStrategyFactory();
                }

                CIM1ConverterConfig config = new CIM1ConverterConfig(invertVoltageStepIncrementOutOfPhase,
                                                                     defaultCountry,
                                                                     substationIdExcludedFromMapping,
                                                                     namingStrategyFactory);

                // CIM model to iTesla model
                network = new CIM1Converter(model, dataSource.getBaseName(), config)
                        .convert();
            }
        } catch (Exception e) {
            throw new CIM1Exception(e);
        }

        LOGGER.debug("CIM import done in " + (System.currentTimeMillis() - startTime) + " ms");

        return network;
    }

}
