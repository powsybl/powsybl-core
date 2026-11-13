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

/**
 * Receives the statements built by the SSH export.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@FunctionalInterface
public interface SshSink {

    void accept(SshStatement statement) throws XMLStreamException;

    /**
     * A sink that writes each statement as soon as it receives it.
     */
    static SshSink immediate(XMLStreamWriter writer, CgmesExportContext context) {
        return statement -> statement.writeSsh(writer, context);
    }
}
