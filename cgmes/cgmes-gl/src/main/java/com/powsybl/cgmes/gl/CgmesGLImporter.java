/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.gl;

import com.powsybl.iidm.network.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class CgmesGLImporter {

    private static final Logger LOG = LoggerFactory.getLogger(CgmesGLImporter.class);

    private Network network;
    private CgmesGLModel cgmesGLModel;

    public CgmesGLImporter(Network network, CgmesGLModel cgmesGLModel) {
        this.network = Objects.requireNonNull(network);
        this.cgmesGLModel = Objects.requireNonNull(cgmesGLModel);
    }

    public void importGLData() {
        importSubstationsPosition();
        importLinesPosition();
    }

    private void importSubstationsPosition() {
        LOG.info("Importing substations position");
        SubstationPositionImporter positionImporter = new SubstationPositionImporter(network);
        cgmesGLModel.getSubstationsPosition().forEach(positionImporter::importPosition);
    }

    private void importLinesPosition() {
        LOG.info("Importing lines position");
        new LinePositionImporter(network, cgmesGLModel).importPosition();
    }

}
