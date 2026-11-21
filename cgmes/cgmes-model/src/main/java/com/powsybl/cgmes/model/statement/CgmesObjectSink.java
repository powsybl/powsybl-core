/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import javax.xml.stream.XMLStreamException;

/**
 * Receives the {@link CgmesObject} instances built by an export.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
@FunctionalInterface
public interface CgmesObjectSink {

    void accept(CgmesObject cgmesObject) throws XMLStreamException;
}
