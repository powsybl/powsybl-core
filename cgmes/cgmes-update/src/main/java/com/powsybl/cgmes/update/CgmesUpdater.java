package com.powsybl.cgmes.update;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

public class CgmesFromIidmModifier {

    public CgmesFromIidmModifier(Network network, List<IidmChangesObject> changes) {
        this.network = network;
        this.changes = changes;
    }

    public void addListenerForUpdates() {
        LOGGER.info("Calling addListener on changes...");

        ChangesListener changeListener = new ChangesListener(network, changes);
        network.addListener(changeListener);
    }

    public CgmesModel mapIidmChangesToCgmesModel() {
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

        String contextName = "<contexts:case1_EQ.xml>";

        for (IidmChangesObject change : changes) {
            PropertyBags result = cgmes.updateCgmesfromIidmBySparql(contextName,
                change.getIdentifiable().toString(),
                change.getOldValueString(),
                change.getNewValueString());
            LOGGER.info("getting result......");
            LOGGER.info(result.tabulateLocals());
            LOGGER.info(contextName);
            LOGGER.info(change.getIdentifiable() + " " + change.getOldValueString()
                + " " + change.getNewValueString());
        }
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(cgmes.created().toString("yyyy-MM-dd HH:mm:ss"));
        }
    }

    private Network network;
    private CgmesModel cgmes;
    private List<IidmChangesObject> changes;
    private static final Logger LOGGER = LoggerFactory.getLogger(CgmesFromIidmModifier.class);
}
