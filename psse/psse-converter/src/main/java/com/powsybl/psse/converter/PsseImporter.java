/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.util.ContainersMapping;
import com.powsybl.psse.converter.extensions.PsseConversionContextExtensionAdder;
import com.powsybl.psse.converter.extensions.PsseModelExtensionAdder;
import com.powsybl.psse.model.PsseException;
import com.powsybl.psse.model.PsseVersion;
import com.powsybl.psse.model.io.Context;
import com.powsybl.psse.model.pf.*;
import com.powsybl.psse.model.pf.io.PowerFlowDataFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

            PsseValidation psseValidation = new PsseValidation(pssePowerFlowModel, context.getVersion());
            if (!psseValidation.isValidCase()) {
                throw new PsseException("The PSS/E file is not a valid case");
            }

            Network network = networkFactory.createNetwork(dataSource.getBaseName(), FORMAT);
            // TODO store the PsseContext with the Network to be able to export back using its information
            convert(pssePowerFlowModel, network, parameters, version);

            // Add the model and context as extensions
            network.newExtension(PsseModelExtensionAdder.class).withModel(pssePowerFlowModel).add();
            network.newExtension(PsseConversionContextExtensionAdder.class).withContext(context).add();

            return network;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Network convert(PssePowerFlowModel psseModel, Network network, Properties parameters, PsseVersion version) {
        // set date and time
        // TODO

        boolean ignoreBaseVoltage = Parameter.readBoolean(FORMAT, parameters, IGNORE_BASE_VOLTAGE_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);
        PerUnitContext perUnitContext = new PerUnitContext(psseModel.getCaseIdentification().getSbase(), ignoreBaseVoltage);

        // The map gives access to PsseBus object with the int bus Number
        Map<Integer, PsseBus> busNumToPsseBus = psseModel.getBuses().stream().collect(Collectors.toMap(PsseBus::getI, Function.identity()));

        // build container to fit IIDM requirements
        ContainersMapping containersMapping = defineContainersMapping(psseModel, busNumToPsseBus, perUnitContext);

        // create buses
        createBuses(psseModel, containersMapping, perUnitContext, network);

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

        for (PsseTwoTerminalDcTransmissionLine psseTwoTerminaDc : psseModel.getTwoTerminalDcTransmissionLines()) {
            new TwoTerminalDcConverter(psseTwoTerminaDc, containersMapping, network).create();
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

    private ContainersMapping defineContainersMapping(PssePowerFlowModel psseModel, Map<Integer, PsseBus> busNumToPsseBus, PerUnitContext perUnitContext) {
        List<Edge> edges = new ArrayList<>();
        // only zeroImpedance Lines are necessary and they are not allowed, so nothing to do

        psseModel.getTransformers().forEach(t -> {
            if (t.getK() == 0) { // twoWindingsTransformers with zero impedance are not allowed
                edges.add(new Edge(t.getI(), t.getJ(), true, false));
            } else { // threeWindingsTransformers with zero impedance are not allowed
                edges.add(new Edge(t.getI(), t.getJ(), true, false));
                edges.add(new Edge(t.getI(), t.getK(), true, false));
            }
        });

        return ContainersMapping.create(psseModel.getBuses(), edges,
            PsseBus::getI,
            Edge::getBus1,
            Edge::getBus2,
            Edge::isZeroImpedance,
            Edge::isTransformer,
            busNumber -> getNominalVFromBusNumber(busNumToPsseBus, busNumber, perUnitContext),
            busNums -> "VL" + busNums.stream().sorted().findFirst().orElseThrow(() -> new PsseException("Unexpected empty busNums")),
            substationNums -> "S" + substationNums.stream().sorted().findFirst().orElseThrow(() -> new PsseException("Unexpected empty substationNums")));
    }

    private double getNominalVFromBusNumber(Map<Integer, PsseBus> busNumToPsseBus, int busNumber, PerUnitContext perUnitContext) {
        if (!busNumToPsseBus.containsKey(busNumber)) { // never should happen
            throw new PsseException("busId without PsseBus" + busNumber);
        }
        return VoltageLevelConverter.getNominalV(busNumToPsseBus.get(busNumber), perUnitContext.isIgnoreBaseVoltage());
    }

    private static void createBuses(PssePowerFlowModel psseModel, ContainersMapping containersMapping,
        PerUnitContext perUnitContext, Network network) {
        for (PsseBus psseBus : psseModel.getBuses()) {

            Substation substation = new SubstationConverter(psseBus, containersMapping, network).create();
            VoltageLevel voltageLevel = new VoltageLevelConverter(psseBus, containersMapping, perUnitContext, network).create(substation);
            new BusConverter(psseBus, containersMapping, network).create(voltageLevel);
        }
    }

    private static final class Edge {
        private final int bus1;
        private final int bus2;
        private final boolean transformer;
        private final boolean zeroImpedance;

        private Edge(int bus1, int bus2, boolean transformer, boolean zeroImpedance) {
            this.bus1 = bus1;
            this.bus2 = bus2;
            this.transformer = transformer;
            this.zeroImpedance = zeroImpedance;
        }

        private int getBus1() {
            return bus1;
        }

        private int getBus2() {
            return bus2;
        }

        private boolean isTransformer() {
            return transformer;
        }

        private boolean isZeroImpedance() {
            return zeroImpedance;
        }
    }

    static class PerUnitContext {
        private final double sb;
        private final boolean ignoreBaseVoltage;

        PerUnitContext(double sb, boolean ignoreBaseVoltage) {
            this.sb = sb;
            this.ignoreBaseVoltage = ignoreBaseVoltage;
        }

        double getSb() {
            return sb;
        }

        boolean isIgnoreBaseVoltage() {
            return ignoreBaseVoltage;
        }
    }
}
