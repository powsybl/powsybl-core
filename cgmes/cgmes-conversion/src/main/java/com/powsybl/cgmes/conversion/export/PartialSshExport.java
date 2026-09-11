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
 * Exports the changes recorded on a network as a partial CGMES .ssh file.
 *
 * <p>Where the regular {@link SteadyStateHypothesisExport} writes the complete steady state of a network, this
 * exporter writes only the objects affected by a list of {@link NetworkEvent}s, typically collected with a
 * {@code NetworkEventRecorder}. The result is a valid SSH instance file that a receiver holding the same base
 * model can apply through the network update workflow.</p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * NetworkEventRecorder recorder = new NetworkEventRecorder();
 * network.addListener(recorder);
 * network.getGenerator("G").setTargetP(120.0);
 * String ssh = PartialSshExport.toString(network, recorder.getEvents(), UnsupportedChangeBehavior.FAIL);
 * }</pre>
 *
 * <p>Changes that have no representation in the SSH profile or whose import/export is not implemented, such as the
 * creation or removal of equipment, are either reported as an error or skipped with a warning:
 * {@link UnsupportedChangeBehavior#FAIL} makes sure a change is never silently lost, while
 * {@link UnsupportedChangeBehavior#IGNORE} means only the supported changes are written to file. Skipped elements are
 * logged and the write method returns a list of written events for cross-reference.</p>
 *
 * <p>A partial SSH describes a single individual grid model, because its header has to reference the SSH it
 * replaces and the equipment model it applies to. A merged network has one of each per subnetwork, so it cannot be
 * exported as a whole, subnetworks have to be passed in one at a time.</p>
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
public final class PartialSshExport {

    /**
     * What to do with a recorded change that cannot be written to a Steady State Hypothesis file.
     */
    public enum UnsupportedChangeBehavior {
        /** Throw a {@link PowsyblException} describing the change. This is what {@link ExportOptions} defaults to. */
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

    private PartialSshExport() {
    }

    /**
     * Export the given changes as a partial .ssh file and return it as an in-memory string.
     *
     * <p>Shorthand for {@link #write(Network, Collection, OutputStream, ExportOptions)} that collects the file in
     * memory.</p>
     *
     * @param network                   the network the changes were recorded on. It has to be an individual grid
     *                                  model, a merged network has to be exported one subnetwork at a time
     * @param events                    the recorded changes to write
     * @param unsupportedChangeBehavior what to do with a change that can not be written
     * @return the partial SSH instance file as an XML document
     * @throws PowsyblException            if the network is a merged model, or if one of the changes has no
     *                                     representation in the SSH profile and the behavior is
     *                                     {@link UnsupportedChangeBehavior#FAIL}
     * @throws UncheckedXmlStreamException if the XML document cannot be written
     */
    public static String toString(Network network, Collection<NetworkEvent> events, UnsupportedChangeBehavior unsupportedChangeBehavior) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        write(network, events, outputStream, new ExportOptions().setUnsupportedChangeBehavior(unsupportedChangeBehavior));
        return outputStream.toString(StandardCharsets.UTF_8);
    }

    /**
     * Export the given changes as a partial .ssh file on disk
     *
     * <p>Shorthand for {@link #write(Network, Collection, OutputStream, ExportOptions)} that opens a file and writes
     * it
     * </p>
     *
     * @param network                   the network the changes were recorded on. It has to be an individual grid
     *                                  model, a merged network has to be exported one subnetwork at a time
     * @param events                    the recorded changes to write
     * @param filePath                  the file to write the instance file to
     * @param unsupportedChangeBehavior what to do with a change that has no representation in the SSH profile
     * @return the changes that reached the file, as described by
     *         {@link #write(Network, Collection, OutputStream, ExportOptions)}
     * @throws IOException                 if the file cannot be opened or written
     * @throws PowsyblException            if the network is a merged model, or if one of the changes has no
     *                                     representation in the SSH profile and the behavior is
     *                                     {@link UnsupportedChangeBehavior#FAIL}
     * @throws UncheckedXmlStreamException if the XML document cannot be written
     */
    public static List<NetworkEvent> write(Network network, Collection<NetworkEvent> events, Path filePath, UnsupportedChangeBehavior unsupportedChangeBehavior) throws IOException {
        Objects.requireNonNull(filePath);
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(filePath))) {
            return write(network, events, outputStream, new ExportOptions().setUnsupportedChangeBehavior(unsupportedChangeBehavior));
        }
    }

    /**
     * Export the given changes as a partial .ssh file and write it to an output stream.
     *
     * <p>The events are first compacted as described in {@link #compactEvents(Collection)} before they are
     * written.</p>
     *
     * @param network       the network the changes were recorded on. It has to be an individual grid model, a
     *                      merged network has to be exported one subnetwork at a time. It also provides the
     *                      metadata of the source SSH, from which the header of the exported model is derived
     * @param events        the recorded changes to write, typically those collected by a {@code NetworkEventRecorder}
     * @param outputStream  the stream to write the instance file to. It is neither flushed nor closed by this
     *                      method
     * @param exportOptions the header values and the unsupported change behavior of the export
     * @return the changes that reached the file, in the order in which they were written. With
     *         {@link UnsupportedChangeBehavior#FAIL} the result is exactly {@link #compactEvents(Collection)}.
     *         With {@link UnsupportedChangeBehavior#IGNORE} it is a subset of that.
     * @throws PowsyblException            if the network is a merged model, or something could not be exported.
     * @throws UncheckedXmlStreamException if the XML document cannot be written
     */
    public static List<NetworkEvent> write(Network network, Collection<NetworkEvent> events, OutputStream outputStream, ExportOptions exportOptions) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(events);
        Objects.requireNonNull(outputStream);
        Objects.requireNonNull(exportOptions);
        checkSingleGridModel(network);

        CgmesExportContext context = new CgmesExportContext(network);
        CgmesMetadataModel model = initializeExportMetadata(network, context, exportOptions);
        PartialSshEventTranslator translator = new PartialSshEventTranslator(network, context, exportOptions.unsupportedChangeBehavior);
        PartialSshUpdates updates = translator.translateAll(compactEvents(events));

        try {
            XMLStreamWriter writer = XmlUtil.initializeWriter(true, "    ", outputStream);
            write(updates, writer, context, model, network);
        } catch (XMLStreamException e) {
            throw new UncheckedXmlStreamException(e);
        }
        return List.copyOf(translator.exportedEvents());
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

    private static CgmesMetadataModel initializeExportMetadata(Network network, CgmesExportContext context, ExportOptions exportOptions) {
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
