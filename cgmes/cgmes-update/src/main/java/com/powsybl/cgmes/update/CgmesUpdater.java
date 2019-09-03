package com.powsybl.cgmes.update;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.conversion.NamingStrategy;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

public class CgmesUpdater {

    public CgmesUpdater(Network network, List<IidmChange> changes) {
        this.network = network;
        this.changes = changes;
        this.namingStrategy = new NamingStrategy.Identity();
    }

    public void addListenerForUpdates() {
        LOG.info("Calling addListener on changes...");

        ChangesListener changeListener = new ChangesListener(network, changes);
        network.addListener(changeListener);
    }

    /**
     * Update. Prepare triple to pass to SPARQL statement.
     *
     * @return the cgmes model
     * @throws Exception the exception
     */
    public CgmesModel update() throws Exception {
        if (network.getExtension(CgmesModelExtension.class) != null) {
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();

            String cimNamespace = cgmes.getCimNamespace();
            String cimVersion = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));

            for (IidmChange change : changes) {

                if (cimVersion.equals("cim14#")) {
                    iidmToCgmes = new IidmToCgmes14(change, cgmes);
                } else if (cimVersion.equals("cim16#")) {
                    iidmToCgmes = new IidmToCgmes16(change, cgmes);
                } else {
                    LOG.info("Incoming cim verson must be checked. Implemented versions are: \ncim14# ; cim16# ");
                }

                List<CgmesPredicateDetails> allCgmesDetails = iidmToCgmes.convert();

                // we need to iterate over the above map, as for onCreate call there will be
                // multiples attributes-values pairs.
                Iterator entries = allCgmesDetails.iterator();
                while (entries.hasNext()) {
                    CgmesPredicateDetails entry = (CgmesPredicateDetails) entries.next();
                    try {
                        for (String context : cgmes.tripleStore().contextNames()) {

                            String currentContext = entry.getContext();

                            if (context.toUpperCase().contains(currentContext)) {

                                String cgmesSubject = (entry.getNewSubject() != null) ? entry.getNewSubject()
                                    : namingStrategy.getCgmesId(change.getIdentifiableId());

                                String cgmesPredicate = entry.getRdfPredicate();
                                String cgmesValue = entry.getValue();
                                String valueIsNode = String.valueOf(entry.valueIsNode());

                                Map<String, String> cgmesChanges = new HashMap<>();
                                cgmesChanges.put("cgmesSubject", cgmesSubject);
                                cgmesChanges.put("cgmesPredicate", cgmesPredicate);
                                cgmesChanges.put("cgmesNewValue", cgmesValue);
                                cgmesChanges.put("valueIsNode", valueIsNode);

                                PropertyBags result = cgmes.updateCgmes(context, cgmesChanges,
                                    instanceClassOfIidmChange(change));

                                LOG.info(result.tabulate());
                            }
                        }
                    } catch (java.lang.NullPointerException e) {
                        LOG.error("Requested attribute {} is not available for conversion\n{}", change.getAttribute(),
                            e.toString());
                    }

                }
            }

        } else {
            LOG.info("No cgmes reference is available.");
        }
        return cgmes;
    }

    private String instanceClassOfIidmChange(IidmChange change) {
        return change.getClass().getSimpleName();
    }

    private Network network;
    private CgmesModel cgmes;
    private AbstractIidmToCgmes iidmToCgmes;
    private List<IidmChange> changes;
    private NamingStrategy namingStrategy;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdater.class);
}
