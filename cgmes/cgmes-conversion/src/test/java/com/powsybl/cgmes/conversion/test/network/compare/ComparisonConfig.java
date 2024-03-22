/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.network.compare;

import com.powsybl.cgmes.model.CgmesSubset;

import java.util.Collections;
import java.util.Set;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
public final class ComparisonConfig {

    public ComparisonConfig() {
        checkNetworkId = true;
        versionIncremented = false;
        isExportedSubset = Collections.emptySet();
        ignoreMissingMetadata = false;
        differences = new DifferencesFail();
        networkMappingFactory = NetworkMapping::new;
        checkVoltageLevelLimits = true;
        checkGeneratorReactiveCapabilityCurve = true;
        checkGeneratorRegulatingTerminal = true;
        compareNamesAllowSuffixes = false; // Must be identical
        tolerance = 1e-12;
    }

    public ComparisonConfig checkNetworkId(boolean checkNetworkId) {
        this.checkNetworkId = checkNetworkId;
        return this;
    }

    public ComparisonConfig incrementVersions(boolean incremented) {
        this.versionIncremented = incremented;
        return this;
    }

    public ComparisonConfig exportedSubset(Set<CgmesSubset> exportedSubsets) {
        this.isExportedSubset = exportedSubsets;
        return this;
    }

    public ComparisonConfig ignoreMissingMetadata() {
        this.ignoreMissingMetadata = true;
        return this;
    }

    public ComparisonConfig onlyReportDifferences() {
        this.differences = new DifferencesReport();
        return this;
    }

    public ComparisonConfig checkVoltageLevelLimits(boolean checkVoltageLevelLimits) {
        this.checkVoltageLevelLimits = checkVoltageLevelLimits;
        return this;
    }

    public ComparisonConfig checkGeneratorReactiveCapabilityCurve(
            boolean checkGeneratorReactiveCapabilityCurve) {
        this.checkGeneratorReactiveCapabilityCurve = checkGeneratorReactiveCapabilityCurve;
        return this;
    }

    public ComparisonConfig checkGeneratorRegulatingTerminal(
            boolean checkGeneratorRegulatingTerminal) {
        this.checkGeneratorRegulatingTerminal = checkGeneratorRegulatingTerminal;
        return this;
    }

    public ComparisonConfig compareNamesAllowSuffixes(boolean allowSuffixes) {
        this.compareNamesAllowSuffixes = allowSuffixes;
        return this;
    }

    public ComparisonConfig tolerance(double tolerance) {
        this.tolerance = tolerance;
        return this;
    }

    public ComparisonConfig networkMappingFactory(NetworkMappingFactory networkMappingFactory) {
        this.networkMappingFactory = networkMappingFactory;
        return this;
    }

    public boolean isExportedSubset(CgmesSubset subset) {
        return isExportedSubset.contains(subset);
    }

    boolean checkNetworkId;
    boolean versionIncremented;
    Set<CgmesSubset> isExportedSubset;
    boolean ignoreMissingMetadata;
    Differences differences;
    NetworkMappingFactory networkMappingFactory;
    boolean checkVoltageLevelLimits;
    boolean checkGeneratorReactiveCapabilityCurve;
    boolean checkGeneratorRegulatingTerminal;
    boolean compareNamesAllowSuffixes;
    double tolerance;
}
