/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.statement.CgmesObject;
import com.powsybl.cgmes.model.statement.CgmesObjectXmlWriter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * <p>The serialization of the CGMES Steady State Hypothesis profile.</p>
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class SteadyStateHypothesisWriter extends AbstractCgmesWriter {

    private final CgmesObjectXmlWriter objectWriter;

    /**
     * Create a {@link SteadyStateHypothesisWriter} instance for writing the CGMES Steady State Hypothesis profile.
     *
     * @param writer the {@link XMLStreamWriter} to serialize the CGMES Steady State Hypothesis profile.
     * @param context the {@link CgmesExportContext} containing the export context.
     */
    public SteadyStateHypothesisWriter(XMLStreamWriter writer, CgmesExportContext context) {
        super(writer, context);
        objectWriter = new CgmesObjectXmlWriter(writer, cimNamespace, this::toRdfAbout);
    }

    /**
     * Write the given object to the CGMES Steady State Hypothesis profile.
     */
    public void write(CgmesObject cgmesObject) throws XMLStreamException {
        objectWriter.write(cgmesObject);
    }
}
