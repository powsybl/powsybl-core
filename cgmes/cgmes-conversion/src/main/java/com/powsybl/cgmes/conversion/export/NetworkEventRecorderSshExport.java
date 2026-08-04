/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.conversion.CgmesExport;
import com.powsybl.cgmes.extensions.CgmesMetadataModels;
import com.powsybl.cgmes.model.CgmesMetadataModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.exceptions.UncheckedXmlStreamException;
import com.powsybl.commons.xml.XmlUtil;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.events.NetworkEvent;
import com.powsybl.iidm.network.events.UpdateNetworkEvent;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Exports the changes recorded on a network as a partial CGMES Steady State Hypothesis file.
 *
 * <p>Where the regular {@link SteadyStateHypothesisExport} writes the complete steady state of a network, this
 * exporter writes only the objects affected by a list of {@link NetworkEvent}s, typically collected with a
 * {@code NetworkEventRecorder}. The result is a valid SSH instance file that a receiver holding the same base
 * model can apply through the usual network update workflow, which makes it suitable for exchanging a business
 * process result between processes without serializing the whole grid model.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * NetworkEventRecorder recorder = new NetworkEventRecorder();
 * network.addListener(recorder);
 * network.getGenerator("G").setTargetP(120.0);
 * String ssh = NetworkEventRecorderSshExport.toString(network, recorder.getEvents());
 * }</pre>
 *
 * <p>Changes that have no representation in the SSH profile, such as the creation or removal of equipment, are
 * either reported as an error or skipped with a warning, depending on the {@link UnsupportedChangeBehavior}. The
 * exporter fails by default, so that a change is never silently lost.</p>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
public final class NetworkEventRecorderSshExport {

    /**
     * What to do with a recorded change that cannot be written to a Steady State Hypothesis file.
     */
    public enum UnsupportedChangeBehavior {
        /** Throw a {@link PowsyblException} describing the change. This is the default. */
        FAIL,
        /** Log a warning and continue, leaving the change out of the exported file. */
        IGNORE
    }

    /**
     * Optional settings of a partial Steady State Hypothesis export.
     *
     * <p>Every header value has a default derived from the model the network was imported from: the identifier is
     * generated, the version is the one of the source SSH incremented by one, and the new model supersedes the
     * source SSH and depends on whatever the source SSH depended on, that is on the equipment model that both the
     * sender and the receiver share.</p>
     */
    public static final class ExportOptions {

        private UnsupportedChangeBehavior unsupportedChangeBehavior = UnsupportedChangeBehavior.FAIL;
        private String modelId;
        private String description;
        private Integer version;
        private String modelingAuthoritySet;
        private boolean clearDependencies;
        private boolean supersedePreviousSshModel = true;
        private final Set<String> dependentOn = new LinkedHashSet<>();
        private final Set<String> supersedes = new LinkedHashSet<>();

        public ExportOptions setUnsupportedChangeBehavior(UnsupportedChangeBehavior unsupportedChangeBehavior) {
            this.unsupportedChangeBehavior = Objects.requireNonNull(unsupportedChangeBehavior);
            return this;
        }

        /** Set the identifier of the exported model, instead of generating one. */
        public ExportOptions setModelId(String modelId) {
            this.modelId = modelId;
            return this;
        }

        public ExportOptions setDescription(String description) {
            this.description = description;
            return this;
        }

        /** Set the version of the exported model, instead of incrementing the version of the source SSH. */
        public ExportOptions setVersion(int version) {
            this.version = version;
            return this;
        }

        public ExportOptions setModelingAuthoritySet(String modelingAuthoritySet) {
            this.modelingAuthoritySet = modelingAuthoritySet;
            return this;
        }

        /** Drop the dependencies inherited from the source SSH, keeping only those added explicitly. */
        public ExportOptions clearDependencies() {
            clearDependencies = true;
            dependentOn.clear();
            return this;
        }

        public ExportOptions addDependentOn(String modelId) {
            dependentOn.add(modelId);
            return this;
        }

        public ExportOptions addDependentOn(Collection<String> modelIds) {
            dependentOn.addAll(modelIds);
            return this;
        }

        /** Whether the exported model declares that it supersedes the SSH the network was imported from. */
        public ExportOptions setSupersedePreviousSshModel(boolean supersedePreviousSshModel) {
            this.supersedePreviousSshModel = supersedePreviousSshModel;
            return this;
        }

        public ExportOptions addSupersedes(String modelId) {
            supersedes.add(modelId);
            return this;
        }

        public ExportOptions addSupersedes(Collection<String> modelIds) {
            supersedes.addAll(modelIds);
            return this;
        }
    }

    private NetworkEventRecorderSshExport() {
    }

    /** Export the given changes and return the resulting file as a string. */
    public static String toString(Network network, Collection<NetworkEvent> events) {
        return toString(network, events, new ExportOptions());
    }

    public static String toString(Network network, Collection<NetworkEvent> events, UnsupportedChangeBehavior unsupportedChangeBehavior) {
        return toString(network, events, new ExportOptions().setUnsupportedChangeBehavior(unsupportedChangeBehavior));
    }

    public static String toString(Network network, Collection<NetworkEvent> events, ExportOptions exportOptions) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        write(network, events, outputStream, exportOptions);
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    /** Export the given changes to a file. */
    public static void write(Network network, Collection<NetworkEvent> events, Path filePath) throws IOException {
        write(network, events, filePath, new ExportOptions());
    }

    public static void write(Network network, Collection<NetworkEvent> events, Path filePath, UnsupportedChangeBehavior unsupportedChangeBehavior) throws IOException {
        write(network, events, filePath, new ExportOptions().setUnsupportedChangeBehavior(unsupportedChangeBehavior));
    }

