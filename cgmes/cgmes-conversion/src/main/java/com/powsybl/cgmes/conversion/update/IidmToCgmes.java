/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class IidmToCgmes {

    public List<TripleStoreChange> convert(IidmChange change) {
        if (change instanceof IidmChangeUpdate) {
            return convertUpdate((IidmChangeUpdate) change);
        } else {
            LOG.warn("Changes of type {} are not yet supported", change.getClass().getSimpleName());
            return Collections.emptyList();
        }
    }

    public List<TripleStoreChange> convertUpdate(IidmChangeUpdate change) {
        TripleStoreSimpleUpdateReference simpleUpdateReference = simpleUpdateReference(change);
        if (simpleUpdateReference != null) {
            String subject = change.getIdentifiable().getId();
            String value = simpleUpdateReference.value(change);
            TripleStoreChangeParams updateParams = new TripleStoreChangeParams(simpleUpdateReference, value);
            TripleStoreChange tschange = new TripleStoreChange("update", subject, updateParams);
            return Collections.singletonList(tschange);
        } else if (ignoredAttributes.contains(change.getAttribute())) {
            return Collections.emptyList();
        } else {
            LOG.warn("Convert to CGMES a change on IIDM {}.{}", change.getIdentifiable().getClass().getSimpleName(), change.getAttribute());
            return Collections.emptyList();
        }
    }

    public boolean isSupported(String attribute) {
        return !unsupportedAttributes.contains(attribute);
    }

    protected void simpleUpdate(String attribute, String predicate, CgmesSubset subset) {

        // The reference to the context in which the change must be applied
        // is the identifier of the CGMES subset
        // "EQ", "SSH", "SV", ...
        simpleUpdateReferences.put(attribute,
            new TripleStoreSimpleUpdateReference(predicate, subset.getIdentifier()));
    }

    protected void computedValueUpdate(String attribute, String predicate, CgmesSubset subset, Function<Identifiable, String> valueComputation) {
        simpleUpdateReferences.put(attribute,
            new TripleStoreComputedValueUpdateReference(predicate, subset.getIdentifier(), valueComputation));
    }

    protected void ignore(String attribute) {
        ignoredAttributes.add(attribute);
    }

    protected void unsupported(String attribute) {
        unsupportedAttributes.add(attribute);
    }

    protected void unsupported(String type, String attribute, String predicate, CgmesSubset subset) {
        LOG.warn("Attribute change not yet supported {}.{} to {} in {}", type, attribute, predicate, subset);
        unsupportedAttributes.add(attribute);
    }

    private TripleStoreSimpleUpdateReference simpleUpdateReference(IidmChangeUpdate change) {
        return simpleUpdateReferences.get(change.getAttribute());
    }

    private final Map<String, TripleStoreSimpleUpdateReference> simpleUpdateReferences = new HashMap<>();
    private final Set<String> ignoredAttributes = new HashSet<>();
    private final Set<String> unsupportedAttributes = new HashSet<>();

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
