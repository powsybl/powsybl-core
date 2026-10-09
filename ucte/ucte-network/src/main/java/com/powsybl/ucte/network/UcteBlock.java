/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * Copyright (c) 2026, TenneT (https://www.tennet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

/**
 * Top-level blocks of a UCTE-DEF file in front of which a comment block can be written.
 * <p>
 * A comment block inside the node block, e.g. in front of a ##Z block, is not supported, because
 * it could not be read back.
 *
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public enum UcteBlock {
    NODES,
    LINES,
    TRANSFORMERS,
    REGULATIONS
}
