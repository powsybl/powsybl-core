/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A sink that collects the SSH statements and writes them later.
 * It keeps one statement for each CGMES object: when a statement arrives for an object that it already has,
 * the new statement replaces the previous one, and the object keeps its first position.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public final class DeferredSshSink implements SshSink {

    private final Map<String, SshStatement> statements = new LinkedHashMap<>();

    @Override
    public void accept(SshStatement statement) {
        statements.put(statement.rdfId(), statement);
    }

    /**
     * The collected statements, one for each CGMES object, in the order of their first arrival.
     */
    public Collection<SshStatement> getStatements() {
        return statements.values();
    }

    /**
     * Write all the collected statements.
     */
    public void writeAll(XMLStreamWriter writer, CgmesExportContext context) throws XMLStreamException {
        for (SshStatement statement : statements.values()) {
            statement.writeSsh(writer, context);
        }
    }
}
