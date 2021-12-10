/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import com.powsybl.cgmes.conversion.ConversionException;
import com.powsybl.cgmes.conversion.update.elements16.IidmToCgmes16;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class CgmesUpdate {

    public CgmesUpdate(Network network) {
        this.changelog = new Changelog(network);
    }

    public Changelog changelog() {
        return changelog;
    }

    /**
     * Update the CGMES model given as a parameter. This update only supports CGMES
     * models implemented on a Triplestore.
     *
     * @param cgmes     CGMES model to be updated
     * @param variantId Update CGMES model with changes made in this variant
     */
    public void update(CgmesModel cgmes, String variantId) {
        requireCgmesModelTripleStore(cgmes);
        CgmesModelTripleStore cgmests = (CgmesModelTripleStore) cgmes;
        List<IidmChange> changes = changelog.getChangesForVariant(variantId);
        if (changes.isEmpty()) {
            return;
        }
        UpdateContext context = new UpdateContext(cgmests);
        for (IidmChange change : changes) {
            List<TripleStoreChange> tsChanges = convert(change, context);
            update(cgmests, tsChanges, context);
        }
    }

    private void requireCgmesModelTripleStore(CgmesModel cgmes) {
        if (!(cgmes instanceof CgmesModelTripleStore)) {
            throw new ConversionException("Unsupported conversion to CGMES model implementation " + cgmes.getClass().getSimpleName());
        }
    }

    private List<TripleStoreChange> convert(IidmChange change, UpdateContext context) {
        // Right now we only know how to deal with update changes
        requireChangeIsUpdate(change);
        IidmToCgmes c = findConversion(change, context);
        if (c == null) {
            LOG.error("Unsupported conversion for IIDM change {}", change);
            return Collections.emptyList();
        }
        return c.convert(change);
    }

    private void requireChangeIsUpdate(IidmChange change) {
        if (!(change instanceof IidmChangeUpdate)) {
            throw new ConversionException("Unsupported conversion to CGMES for IIDM change type " + change.getClass().getSimpleName());
        }
    }

    private IidmToCgmes findConversion(IidmChange change, UpdateContext context) {
        if (context.cimVersion == 16) {
            return IIDM_TO_CGMES16.findConversion(change);
        }
        throw new ConversionException("Unsupported format for conversion to CGMES model " + context.cimVersion);
    }

    private void update(CgmesModelTripleStore ts, List<TripleStoreChange> tsChanges, UpdateContext context) {
        for (TripleStoreChange tsChange : tsChanges) {
            String predicate = tsChange.params().predicate();
            String newValue = tsChange.params().value();
            boolean valueIsUri = tsChange.params().valueIsUri();
            ts.update(
                tsChange.queryName(),
                context.actualContext(tsChange.params().contextReference()),
                context.basename,
                tsChange.subject(),
                predicate,
                newValue,
                valueIsUri);
        }
    }

    private static class UpdateContext {
        UpdateContext(CgmesModelTripleStore cgmes) {
            this.basename = cgmes.getBasename();
            computeActualContexts(cgmes.tripleStore());
            this.cimVersion = cgmes.getCimVersion();
        }

        String actualContext(String context) {
            return actualContexts.get(context);
        }

        private Map<String, String> computeActualContexts(TripleStore ts) {
            for (String context : ts.contextNames()) {
                for (CgmesSubset subset : CgmesSubset.values()) {
                    if (subset.isValidName(context)) {
                        actualContexts.put(subset.getIdentifier(), context);
                    }
                }
            }
            return actualContexts;
        }

        private final String basename;
        private final Map<String, String> actualContexts = new HashMap<>();
        private final int cimVersion;
    }

    private final Changelog changelog;

    private static final IidmToCgmes16 IIDM_TO_CGMES16 = new IidmToCgmes16();

    private static final Logger LOG = LoggerFactory.getLogger(CgmesUpdate.class);
}
