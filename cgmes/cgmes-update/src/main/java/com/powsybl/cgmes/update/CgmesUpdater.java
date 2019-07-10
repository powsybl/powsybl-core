package com.powsybl.cgmes.update;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

public class CgmesUpdater {

    public CgmesUpdater(Network network, List<IidmChangeOnUpdate> changes) {
        this.network = network;
        this.changes = changes;
    }

    public void addListenerForUpdates() {
        LOG.info("Calling addListener on changes...");

        ChangesListener changeListener = new ChangesListener(network, changes);
        network.addListener(changeListener);
    }

    public CgmesModel update() throws Exception {
        if (network.getExtension(CgmesModelExtension.class) != null) {
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();

            for (IidmChangeOnUpdate change : changes) {
                for (String context : cgmes.tripleStore().contextNames()) {
                    LOG.info("Update cgmes for: " + context);

                    iidmToCgmes = new IidmToCgmes(change);
                    cgmesChanges = iidmToCgmes.convert(change);
                    String contextName = context;

                    PropertyBags result = cgmes.updateCgmes(contextName, cgmesChanges);
                    LOG.info(result.tabulate());
                }
            }

        } else {
            LOG.info("No cgmes reference is available.");
        }
        return cgmes;
    }

    private Network network;
    private CgmesModel cgmes;
    private List<IidmChangeOnUpdate> changes;
    private IidmToCgmes iidmToCgmes;
    private Map<String, String> cgmesChanges;
    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdater.class);
}
