package com.powsybl.cgmes.update;

import java.util.List;

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

    public CgmesModel mapIidmChangesToCgmesModel() {
        if (network.getExtension(CgmesModelExtension.class) != null) {
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();
            LOG.info("RUNNING FROM CgmesUpdater.mapIidmChangesToCgmesModel(");
            cgmes.print(LOG::info);
            iidmToCgmes = new IidmToCgmes(changes);
            cgmesChanges = iidmToCgmes.convert(changes);
            applyIidmChangesToCgmes(cgmes, cgmesChanges);
        } else {
            LOG.info("No cgmes reference is available.");
        }
        return cgmes;
    }

    private void applyIidmChangesToCgmes(CgmesModel cgmes, List<String> cgmesChanges) {
        LOG.info("Applying IIDM changes to CGMES...");

        String contextName = "<contexts:case1_EQ.xml>";

        for (IidmChangeOnUpdate change : changes) {
            PropertyBags result = cgmes.updateCgmes(contextName,
                change.getIdentifiable().toString(),
                change.getOldValueString(),
                change.getNewValueString());
            LOG.info("Getting result......");
            LOG.info(result.tabulateLocals());
            LOG.info(contextName);
            LOG.info(change.getIdentifiable() + " " + change.getOldValueString()
                + " " + change.getNewValueString() + " " + cgmesChanges);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info(cgmes.created().toString("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public List<IidmChangeOnUpdate> getChanges() {
        return changes;
    }

    private Network network;
    private CgmesModel cgmes;
    private List<IidmChangeOnUpdate> changes;
    private IidmToCgmes iidmToCgmes;
    private List<String> cgmesChanges;
    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdater.class);
}
