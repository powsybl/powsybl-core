/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import cim1.CIMModelFactory;
import cim1.model.CIMModel;
import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.ByteStreams;
import com.google.gdata.util.io.base.UnicodeReader;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
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
    public List<Parameter> getParameters() {
        return PARAMETERS;
    }

    @Override
    public String getComment() {
        return "CIM ENTSOE profile V1";
    }

    private Packaging detectPackaging(ReadOnlyDataSource dataSource) throws IOException {
        if (exists(dataSource, "_ME", "xml")) {
            return Packaging.MERGED;
        }
        if (exists(dataSource, "_EQ", "xml") && exists(dataSource, "_TP", "xml") && exists(dataSource, "_SV", "xml")) {
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
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            Packaging packaging = detectPackaging(dataSource);
            if (packaging != null) {
                switch (packaging) {
                    case MERGED:
                        try (InputStream is = newInputStream(dataSource, "_ME", "xml")) {
                            return isCim14(is);
                        }
                    case SPLIT:
                        try (InputStream eqIs = newInputStream(dataSource, "_EQ", "xml")) { // just test eq file to save time
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
            throw new UncheckedIOException(e);
        }
    }

    private CIMModel loadMergedModel(ReadOnlyDataSource dataSource, Reader bseqr, Reader bstpr) throws Exception {
        CIMModel model = new CIMModel();
        try (Reader mer = new UnicodeReader(newInputStream(dataSource, "_ME", "xml"), null)) {

            long startTime2 = System.currentTimeMillis();

            CIMModelFactory.read(null, null, null, null, null, null, null, null, mer, "ME",
                    bseqr, "Boundary EQ", bstpr, "Boundary TP",
                    model, new CIMModel(), true);

            LOGGER.debug("CIM model (ME) loaded in "
                    + (System.currentTimeMillis() - startTime2) + " ms");
        }
        return model;
    }

    private CIMModel loadSplitModel(ReadOnlyDataSource dataSource, Reader bseqr, Reader bstpr) throws Exception {
        CIMModel model = new CIMModel();
        try (Reader eqr = new UnicodeReader(newInputStream(dataSource, "_EQ", "xml"), null);
             Reader tpr = new UnicodeReader(newInputStream(dataSource, "_TP", "xml"), null);
             Reader svr = new UnicodeReader(newInputStream(dataSource, "_SV", "xml"), null)) {

            long startTime2 = System.currentTimeMillis();

            CIMModelFactory.read(eqr, "EQ", null, null, tpr, "TP", svr, "SV", null, null,
                    bseqr, "Boundary EQ", bstpr, "Boundary TP",
                    model, new CIMModel(), true);

            LOGGER.debug("CIM model ({EQ, TP, SV}) loaded in "
                    + (System.currentTimeMillis() - startTime2) + " ms");
        }
        return model;
    }

    private CIMModel loadModel(ReadOnlyDataSource dataSource, Reader bseqr, Reader bstpr) throws Exception {
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

    private InputStream getEqBoundaryFile(ReadOnlyDataSource dataSource) throws IOException {
        if (dataSource.exists(EQ_BOUNDARY_FILE_NAME)) {
            LOGGER.debug("Using custom EQ boundary file");
            return dataSource.newInputStream(EQ_BOUNDARY_FILE_NAME);
        } else {
            Path path = PlatformConfig.defaultConfig().getConfigDir().resolve(EQ_BOUNDARY_FILE_NAME);
            if (Files.exists(path)) {
                return Files.newInputStream(path);
            } else {
                throw new CIM1Exception("EQ boundary file not found");
            }
        }
    }

    private InputStream getTpBoundaryFile(ReadOnlyDataSource dataSource) throws IOException {
        if (dataSource.exists(TP_BOUNDARY_FILE_NAME)) {
            LOGGER.debug("Using custom TP boundary file");
            return dataSource.newInputStream(TP_BOUNDARY_FILE_NAME);
        } else {
            Path path = PlatformConfig.defaultConfig().getConfigDir().resolve(TP_BOUNDARY_FILE_NAME);
            if (Files.exists(path)) {
                return Files.newInputStream(path);
            } else {
                throw new CIM1Exception("TP boundary file not found");
            }
        }
    }

    private void copyFile(ReadOnlyDataSource fromDataSource, DataSource toDataSource, String fromFileName, String toFileName) throws IOException {
        if (fromDataSource.exists(fromFileName)) {
            try (InputStream is = fromDataSource.newInputStream(fromFileName);
                 OutputStream os = toDataSource.newOutputStream(toFileName, false)) {
                ByteStreams.copy(is, os);
            }
        }
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            String fromBaseName = getBaseName(fromDataSource);
            String toBaseName = getBaseName(toDataSource);
            for (String suffix : new String[] {"_ME", "_EQ", "_TP", "_SV"}) {
                String fromFileName = DataSourceUtil.getFileName(fromBaseName, suffix, "xml");
                String toFileName = DataSourceUtil.getFileName(toBaseName, suffix, "xml");
                copyFile(fromDataSource, toDataSource, fromFileName, toFileName);
            }
            for (String fileName : new String[] {EQ_BOUNDARY_FILE_NAME, TP_BOUNDARY_FILE_NAME}) {
                copyFile(fromDataSource, toDataSource, fileName, fileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, Properties parameters) {

        Network network;

        long startTime = System.currentTimeMillis();

        try {
            try (Reader bseqr = new UnicodeReader(getEqBoundaryFile(dataSource), null);
                 Reader bstpr = new UnicodeReader(getTpBoundaryFile(dataSource), null)) {

                CIMModel model;
                try {
                    model = loadModel(dataSource, bseqr, bstpr);
                } catch (Exception e) {
                    throw new CIM1Exception(e);
                }

                boolean invertVoltageStepIncrementOutOfPhase = (Boolean) Importers.readParameter(FORMAT, parameters, INVERT_VOLTAGE_STEP_INCREMENT_OUT_OF_PHASE_PARAMETER);
                Country defaultCountry = Country.valueOf((String) Importers.readParameter(FORMAT, parameters, DEFAULT_COUNTRY_PARAMETER));
                boolean usePsseNamingStrategy = (Boolean) Importers.readParameter(FORMAT, parameters, USE_PSSE_NAMING_STRATEGY_PARAMETER);
                List<String> substationIdExcludedFromMapping = (List<String>) Importers.readParameter(FORMAT, parameters, SUBSTATION_ID_EXCLUDED_FROM_MAPPING);

                if (invertVoltageStepIncrementOutOfPhase) {
                    LOGGER.warn("Voltage step increment out of phase has been inverted!");
                }

                CIM1NamingStrategyFactory namingStrategyFactory;
                try (InputStream eqIs = newInputStream(dataSource, "_EQ", "xml")) { // just test eq file to save time
                    namingStrategyFactory = usePsseNamingStrategy && isPtiCim14(eqIs) ? new CIM1PsseNamingStrategyFactory()
                                                                                      : new CIM1DefaultNamingStrategyFactory();
                }

                CIM1ConverterConfig config = new CIM1ConverterConfig(invertVoltageStepIncrementOutOfPhase,
                                                                     defaultCountry,
                                                                     substationIdExcludedFromMapping,
                                                                     namingStrategyFactory);

                // CIM model to IIDM model
                network = new CIM1Converter(model, dataSource.getBaseName(), config)
                        .convert();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }

        LOGGER.debug("CIM import done in " + (System.currentTimeMillis() - startTime) + " ms");

        return network;
    }

    private String getBaseName(ReadOnlyDataSource dataSource) {
        String basename = dataSource.getBaseName();
        if (basename.endsWith("_ME") || basename.endsWith("_EQ") || basename.endsWith("_TP") || basename.endsWith("_SV")) {
            basename = basename.substring(0, basename.length() - 3);
        }

        return basename;
    }

    private InputStream newInputStream(ReadOnlyDataSource dataSource, String suffix, String ext) throws IOException {
        String baseName = getBaseName(dataSource);
        String fileName = DataSourceUtil.getFileName(baseName, suffix, ext);
        return dataSource.newInputStream(fileName);
    }

    private boolean exists(ReadOnlyDataSource dataSource, String suffix, String ext) throws IOException {
        String baseName = getBaseName(dataSource);
        String fileName = DataSourceUtil.getFileName(baseName, suffix, ext);
        return dataSource.exists(fileName);
    }
}
