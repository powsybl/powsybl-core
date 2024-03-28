/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.SerializerContext;
import com.powsybl.commons.io.TreeDataWriter;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.serde.anonymizer.Anonymizer;

import javax.xml.stream.XMLStreamWriter;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class NetworkSerializerContext extends AbstractNetworkSerDeContext<ExportOptions> implements SerializerContext {

    private final TreeDataWriter writer;
    private final ExportOptions options;
    private final BusFilter filter;
    private final boolean valid;
    private final Set<Identifiable> exportedEquipments;

    NetworkSerializerContext(Anonymizer anonymizer, TreeDataWriter writer, ExportOptions options, BusFilter filter, IidmVersion version, boolean valid) {
        super(anonymizer, version);
        this.writer = Objects.requireNonNull(writer);
        this.options = options;
        this.filter = filter;
        this.valid = valid;
        this.exportedEquipments = new HashSet<>();
    }

    @Override
    public TreeDataWriter getWriter() {
        return writer;
    }

    @Override
    public ExportOptions getOptions() {
        return options;
    }

    public BusFilter getFilter() {
        return filter;
    }

    public boolean isValid() {
        return valid;
    }

    /**
     * @deprecated Should not be used anymore.
     */
    @Deprecated(since = "3.8.1")
    public void setExtensionsWriter(XMLStreamWriter extensionsWriter) {
        // does nothing
        // only kept to prevent breaking change
    }

    public Set<Identifiable> getExportedEquipments() {
        return Collections.unmodifiableSet(exportedEquipments);
    }

    public void addExportedEquipment(Identifiable<?> equipment) {
        exportedEquipments.add(equipment);
    }

    public boolean isExportedEquipment(Identifiable<?> equipment) {
        return exportedEquipments.contains(equipment);
    }

    public Optional<String> getExtensionVersion(String extensionName) {
        return options.getExtensionVersion(extensionName);
    }

    public String getNamespaceURI() {
        return getVersion().getNamespaceURI(valid);
    }
}
