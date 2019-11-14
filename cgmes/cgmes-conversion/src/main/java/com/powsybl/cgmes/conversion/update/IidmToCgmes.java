/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.powsybl.cgmes.model.CgmesSubset;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class IidmToCgmes {

    public List<TripleStoreChange> convert(IidmChange change) {
        if (change instanceof IidmChangeUpdate) {
            return convertUpdate((IidmChangeUpdate) change);
        } else {
            return Collections.emptyList();
        }
    }

    public List<TripleStoreChange> convertUpdate(IidmChangeUpdate change) {
        TripleStoreSimpleUpdateReference simpleUpdateReference = simpleUpdateReference(change);
        if (simpleUpdateReference != null) {
            String subject = change.getIdentifiable().getId();
            String value = change.getNewValue().toString();
            TripleStoreChangeParams updateParams = new TripleStoreChangeParams(simpleUpdateReference, value);
            TripleStoreChange tschange = new TripleStoreChange("update", subject, updateParams);
            return Collections.singletonList(tschange);
        } else {
            return Collections.emptyList();
        }
    }

    protected void addSimpleUpdate(String attribute, String predicate, CgmesSubset subset, boolean valueIsNode) {
        // The reference to the context in which the change must be applied
        // is the identifier of the CGMES subset
        // "EQ", "SSH", "SV", ...
        simpleUpdateReferences.put(attribute,
            new TripleStoreSimpleUpdateReference(predicate, subset.getIdentifier(), valueIsNode));
    }

    private TripleStoreSimpleUpdateReference simpleUpdateReference(IidmChangeUpdate change) {
        return simpleUpdateReferences.get(change.getAttribute());
    }

    private final Map<String, TripleStoreSimpleUpdateReference> simpleUpdateReferences = new HashMap<>();
}
