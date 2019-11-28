/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.powsybl.cgmes.model.CgmesSubset;
import com.powsybl.cgmes.model.triplestore.CgmesModelTripleStore;
import com.powsybl.iidm.network.Identifiable;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class IidmToCgmes {

    public List<TripleStoreChange> convert(IidmChange change, CgmesModelTripleStore cgmests) {
        if (change instanceof IidmChangeUpdate) {
            return convertUpdate((IidmChangeUpdate) change, cgmests);
        } else {
            throw new UnsupportedOperationException(
                String.format("Changes of type %s are not yet supported", change.getClass().getSimpleName()));
        }
    }

    public List<TripleStoreChange> convertUpdate(IidmChangeUpdate change, CgmesModelTripleStore cgmests) {
        List<TripleStoreChange> tschanges = new ArrayList<TripleStoreChange>();
        for (TripleStoreSimpleUpdateReference simpleUpdateReference : simpleUpdateReferences(change)) {
            if (simpleUpdateReference != null) {
                String subject = simpleUpdateReference.subject(change, cgmests);
                String value = simpleUpdateReference.value(change, cgmests);
                TripleStoreChangeParams updateParams = new TripleStoreChangeParams(simpleUpdateReference, value);
                TripleStoreChange tschange = new TripleStoreChange("update", subject, updateParams);
                tschanges.add(tschange);
            } else if (ignoredAttributes.contains(change.getAttribute())) {
                LOG.info(String.format("Changes of type %s are not yet supported", change.getAttribute()));
                continue;
            } else {
                throw new UnsupportedOperationException("Convert to CGMES a change on IIDM "
                    + change.getIdentifiable().getClass().getSimpleName() + "." + change.getAttribute());
            }
        }
        return tschanges;
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

    protected void computedValueUpdate(String attribute, String predicate, CgmesSubset subset,
        Function<Identifiable, String> valueComputation) {
        simpleUpdateReferences.put(attribute,
            new TripleStoreComputedValueUpdateReference(predicate, subset.getIdentifier(), valueComputation));
    }

    protected void computedSubjectUpdate(String attribute, String predicate, CgmesSubset subset,
        BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation) {
        simpleUpdateReferences.put(attribute,
            new TripleStoreSimpleUpdateReference(predicate, subset.getIdentifier(),
                subjectComputation));
    }

    protected void computedValueAndSubjectUpdate(String attribute, String predicate, CgmesSubset subset,
        Function<Identifiable, String> valueComputation,
        BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation) {
        simpleUpdateReferences.put(attribute,
            new TripleStoreComputedValueUpdateReference(predicate, subset.getIdentifier(), valueComputation,
                subjectComputation));
    }

    protected void computedValueAndSubjectUpdate(String attribute, String predicate, CgmesSubset subset,
        BiFunction<Identifiable, CgmesModelTripleStore, String> complexValueComputation,
        BiFunction<Identifiable, CgmesModelTripleStore, String> subjectComputation) {
        simpleUpdateReferences.put(attribute,
            new TripleStoreComputedValueUpdateReference(predicate, subset.getIdentifier(), complexValueComputation,
                subjectComputation));
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

    private List<TripleStoreSimpleUpdateReference> simpleUpdateReferences(IidmChangeUpdate change) {
        List<TripleStoreSimpleUpdateReference> list = new ArrayList<>();
        list.addAll(simpleUpdateReferences.get(change.getAttribute()));
        return list;
    }

    private final Multimap<String, TripleStoreSimpleUpdateReference> simpleUpdateReferences = ArrayListMultimap.create();
    private final Set<String> ignoredAttributes = new HashSet<>();
    private final Set<String> unsupportedAttributes = new HashSet<>();

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes.class);
}
