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
     * Update. Prepare triple to pass to SPARQL statement. For cgmesSubject we might
     * need Id diferent from the incoming Identifiable, e.g. for
     * TwoWindingsTransformer, where End1 and End2 have their oun Ids.
     *
     * @return the cgmes model
     * @throws Exception the exception
     */
    public CgmesModel update() throws Exception {
        if (network.getExtension(CgmesModelExtension.class) != null) {
            CgmesModel cgmes = network.getExtension(CgmesModelExtension.class).getCgmesModel();

            String cimNamespace = cgmes.getCimNamespace();
            cimVersion = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));
            // cimVersion = "cim16#";

            for (IidmChange change : changes) {

                String instanceClassOfIidmChange = instanceClassOfIidmChange(change);

                if (cimVersion.equals("cim14#")) {
                    iidmToCgmes = new IidmToCgmes(change, cgmes);
                } else if (cimVersion.equals("cim16#")) {
                    iidmToCgmes = new IidmToCgmes16(change, cgmes);
                } else {
                    LOG.info("Incoming cim verson must be checked. Implemented versions are: \ncim14# ; cim16# ");
                }

                mapDetailsOfChange = iidmToCgmes.convert();

                // we need to iterate over the above map, as for onCreate call there will be
                // multiples attributes-values pairs.
                Iterator entries = mapDetailsOfChange.entrySet().iterator();
                while (entries.hasNext()) {
                    Map.Entry entry = (Map.Entry) entries.next();
                    CgmesPredicateDetails map = (CgmesPredicateDetails) entry.getKey();

                    try {
                        for (String context : cgmes.tripleStore().contextNames()) {

                            cgmesSubject = (map.getNewSubject() != null) ? map.getNewSubject()
                                : namingStrategy.getCgmesId(change.getIdentifiableId());

                            cgmesPredicate = map.getAttributeName();
                            cgmesValue = (String) entry.getValue();
                            currentContext = map.getContext();
                            valueIsNode = String.valueOf(map.valueIsNode());

                            if (context.toUpperCase().contains(currentContext)) {

                                cgmesChanges = new HashMap<>();
                                cgmesChanges.put("cgmesSubject", cgmesSubject);
                                cgmesChanges.put("cgmesPredicate", cgmesPredicate);
                                cgmesChanges.put("cgmesNewValue", cgmesValue);
                                cgmesChanges.put("valueIsNode", valueIsNode);

                                PropertyBags result = cgmes.updateCgmes(context, cgmesChanges,
                                    instanceClassOfIidmChange);

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
    private IidmToCgmes iidmToCgmes;
    private String cimVersion;
    private List<IidmChange> changes;
    private NamingStrategy namingStrategy;
    private String cgmesSubject;
    private String cgmesPredicate;
    private String cgmesValue;
    private String currentContext;
    private String valueIsNode;
    private Map<String, String> cgmesChanges;
    private Map<CgmesPredicateDetails, String> mapDetailsOfChange;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdater.class);
}
