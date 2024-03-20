/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.serde;

import com.google.common.collect.Sets;
import com.powsybl.iidm.network.ValidationLevel;

import java.util.Optional;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class ImportOptions extends AbstractOptions<ImportOptions> {

    private boolean throwExceptionIfExtensionNotFound = false;
    private boolean withAutomationSystems = true;
    private double missingPermanentLimitPercentage = 100.;

    private ValidationLevel minimalValidationLevel = null;

    public ImportOptions() {
    }

    public ImportOptions(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
    }

    @Override
    public ImportOptions setExtensions(Set<String> extensions) {
        this.extensions = extensions;
        return this;
    }

    public ImportOptions setThrowExceptionIfExtensionNotFound(boolean throwExceptionIfExtensionNotFound) {
        this.throwExceptionIfExtensionNotFound = throwExceptionIfExtensionNotFound;
        return this;
    }

    @Override
    public ImportOptions addExtension(String extension) {
        if (extensions != null) {
            extensions.add(extension);
        } else {
            this.extensions = Sets.newHashSet(extension);
        }
        return this;
    }

    @Override
    public boolean isThrowExceptionIfExtensionNotFound() {
        return throwExceptionIfExtensionNotFound;
    }

    public boolean isWithAutomationSystems() {
        return withAutomationSystems;
    }

    public ImportOptions setWithAutomationSystems(boolean withAutomationSystems) {
        this.withAutomationSystems = withAutomationSystems;
        return this;
    }

    public ImportOptions setMissingPermanentLimitPercentage(double missingPermanentLimitPercentage) {
        if (missingPermanentLimitPercentage < 0 || missingPermanentLimitPercentage > 100) {
            throw new IllegalArgumentException("Missing permanent limit percentage must be between 0 and 100.");
        }
        this.missingPermanentLimitPercentage = missingPermanentLimitPercentage;
        return this;
    }

    /**
     * <p>Percentage to use to compute a missing permanent limit from the temporary limits.</p>
     * <p>IMPORTANT: This parameter is only effective when importing networks in IIDM version < 1.12
     * (the permanent limit is mandatory since IIDM v1.12).</p>
     * @return the percentage to use to compute the value for missing permanent limits
     */
    public double getMissingPermanentLimitPercentage() {
        return missingPermanentLimitPercentage;
    }

    public ImportOptions setMinimalValidationLevel(String minimalValidationLevel) {
        if (minimalValidationLevel != null) {
            // no check?
            this.minimalValidationLevel = ValidationLevel.valueOf(minimalValidationLevel);
        }
        return this;
    }

    /**
     * <p>Minimal validation level accepted during import.</p>
     * <p>If null, we first look at the one defined in the file. If not defined, the default value is SSH.</p>
     * @return the validation level if defined.
     */
    public Optional<ValidationLevel> getMinimalValidationLevel() {
        return Optional.ofNullable(minimalValidationLevel);
    }
}
