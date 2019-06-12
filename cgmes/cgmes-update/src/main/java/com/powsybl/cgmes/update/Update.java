package com.powsybl.cgmes.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Network;

public class Update {

    void addListener(Network network) {

        LOGGER.info("Calling addListener on changes...");

        ChangeListener changes = new ChangeListener(network);
        network.addListener(changes);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Update.class);

}
