/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.psse.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.powsybl.psse.model.pf.PssePowerFlowModel;

import java.lang.reflect.Field;
import java.util.Objects;

import static com.powsybl.psse.model.PsseVersion.MAX_VERSION;
import static com.powsybl.psse.model.PsseVersion.fromRevision;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
@JsonFilter("PsseVersionFilter")
public class PsseVersioned {
    private PssePowerFlowModel model = null;

    public void setModel(PssePowerFlowModel model) {
        this.model = Objects.requireNonNull(model);
    }

    @JsonIgnore
    public PssePowerFlowModel getModel() {
        return model;
    }

    public void checkVersion(String fieldName) {
        // If we do not have a reference back to a model
        // We can not obtain current version and we can not perform checks
        if (model == null) {
            return;
        }
        Field field = null;
        try {
            field = this.getClass().getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new PsseException("Wrong field name " + fieldName, e);
        }

        checkVersionField(field, fieldName);
    }

    public static boolean isValidVersion(PsseVersion version, Revision revisionAnnotation) {
        PsseVersion since = fromRevision(revisionAnnotation.since());
        PsseVersion until = fromRevision(revisionAnnotation.until());
        return since.getNumber() <= version.getNumber() && version.getNumber() <= until.getNumber();
    }

    private void checkVersionField(Field field, String fieldName) {
        if (!field.isAnnotationPresent(Revision.class)) {
            throw new PsseException("Missing Revision annotation in field " + fieldName);
        }
        PsseVersion current = fromRevision(model.getCaseIdentification().getRev());
        if (!isValidVersion(current, field.getAnnotation(Revision.class))) {
            PsseVersion since = fromRevision(field.getAnnotation(Revision.class).since());
            PsseVersion until = fromRevision(field.getAnnotation(Revision.class).until());
            String message = String.format(
                "Wrong version of PSSE RAW model (%s). Field '%s' is valid since version %s%s",
                current,
                fieldName,
                since,
                until.getNumber() != MAX_VERSION.getNumber() ? " until " + until : "");
            throw new PsseException(message);
        }
    }

}

