/**
 * Copyright (c) 2026, Elia Group (http://www.eliagroup.eu)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.export;

import com.powsybl.cgmes.model.statement.CgmesObject;

/**
 * The SSH data of one CGMES object, collected by the SSH export from the network.
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public interface SshRecord {

    /**
     * Get the SSH statements of this record, grouped in one object.
     *
     * @param cimNamespace the CIM namespace of the export, for the enumeration values
     */
    CgmesObject normalize(String cimNamespace);
}