    public static void write(Network network, Collection<NetworkEvent> events, Path filePath, ExportOptions exportOptions) throws IOException {
        Objects.requireNonNull(filePath);
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(filePath))) {
            write(network, events, outputStream, exportOptions);
        }
    }

    /** Export the given changes to an output stream. The stream is neither flushed nor closed by this method. */
    public static void write(Network network, Collection<NetworkEvent> events, OutputStream outputStream) {
        write(network, events, outputStream, new ExportOptions());
    }

    public static void write(Network network, Collection<NetworkEvent> events, OutputStream outputStream, UnsupportedChangeBehavior unsupportedChangeBehavior) {
        write(network, events, outputStream, new ExportOptions().setUnsupportedChangeBehavior(unsupportedChangeBehavior));
    }

    public static void write(Network network, Collection<NetworkEvent> events, OutputStream outputStream, ExportOptions exportOptions) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(events);
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(exportOptions);
        checkSingleGridModel(network);

        CgmesExportContext context = new CgmesExportContext(network);
        CgmesMetadataModel model = initializePartialSshModel(network, context, exportOptions);
        PartialSshUpdates updates = new PartialSshEventCollector(network, context, exportOptions.unsupportedChangeBehavior).collect(events);

        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", outputStream);
            write(updates, writer, context, model, network);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
    }

    /**
     * Keep, for every updated attribute, only the last recorded change.
     *
     * <p>The relative order of the retained changes is the order of their last occurrence. Events that are not
     * attribute updates are all retained, since they cannot be identified by an attribute.</p>
     */
    public static List<NetworkEvent> compactEvents(Collection<NetworkEvent> events) {
        Objects.requireNonNull(events);

        List<NetworkEvent> reversedEvents = new ArrayList<>(events);
        Collections.reverse(reversedEvents);
        List<NetworkEvent> compactedEvents = new ArrayList<>(reversedEvents.size());
        Set<UpdateKey> retainedUpdates = new HashSet<>();
        for (NetworkEvent event : reversedEvents) {
            Objects.requireNonNull(event);
            if (event instanceof UpdateNetworkEvent updateEvent) {
                if (retainedUpdates.add(new UpdateKey(updateEvent.id(), updateEvent.attribute()))) {
                    compactedEvents.add(event);
                }
            } else {
                compactedEvents.add(event);
            }
        }
        Collections.reverse(compactedEvents);
        return compactedEvents;
    }

    /**
     * A partial SSH describes a single individual grid model: its header has to reference the SSH it replaces and
     * the equipment model it applies to, and a merged network has one of each per subnetwork.
     */
    private static void checkSingleGridModel(Network network) {
        if (!network.getSubnetworks().isEmpty()) {
            throw new PowsyblException("Network " + network.getId() + " is a merged model with "
                    + network.getSubnetworks().size() + " subnetworks. A partial SSH file describes a single "
                    + "individual grid model, so it has to be exported from each subnetwork separately.");
        }
    }

    private static CgmesMetadataModel initializePartialSshModel(Network network, CgmesExportContext context, ExportOptions exportOptions) {
        CgmesMetadataModel model = CgmesExport.initializeModelForExport(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, context, true, false);
        Optional<CgmesMetadataModel> sourceSshModel = getSourceSshModel(network);

        if (exportOptions.description != null) {
            model.setDescription(exportOptions.description);
        }
        if (exportOptions.version != null) {
            model.setVersion(exportOptions.version);
        } else {
            sourceSshModel.ifPresent(sourceModel -> model.setVersion(sourceModel.getVersion() + 1));
        }
        if (exportOptions.modelingAuthoritySet != null) {
            model.setModelingAuthoritySet(exportOptions.modelingAuthoritySet);
        }

        if (exportOptions.modelId != null) {
            model.setId(exportOptions.modelId);
        } else {
            CgmesExportUtil.initializeModelId(network, model, context);
        }

        if (exportOptions.clearDependencies) {
            model.clearDependencies();
        }
        model.addDependentOn(exportOptions.dependentOn);

        model.clearSupersedes();
        if (exportOptions.supersedePreviousSshModel) {
            sourceSshModel.map(CgmesMetadataModel::getId)
                    .filter(id -> id != null && !id.isEmpty())
                    .filter(id -> !id.equals(model.getId()))
                    .ifPresent(model::addSupersedes);
        }
        exportOptions.supersedes.stream()
                .filter(id -> !id.equals(model.getId()))
                .forEach(model::addSupersedes);

        return model;
    }

    private static Optional<CgmesMetadataModel> getSourceSshModel(Network network) {
        CgmesMetadataModels networkModels = network.getExtension(CgmesMetadataModels.class);
        return networkModels != null ? networkModels.getModelForSubset(CgmesSubset.STEADY_STATE_HYPOTHESIS) : Optional.empty();
    }

    private static void write(PartialSshUpdates updates, XMLStreamWriter writer, CgmesExportContext context,
                              CgmesMetadataModel model, Network network) throws XMLStreamException {
        String cimNamespace = context.getCim().getNamespace();
        CgmesExportUtil.writeRdfRoot(cimNamespace, context.getCim().getEuPrefix(), context.getCim().getEuNamespace(), writer);
        if (context.getCimVersion() >= 16) {
            CgmesExportUtil.writeModelDescription(network, CgmesSubset.STEADY_STATE_HYPOTHESIS, writer, model, context);
        }
        updates.write(cimNamespace, writer, context);
        writer.writeEndDocument();
    }

    private record UpdateKey(String identifiableId, String attribute) {
    }
}
