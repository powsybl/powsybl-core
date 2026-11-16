/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

/**
 * The data of one CGMES object, built by an export before it is written.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public interface CgmesStatement {

    /**
     * The identifier of the CGMES object described by this statement.
     */
    String rdfId();
}
