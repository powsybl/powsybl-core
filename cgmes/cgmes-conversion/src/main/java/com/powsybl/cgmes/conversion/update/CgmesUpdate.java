/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public class CgmesUpdate {

    public CgmesUpdate(Network network) {
        this.network = network;
        this.changes = new ArrayList<>();
        ChangesListener changeListener = new ChangesListener(changes);
        network.addListener(changeListener);
    }

    /**
     * Update. Prepare triples for SPARQL Update statement.
     *
     * @throws Exception the exception
     */
    public void update(CgmesModel cgmes, String variantId) throws Exception {

        String cimNamespace = cgmes.getCimNamespace();
        String cimVersion = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));
        Map<String, String> contexts = contexts(cgmes);
        // XXX LUMA IidmToCgmes iidmToCgmes = new IidmToCgmes(cimVersion);
        IidmToCgmes iidmToCgmes = new IidmToCgmes(cimVersion);

        int changesSize = changes.size();
        int changesCounter = 0;
        System.err.println("numChanges " + changesSize);
        for (IidmChange change : changes) {
            changesCounter++;
            if (change.getVariant() == null || change.getVariant().equals(variantId)) {

//                List<CgmesPredicateDetails> allCgmesDetails = iidmToCgmes(cimVersion, change, cgmes).convert();
                // XXX LUMA List<CgmesPredicateDetails> allCgmesDetails =
                // iidmToCgmes.convert(change, cgmes);
                List<CgmesPredicateDetails> entries = iidmToCgmes.convert(change, cgmes);
                for (CgmesPredicateDetails entry : entries) {
                    try {
                        PropertyBags result = cgmes.updateCgmes(queryName(change),
                            contexts.get(entry.getContext()),
                            cgmes.getBaseName(),
                            getCgmesChanges(entry, change));

                        LOG.info(result.tabulate());

                    } catch (java.lang.NullPointerException e) {
                        LOG.error("Requested attribute {} is not available for conversion\n{}", change.getAttribute(),
                            e.getMessage());
                    }
                }
            }
        }

    }

    private Map<String, String> contexts(CgmesModel cgmes) {
        Map<String, String> cmap = new HashMap<>();
        for (String context : cgmes.tripleStore().contextNames()) {
            // TODO elena : will need to add a logic to find the right context
            // XXX LUMA this map can be moved to the top (only relates entry.getContext with
            // current context in cgmes)
            if (context.toUpperCase().contains("BD")
                || context.toUpperCase().contains("BOUNDARY")) {
                continue;
            }
            if (context.toUpperCase().contains("_SSH")) {
                cmap.put("_SSH", context);
            }
            if (context.toUpperCase().contains("_EQ")) {
                cmap.put("_EQ", context);
            }
            if (context.toUpperCase().contains("_SV")) {
                cmap.put("_SV", context);
            }
            if (context.toUpperCase().contains("_TP")) {
                cmap.put("_TP", context);
            }

        }
        return cmap;
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

//    private AbstractIidmToCgmes iidmToCgmes(String cimVersion, IidmChange change, CgmesModel cgmes) {
//        AbstractIidmToCgmes iidmToCgmes = null;
//        if (cimVersion.equals("cim14#")) {
//            iidmToCgmes = new IidmToCgmes14(change, cgmes);
//        } else {
//            iidmToCgmes = new IidmToCgmes16(change, cgmes);
//        }
//        return iidmToCgmes;
//    }

    private Map<String, String> getCgmesChanges(CgmesPredicateDetails entry, IidmChange change) {
        Map<String, String> cgmesChanges = new HashMap<>();
        cgmesChanges.put("cgmesSubject",
            (entry.getNewSubject() != null) ? entry.getNewSubject() : change.getIdentifiableId());
        cgmesChanges.put("cgmesPredicate", entry.getRdfPredicate());
        cgmesChanges.put("cgmesNewValue", entry.getValue());
        cgmesChanges.put("valueIsNode", String.valueOf(entry.valueIsNode()));

        return cgmesChanges;
    }

    public List<IidmChange> changes() {
        return Collections.unmodifiableList(changes);
    }

    private Network network;
    private List<IidmChange> changes;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdate.class);
}
