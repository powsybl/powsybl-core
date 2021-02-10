/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;
import com.powsybl.psse.model.*;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.*;
import com.powsybl.psse.model.pf.io.PowerFlowDataFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author JB Heyberger <jean-baptiste.heyberger at rte-france.com>
 */
@AutoService(Importer.class)
public class PsseImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(PsseImporter.class);

    private static final String FORMAT = "PSS/E";

    private static final String[] EXTENSIONS = {"raw", "RAW", "rawx", "RAWX"};

    private static final Parameter IGNORE_BASE_VOLTAGE_PARAMETER = new Parameter("psse.import.ignore-base-voltage",
            ParameterType.BOOLEAN,
            "Ignore base voltage specified in the file",
            Boolean.FALSE);

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<Parameter> getParameters() {
        return Collections.singletonList(IGNORE_BASE_VOLTAGE_PARAMETER);
    }

    @Override
    public String getComment() {
        return "PSS/E Format to IIDM converter";
    }

    @Override
    public boolean exists(ReadOnlyDataSource dataSource) {
        try {
            String ext = findExtension(dataSource);
            return exists(dataSource, ext);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String findExtension(ReadOnlyDataSource dataSource) throws IOException {
        for (String ext : EXTENSIONS) {
            if (dataSource.exists(null, ext)) {
                return ext;
            }
        }
        return null;
    }

    private boolean exists(ReadOnlyDataSource dataSource, String ext) throws IOException {
        if (ext != null) {
            try {
                return PowerFlowDataFactory.create(ext).isValidFile(dataSource, ext);
            } catch (PsseException | IOException e) {
                LOGGER.error(String.format("Invalid content in filename %s.%s: %s",
                    dataSource.getBaseName(),
                    ext,
                    e.getMessage()));
            }
        }
        return false;
    }

    @Override
    public void copy(ReadOnlyDataSource fromDataSource, DataSource toDataSource) {
        Objects.requireNonNull(fromDataSource);
        Objects.requireNonNull(toDataSource);
        try {
            String ext = findExtension(fromDataSource);
            if (!exists(fromDataSource, ext)) {
                throw new PowsyblException("From data source is not importable");
            }
            try (InputStream is = fromDataSource.newInputStream(null, ext);
                 OutputStream os = toDataSource.newOutputStream(null, ext, false)) {
                ByteStreams.copy(is, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters) {
        Objects.requireNonNull(dataSource);
        Objects.requireNonNull(networkFactory);
        try {
            String ext = findExtension(dataSource);
            if (ext == null) {
                throw new PsseException(String.format("No Power Flow Data file found. Basename: %s, supported extensions: %s",
                        dataSource.getBaseName(),
                        String.join("|", EXTENSIONS)));
            }
            PsseVersion version = PowerFlowDataFactory.create(ext).readVersion(dataSource, ext);
            Context context = new Context();
            PssePowerFlowModel pssePowerFlowModel = PowerFlowDataFactory.create(ext, version).read(dataSource, ext, context);
            pssePowerFlowModel.getCaseIdentification().validate();

            Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);
            // TODO store the PsseContext with the Network to be able to export back using its information
            convert(pssePowerFlowModel, network, parameters, version);
            return network;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Network convert(PssePowerFlowModel psseModel, Network network, Properties parameters, PsseVersion version) {
        // set date and time
        // TODO

        // build container to fit IIDM requirements
        // build container to fit IIDM requirements
        ContainersMapping containersMapping = defineContainersMappging(psseModel);

        boolean ignoreBaseVoltage = ConversionParameters.readBooleanParameter(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);
        PerUnitContext perUnitContext = new PerUnitContext(psseModel.getCaseIdentification().getSbase(), ignoreBaseVoltage);

        // The map gives access to PsseBus object with the int bus Number
        Map<Integer, PsseBus> busNumToPsseBus = new HashMap<>();

        // create buses
        createBuses(psseModel, containersMapping, perUnitContext, network, busNumToPsseBus);

        // Create loads
        for (PsseLoad psseLoad : psseModel.getLoads()) {
            new LoadConverter(psseLoad, containersMapping, network).create();
        }

        // Create fixed shunts
        for (PsseFixedShunt psseShunt : psseModel.getFixedShunts()) {
            new FixedShuntCompensatorConverter(psseShunt, containersMapping, network).create();
        }

        // Create switched shunts
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            new SwitchedShuntCompensatorConverter(psseSwShunt, containersMapping, network, version).create();
        }

        for (PsseGenerator psseGen : psseModel.getGenerators()) {
            new GeneratorConverter(psseGen, containersMapping, network).create();
        }

        for (PsseNonTransformerBranch psseLine : psseModel.getNonTransformerBranches()) {
            new LineConverter(psseLine, containersMapping, perUnitContext, network, version).create();
        }

        for (PsseTransformer psseTfo : psseModel.getTransformers()) {
            new TransformerConverter(psseTfo, containersMapping, perUnitContext, network, busNumToPsseBus, psseModel.getCaseIdentification().getSbase(), version).create();
        }

        // Attach a slack bus
        new SlackConverter(psseModel.getBuses(), containersMapping, network).create();

        // Add controls
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            new SwitchedShuntCompensatorConverter(psseSwShunt, containersMapping, network, version).addControl();
        }
        for (PsseGenerator psseGen : psseModel.getGenerators()) {
            new GeneratorConverter(psseGen, containersMapping, network).addControl(busNumToPsseBus.get(psseGen.getI()));
        }
        for (PsseTransformer psseTransformer : psseModel.getTransformers()) {
            new TransformerConverter(psseTransformer, containersMapping, perUnitContext, network, busNumToPsseBus, psseModel.getCaseIdentification().getSbase(), version).addControl();
        }

        return network;
    }

    private ContainersMapping defineContainersMappging(PssePowerFlowModel psseModel) {
        List<Object> branches = ImmutableList.builder()
            .addAll(psseModel.getNonTransformerBranches())
            .addAll(psseModel.getTransformers())
            .build();

        ToIntFunction<Object> branchToNum1 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getI() : ((PsseTransformer) branch).getI();
        ToIntFunction<Object> branchToNum2 = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getJ() : ((PsseTransformer) branch).getJ();
        ToIntFunction<Object> branchToNum3 = branch -> branch instanceof PsseNonTransformerBranch ? 0 : ((PsseTransformer) branch).getK();
        ToDoubleFunction<Object> branchToResistance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getR() : ((PsseTransformer) branch).getR12();
        ToDoubleFunction<Object> branchToReactance = branch -> branch instanceof PsseNonTransformerBranch ? ((PsseNonTransformerBranch) branch).getX() : ((PsseTransformer) branch).getX12();
        Predicate<Object> branchToIsTransformer = branch -> branch instanceof PsseTransformer;

        return ContainersMapping.create(psseModel.getBuses(), branches, PsseBus::getI, branchToNum1,
            branchToNum2, branchToNum3, branchToResistance, branchToReactance, branchToIsTransformer,
            busNums -> "VL" + busNums.iterator().next(), substationNum -> "S" + substationNum++);
    }

    private static void createBuses(PssePowerFlowModel psseModel, ContainersMapping containersMapping,
        PerUnitContext perUnitContext, Network network, Map<Integer, PsseBus> busNumToPsseBus) {
        for (PsseBus psseBus : psseModel.getBuses()) {

            Substation substation = new SubstationConverter(psseBus, containersMapping, network).create();
            VoltageLevel voltageLevel = new VoltageLevelConverter(psseBus, containersMapping, perUnitContext, network).create(substation);
            new BusConverter(psseBus, containersMapping, network).create(voltageLevel);

            busNumToPsseBus.put(psseBus.getI(), psseBus);
        }
    }

    public static class PerUnitContext {
        private final double sb;
        private final boolean ignoreBaseVoltage;

        PerUnitContext(double sb, boolean ignoreBaseVoltage) {
            this.sb = sb;
            this.ignoreBaseVoltage = ignoreBaseVoltage;
        }

        public double getSb() {
            return sb;
        }

        public boolean isIgnoreBaseVoltage() {
            return ignoreBaseVoltage;
        }
    }
}
