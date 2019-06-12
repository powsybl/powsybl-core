/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//import com.powsybl.iidm.network.NetworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkListener;

public class ChangeListener implements NetworkListener {
    // class register network changes, and add to list

    public ChangeListener(Network network) {
        this.network = Objects.requireNonNull(network);
        this.changeList = new ArrayList<Changes>();
    }

    public void onCreation(Identifiable identifiable) {
        LOGGER.info("calling onCreation method");
        String variant = network.getVariantManager().getWorkingVariantId();
        Changes change = new Changes(identifiable, variant);
        changeList.add(change);
    }

    public void onRemoval(Identifiable identifiable) {
        LOGGER.info("calling onRemoval method");
        String variant = network.getVariantManager().getWorkingVariantId();
        Changes change = new Changes(identifiable, variant);
        changeList.add(change);
    }

    public void onUpdate(Identifiable identifiable, String attribute, Object oldValue, Object newValue) {
        LOGGER.info("calling onUpdate method");
        String variant = network.getVariantManager().getWorkingVariantId();
        Changes change = new Changes(identifiable, attribute, oldValue, newValue, variant);
        changeList.add(change);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeListener.class);
    private final Network network;
    private List<Changes> changeList;

}
