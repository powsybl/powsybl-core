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

    public CgmesModel update() throws Exception {
        if (network.getExtension(CgmesModelExtension.class) != null) {
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();

            for (IidmChange change : changes) {

                cgmesSubject = namingStrategy.getCgmesId(change.getIdentifiableId());
                String instanceClassOfIidmChange = instanceClassOfIidmChange(change);

                IidmToCgmes iidmToCgmes = new IidmToCgmes(change);
                mapChangeDetails = iidmToCgmes.convert();
                // we need to iterate over the above map, as for onCreate call there will be
                // multiples attributes-values pairs.
                Iterator entries = mapChangeDetails.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    CgmesPredicateDetails map = (CgmesPredicateDetails) entry.getKey();
                    cgmesPredicate = map.getAttributeName();
                    cgmesValue = (String) entry.getValue();
                    currentContext = map.getContext();
                    valueIsNode = String.valueOf(map.valueIsNode());

                    for (String context : cgmes.tripleStore().contextNames()) {

                        cgmesChanges = new HashMap<>();
                        cgmesChanges.put("cgmesSubject", cgmesSubject);
                        cgmesChanges.put("cgmesPredicate", cgmesPredicate);
                        cgmesChanges.put("cgmesNewValue", cgmesValue);
                        cgmesChanges.put("valueIsNode", valueIsNode);

                        // check if the current triplestore context contains the context-string mapped
                        // in the IidmToCgmes class converter. If yes - call update.
                        if (context.toUpperCase().contains(currentContext)) {

                            PropertyBags result = cgmes.updateCgmes(context, cgmesChanges,
                                instanceClassOfIidmChange);

                            LOG.info(result.tabulate());
                        }
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
    private List<IidmChange> changes;
    private NamingStrategy namingStrategy;
    private String cgmesSubject;
    private String cgmesPredicate;
    private String cgmesValue;
    private String currentContext;
    private String valueIsNode;
    private Map<String, String> cgmesChanges;
    private Map<CgmesPredicateDetails, String> mapChangeDetails;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdater.class);
}
