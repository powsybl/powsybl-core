package com.powsybl.cgmes.update;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;

public class CgmesFromIidmModifier {

    public CgmesFromIidmModifier(Network network, List<IidmChangesObject> changes) {
        this.network = network;
        this.changes = changes;
    }

    public void addListenerForUpdates() {
        LOGGER.info("Calling addListener on changes...");

        ChangeListener changeListener = new ChangeListener(network, changes);
        network.addListener(changeListener);
    }

    public CgmesModel mapIidmChangesToCgmes() {
        if (network.getExtension(CgmesModelExtension.class) != null) {
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
            cgmes.print(LOGGER::info);
            applyIidmChangesToCgmes(cgmes);
        } else {
            LOGGER.info("No cgmes reference is available.");
        }
        return cgmes;
    }

    private void applyIidmChangesToCgmes(CgmesModel cgmes) {
        LOGGER.info("Applyong IIDM changes to CGMES...");
        cgmes.updateCgmesfromIidm();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(cgmes.created().toString("yyyy-MM-dd HH:mm:ss"));
        }
    }

    private Network network;
    private CgmesModel cgmes;
    private List<IidmChangesObject> changes;
    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesFromIidmModifier.class);
}
