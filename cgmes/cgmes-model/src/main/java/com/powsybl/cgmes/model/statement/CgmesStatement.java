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
 * One RDF statement (subject, predicate, object) of a CGMES profile.
 *
 * @param subject the rdf:ID of the CGMES object
 * @param predicate {@link #TYPE} for the class of the object, otherwise the local name of the predicate in the CIM namespace
 * @param object the value as it is written in the file: the class name for {@link #TYPE}, the lexical form of a literal,
 *               or the IRI of a resource
 * @param resource {@code true} if the object is a resource (the class, an enumeration value or a reference),
 *                 {@code false} if it is a literal
 *
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public record CgmesStatement(String subject, String predicate, String object, boolean resource) {

    /**
     * The predicate of the statement that gives the class of the object.
     */
    public static final String TYPE = "rdf:type";

    public CgmesStatement {
        Objects.requireNonNull(subject);
        Objects.requireNonNull(predicate);
        Objects.requireNonNull(object);
    }
}
