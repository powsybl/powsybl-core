/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.converter;

import com.google.auto.service.AutoService;
import com.google.common.io.ByteStreams;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.parameters.ConfiguredParameter;
import com.powsybl.commons.parameters.Parameter;
import com.powsybl.commons.parameters.ParameterDefaultValueConfig;
import com.powsybl.commons.parameters.ParameterType;
import com.powsybl.iidm.network.*;
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

import static com.powsybl.psse.converter.AbstractConverter.getSubstationIdFromBuses;
import static com.powsybl.psse.converter.AbstractConverter.getSubstationIdFromPsseSubstationIds;

/**
 * @author JB Heyberger {@literal <jean-baptiste.heyberger at rte-france.com>}
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
    private static final Parameter IGNORE_NODE_BREAKER_TOPOLOGY_PARAMETER = new Parameter("psse.import.ignore-node-breaker-topology",
            ParameterType.BOOLEAN,
            "Ignore the node breaker topology specified in the substation data of the file",
            Boolean.FALSE);

    @Override
    public String getFormat() {
        return FORMAT;
    }

    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList(EXTENSIONS);
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> parameterList = List.of(
                IGNORE_BASE_VOLTAGE_PARAMETER,
                IGNORE_NODE_BREAKER_TOPOLOGY_PARAMETER);
        return ConfiguredParameter.load(parameterList, getFormat(), ParameterDefaultValueConfig.INSTANCE);
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
            if (dataSource.isDataExtension(ext) && dataSource.exists(null, ext)) {
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

            new PsseFixes(pssePowerFlowModel, context.getVersion()).fix();
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
        boolean ignoreNodeBreakerTopology = Parameter.readBoolean(FORMAT, parameters, IGNORE_NODE_BREAKER_TOPOLOGY_PARAMETER,
                ParameterDefaultValueConfig.INSTANCE);
        PerUnitContext perUnitContext = new PerUnitContext(psseModel.getCaseIdentification().getSbase(), ignoreBaseVoltage);

        // The map gives access to PsseBus object with the int bus Number
        Map<Integer, PsseBus> busNumToPsseBus = psseModel.getBuses().stream().collect(Collectors.toMap(PsseBus::getI, Function.identity()));

        // Necessary data for validating nodeBreaker topology
        NodeBreakerValidation nodeBreakerValidation = new NodeBreakerValidation(ignoreNodeBreakerTopology);
        nodeBreakerValidation.fillAndValidate(psseModel, version);

        // build container to fit IIDM requirements
        ContainersMapping containersMapping = defineContainersMapping(psseModel, busNumToPsseBus, perUnitContext, nodeBreakerValidation);

        // create buses
        NodeBreakerImport nodeBreakerImport = createBuses(psseModel, containersMapping, perUnitContext, network, nodeBreakerValidation);

        // Create loads
        for (PsseLoad psseLoad : psseModel.getLoads()) {
            new LoadConverter(psseLoad, containersMapping, network, nodeBreakerImport).create();
        }

        // Create fixed shunts
        for (PsseFixedShunt psseShunt : psseModel.getFixedShunts()) {
            new FixedShuntCompensatorConverter(psseShunt, containersMapping, network, nodeBreakerImport).create();
        }

        for (PsseGenerator psseGen : psseModel.getGenerators()) {
            new GeneratorConverter(psseGen, containersMapping, network, nodeBreakerImport).create();
        }

        for (PsseNonTransformerBranch psseLine : psseModel.getNonTransformerBranches()) {
            new LineConverter(psseLine, containersMapping, perUnitContext, network, version, nodeBreakerImport).create();
        }

        for (PsseTransformer psseTfo : psseModel.getTransformers()) {
            new TransformerConverter(psseTfo, containersMapping, perUnitContext, network, busNumToPsseBus, psseModel.getCaseIdentification().getSbase(), version, nodeBreakerImport).create();
        }

        for (PsseTwoTerminalDcTransmissionLine psseTwoTerminaDc : psseModel.getTwoTerminalDcTransmissionLines()) {
            new TwoTerminalDcConverter(psseTwoTerminaDc, containersMapping, network, nodeBreakerImport).create();
        }

        // Create switched shunts
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            new SwitchedShuntCompensatorConverter(psseSwShunt, containersMapping, network, version, nodeBreakerImport).create();
        }

        // Create areas
        for (PsseArea psseArea : psseModel.getAreas()) {
            new AreaConverter(psseArea, psseModel.getBuses(), containersMapping, network).create();
        }

        // Attach a slack bus
        new SlackConverter(psseModel.getBuses(), containersMapping, network, nodeBreakerImport).create();

        // Add controls
        for (PsseGenerator psseGen : psseModel.getGenerators()) {
            new GeneratorConverter(psseGen, containersMapping, network, nodeBreakerImport).addControl(busNumToPsseBus.get(psseGen.getI()));
        }
        for (PsseTransformer psseTransformer : psseModel.getTransformers()) {
            new TransformerConverter(psseTransformer, containersMapping, perUnitContext, network, busNumToPsseBus, psseModel.getCaseIdentification().getSbase(), version, nodeBreakerImport).addControl();
        }
        for (PsseSwitchedShunt psseSwShunt : psseModel.getSwitchedShunts()) {
            new SwitchedShuntCompensatorConverter(psseSwShunt, containersMapping, network, version, nodeBreakerImport).addControl();
        }
        return network;
    }

    private ContainersMapping defineContainersMapping(PssePowerFlowModel psseModel, Map<Integer, PsseBus> busNumToPsseBus, PerUnitContext perUnitContext, NodeBreakerValidation nodeBreakerValidation) {
        List<Edge> edges = new ArrayList<>();
        // only zeroImpedance Lines are necessary and as they are not allowed, nothing to do

        defineEdgesForTransformers(psseModel, busNumToPsseBus, edges);
        // buses inside a psse substation are connected in the same way as transformers
        defineEdgesForBusesInsideSubstation(nodeBreakerValidation, edges);

        return ContainersMapping.create(psseModel.getBuses(), edges,
                PsseBus::getI,
                Edge::bus1,
                Edge::bus2,
                Edge::zeroImpedance,
                Edge::transformer,
                busNumber -> getNominalVFromBusNumber(busNumToPsseBus, busNumber, perUnitContext),
                AbstractConverter::getVoltageLevelId,
                substationNums -> getSubstationId(nodeBreakerValidation, substationNums));
    }

    private void defineEdgesForTransformers(PssePowerFlowModel psseModel, Map<Integer, PsseBus> busNumToPsseBus, List<Edge> edges) {
        psseModel.getTransformers().forEach(t -> {
            if (t.getK() == 0) { // twoWindingsTransformers with zero impedance are not allowed
                if (busNumToPsseBus.containsKey(t.getI()) && busNumToPsseBus.containsKey(t.getJ())) {
                    edges.add(new Edge(t.getI(), t.getJ(), true, false));
                }
            } else { // threeWindingsTransformers with zero impedance are not allowed
                if (busNumToPsseBus.containsKey(t.getI()) && busNumToPsseBus.containsKey(t.getJ()) && busNumToPsseBus.containsKey(t.getK())) {
                    edges.add(new Edge(t.getI(), t.getJ(), true, false));
                    edges.add(new Edge(t.getI(), t.getK(), true, false));
                }
            }
        });
    }

    private void defineEdgesForBusesInsideSubstation(NodeBreakerValidation nodeBreakerValidation, List<Edge> edges) {
        nodeBreakerValidation.getValidSubstations().forEach(psseSubstation -> {
            List<Integer> busesInside = nodeBreakerValidation.getBuses(psseSubstation);
            if (busesInside.size() >= 2) {
                int bus = busesInside.get(0);
                for (int index = 1; index < busesInside.size(); index++) {
                    edges.add(new Edge(bus, busesInside.get(index), true, false));
                }
            }
        });
    }

    private static String getSubstationId(NodeBreakerValidation nodeBreakerValidation, Set<Integer> substationBusNumbers) {
        Set<Integer> validSubstationIds = nodeBreakerValidation.getValidSubstationsIds(substationBusNumbers);
        return validSubstationIds.isEmpty() ? getSubstationIdFromBuses(substationBusNumbers) : getSubstationIdFromPsseSubstationIds(validSubstationIds);
    }

    private double getNominalVFromBusNumber(Map<Integer, PsseBus> busNumToPsseBus, int busNumber, PerUnitContext perUnitContext) {
        if (!busNumToPsseBus.containsKey(busNumber)) { // never should happen
            throw new PsseException("busId without PsseBus" + busNumber);
        }
        return VoltageLevelConverter.getNominalV(busNumToPsseBus.get(busNumber), perUnitContext.ignoreBaseVoltage());
    }

    private static NodeBreakerImport createBuses(PssePowerFlowModel psseModel, ContainersMapping containersMapping,
                                                 PerUnitContext perUnitContext, Network network,
                                                 NodeBreakerValidation nodeBreakerValidation) {

        NodeBreakerImport nodeBreakerImport = new NodeBreakerImport();

        for (PsseBus psseBus : psseModel.getBuses()) {
            Substation substation = new SubstationConverter(psseBus, containersMapping, network).create();
            VoltageLevel voltageLevel = new VoltageLevelConverter(psseBus, containersMapping, perUnitContext, network, nodeBreakerValidation, nodeBreakerImport).create(substation);
            new BusConverter(psseBus, containersMapping, network, nodeBreakerImport).create(voltageLevel);
        }

        return nodeBreakerImport;
    }

    private record Edge(int bus1, int bus2, boolean transformer, boolean zeroImpedance) {
    }

    record PerUnitContext(double sb, boolean ignoreBaseVoltage) {
    }
}
