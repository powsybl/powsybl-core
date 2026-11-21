/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model.statement;

import java.util.Objects;

/**
 * A property of a {@link CgmesObject}: the predicate and the object of one RDF statement.
 *
 * @param predicate the local name of the predicate in the CIM namespace, for example {@code SynchronousMachine.referencePriority}
 * @param object the object as it is written in the file: the lexical form of a literal, or the IRI of a resource
 * @param resource {@code true} if the object is a resource (an enumeration value or a reference), {@code false} if it is a literal
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public record CgmesProperty(String predicate, String object, boolean resource) {

    public CgmesProperty {
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(object);
    }
}
