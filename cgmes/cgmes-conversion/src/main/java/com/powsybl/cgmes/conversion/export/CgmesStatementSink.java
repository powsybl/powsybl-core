/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import javax.xml.stream.XMLStreamException;

/**
 * Receives the statements built by an export.
 *
 * @param <T> the type of the statements
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@FunctionalInterface
public interface CgmesStatementSink<T extends CgmesStatement> {

    void accept(T statement) throws XMLStreamException;
}
