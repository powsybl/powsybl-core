/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.SlackTerminal;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.extensions.TerminalWithPriorityImpl;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;

import java.util.List;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
class SlackConverter extends AbstractConverter {

    SlackConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    void create(List<DataObject> slackGenerators) {

        for (DataObject slackGenerator : slackGenerators) {

            Generator generator = getNetwork().getGenerator(GeneratorConverter.getId(slackGenerator));
            if (generator == null) {
                continue;
            }
            Terminal terminal = generator.getTerminal();
            VoltageLevel voltageLevel = terminal.getVoltageLevel();
            SlackTerminal ext = voltageLevel.getExtension(SlackTerminal.class);
            if (ext == null) {
                voltageLevel.newExtension(SlackTerminalAdder.class).withTerminal(terminal, 1).add();
            } else {
                ext.addTerminal(new TerminalWithPriorityImpl(terminal, 1));
            }
        }
    }
}
