package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Profiling;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;

public class CgmesUpdate {

    public CgmesUpdate(Network network) {
        this.changes = new ArrayList<>();
        ChangesListener changeListener = new ChangesListener(changes);
        network.addListener(changeListener);
    }

    /**
     * Update. Prepare triple to pass to SPARQL statement.
     *
     * @throws Exception the exception
     */
    public void update(CgmesModel cgmes, String variantId, Profiling profiling) throws Exception {

        String cimNamespace = cgmes.getCimNamespace();
        String cimVersion = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));

        // XXX LUMA refactor mapping between IIDM and CGMES:
        // IidmToCgmes iidmToCgmes = new IidmToCgmes(cimVersion);

        int changesSize = changes.size();
        int changesCounter = 0;

        // XXX LUMA dirty debug
        System.err.println("numChanges " + changesSize);

        for (IidmChange change : changes) {
            changesCounter++;
            if (change.getVariant() == null || change.getVariant().equals(variantId)) {

                // XXX LUMA refactor mapping between IIDM and CGMES:
                // allCgmesDetails = iidmToCgmes.convert(change, cgmes);
                List<CgmesPredicateDetails> allCgmesDetails = iidmToCgmes(cimVersion, change, cgmes).convert();

                // XXX LUMA dirty debug
                if (changesCounter % (changesSize / 10) == 0) {
                    System.err.printf("    %3d change %4d %1d%n", (100 * changesCounter) / changesSize, changesCounter, allCgmesDetails.size());
                }

                // we need to iterate over the above map, as for onCreate call there will be
                // multiples attributes-values pairs.
                Iterator entries = allCgmesDetails.iterator();
                while (entries.hasNext()) {
                    CgmesPredicateDetails entry = (CgmesPredicateDetails) entries.next();
                    try {
                        for (String context : cgmes.tripleStore().contextNames()) {

                            // TODO elena : will need to add a logic to find the right context
                            // XXX LUMA this map can be moved to the top
                            // (only relates entry.getContext with current context in cgmes)
                            if (context.toUpperCase().contains(entry.getContext().toUpperCase())
                                && !context.toUpperCase().contains("BD")
                                && !context.toUpperCase().contains("BOUNDARY")) {

                                profiling.startLoopIteration();
                                String subject = (entry.getNewSubject() != null) ? entry.getNewSubject() : change.getIdentifiableId();
                                String predicate = entry.getRdfPredicate();
                                String newValue = entry.getValue();
                                String valueIsNode = String.valueOf(entry.valueIsNode());
                                cgmes.updateCgmes(
                                    queryName(change),
                                    context, cgmes.getBaseName(),
                                    subject,
                                    predicate,
                                    newValue,
                                    valueIsNode);
                                profiling.endLoopIteration();
                            }
                        }
                    } catch (java.lang.NullPointerException e) {
                        LOG.error("Requested attribute {} is not available for conversion\n{}", change.getAttribute(),
                            e.getMessage());
                    }
                }
            }
        }
    }

    private String queryName(IidmChange change) {
        String queryName = null;
        if (change instanceof IidmChangeOnUpdate) {
            queryName = "updateCgmes";
        } else if (change instanceof IidmChangeOnCreate) {
            queryName = "updateCgmesCreate";
        } else if (change instanceof IidmChangeOnRemove) {
            queryName = "updateCgmesRemove";
        }

        return queryName;
    }

    private AbstractIidmToCgmes iidmToCgmes(String cimVersion, IidmChange change, CgmesModel cgmes) {
        AbstractIidmToCgmes iidmToCgmes = null;
        if (cimVersion.equals("cim14#")) {
            iidmToCgmes = new IidmToCgmes14(change, cgmes);
        } else {
            iidmToCgmes = new IidmToCgmes16(change, cgmes);
        }
        return iidmToCgmes;
    }

    public List<IidmChange> changes() {
        return Collections.unmodifiableList(changes);
    }

    private List<IidmChange> changes;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdate.class);
}
