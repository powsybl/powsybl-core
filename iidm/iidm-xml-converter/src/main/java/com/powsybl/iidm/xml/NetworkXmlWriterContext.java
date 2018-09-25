/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.powsybl.commons.xml.XmlWriterContext;
import com.powsybl.iidm.anonymizer.Anonymizer;
import com.powsybl.iidm.ConverterContext;
import com.powsybl.iidm.export.BusFilter;
import com.powsybl.iidm.export.ExportOptions;
import com.powsybl.iidm.network.Identifiable;

import javax.xml.stream.XMLStreamWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkXmlWriterContext extends ConverterContext implements XmlWriterContext {

    private final XMLStreamWriter writer;
    private final ExportOptions options;
    private final BusFilter filter;
    private final Set<Identifiable> exportedEquipments;

    NetworkXmlWriterContext(Anonymizer anonymizer, XMLStreamWriter writer, ExportOptions options, BusFilter filter) {
        super(anonymizer);
        this.writer = writer;
        this.options = options;
        this.filter = filter;

        this.exportedEquipments = new HashSet<>();
    }

    @Override
    public XMLStreamWriter getWriter() {
        return writer;
    }

    public ExportOptions getOptions() {
        return options;
    }

    public BusFilter getFilter() {
        return filter;
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
}
