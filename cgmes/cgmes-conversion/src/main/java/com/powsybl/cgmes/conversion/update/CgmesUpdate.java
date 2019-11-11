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
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.conversion.Profiling;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 *
 */
public class CgmesUpdate {

    public CgmesUpdate(Network network) {
        this.changes = new ArrayList<>();
        ChangesListener changeListener = new ChangesListener(changes);
        network.addListener(changeListener);
    }

    /**
     * Update. Prepare triples for SPARQL Update statement.
     *
     * @throws Exception the exception
     */
    public void update(CgmesModel cgmes, String variantId, Profiling profiling) throws Exception {

        String cimNamespace = cgmes.getCimNamespace();
        String cimVersion = cimNamespace.substring(cimNamespace.lastIndexOf("cim"));
        Map<String, String> contexts = contexts(cgmes);
        // XXX LUMA refactor mapping between IIDM and CGMES:
        // IidmToCgmes iidmToCgmes = new IidmToCgmes(cimVersion);
        IidmToCgmes iidmToCgmes = new IidmToCgmes(cimVersion);

        int changesSize = changes.size();
        int changesCounter = 0;
        // XXX LUMA dirty debug
        System.err.println("numChanges " + changesSize);
        for (IidmChange change : changes) {
            changesCounter++;
            if (change.getVariant() == null || change.getVariant().equals(variantId)) {
                // XXX LUMA List<CgmesPredicateDetails> allCgmesDetails =
                // iidmToCgmes.convert(change, cgmes);
                List<CgmesPredicateDetails> entries = iidmToCgmes.convert(change, cgmes);
                // XXX LUMA dirty debug
                if (changesCounter % (changesSize / 10) == 0) {
                    System.err.printf("    %3d change %4d %1d%n", (100 * changesCounter) / changesSize, changesCounter,
                        entries.size());
                }
                for (CgmesPredicateDetails entry : entries) {
                    try {
                        profiling.startLoopIteration();
                        String subject = (entry.getNewSubject() != null) ? entry.getNewSubject()
                            : change.getIdentifiableId();
                        String predicate = entry.getRdfPredicate();
                        String newValue = entry.getValue();
                        String valueIsNode = String.valueOf(entry.valueIsNode());
                        PropertyBags result = cgmes.updateCgmes(
                            queryName(change),
                            contexts.get(entry.getContext()),
                            cgmes.getBaseName(),
                            subject,
                            predicate,
                            newValue,
                            valueIsNode);
                        profiling.endLoopIteration();

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

    public List<IidmChange> changes() {
        return Collections.unmodifiableList(changes);
    }

    private List<IidmChange> changes;

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdate.class);
}
