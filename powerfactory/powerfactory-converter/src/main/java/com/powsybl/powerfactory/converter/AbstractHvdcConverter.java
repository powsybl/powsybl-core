/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Network;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 *
 *         Base importer from the DGS data model for DC subgrids.
 *         This enables derivation to differentiate simplified and detailed HVDC
 *         subgrids.
 */
abstract class AbstractHvdcConverter extends AbstractConverter {

    AbstractHvdcConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    /**
     * Check if a given DGS terminal is in a DC subgrid.
     *
     * @param elmTerm Terminal to check.
     * @return true iff the terminal is in a DC subgrid.
     */
    abstract boolean isDcNode(DataObject elmTerm);

    /**
     * Check if a given DGS line is in a DC subgrid.
     *
     * @param elmLne Line to check.
     * @return true iff the line is in a DC subgrid.
     */
    abstract boolean isDcLink(DataObject elmLne);

    /**
     * Create the DC subgrids in the network.
     */
    abstract void create();

}
