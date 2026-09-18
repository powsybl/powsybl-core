/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.model.triplestore;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.nc.model.NcException;
import com.powsybl.nc.model.NcKeyword;
import com.powsybl.nc.model.NcProfileMetadata;
import com.powsybl.nc.model.NcVersion;
import com.powsybl.nc.model.io.NcConstants;
import com.powsybl.nc.model.io.NcModelReports;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

final class NcProfileSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(NcProfileSelector.class);

    private NcProfileSelector() {
    }

    static Map<String, NcProfileMetadata> readMetadata(Set<String> contexts,
                                                       Map<String, Set<String>> contextsByKeyword,
                                                       NcQueryExecutor queryExecutor,
                                                       ReportNode reportNode) {
        Map<String, NcProfileMetadata> metadata = new LinkedHashMap<>();
        for (String context : contexts) {
            PropertyBags headers = queryExecutor.query(NcConstants.REQUEST_HEADER, Set.of(context));
            PropertyBag header = headers.isEmpty() ? null : headers.getFirst();
            String profileUri = profileUri(header);
            NcVersion version = NcVersion.fromProfileUri(profileUri);
            if (!version.isSupported()) {
                LOGGER.warn("NC profile {} declares unsupported version {}", context, version);
                NcModelReports.unsupportedNcVersion(reportNode, context, version.toString());
                throw new NcException("NC profile " + context + " declares unsupported version " + version);
            }
            metadata.put(context, new NcProfileMetadata(
                context,
                keyword(context, contextsByKeyword),
                profileUri,
                version,
                date(header, NcConstants.REQUEST_HEADER_START_DATE, context),
                date(header, NcConstants.REQUEST_HEADER_END_DATE, context),
                date(header, NcConstants.REQUEST_HEADER_SCENARIO_TIME, context)
            ));
        }
        return Map.copyOf(metadata);
    }

    static Selection baseline(Map<String, NcProfileMetadata> metadata) {
        return select(metadata, null, ReportNode.NO_OP);
    }

    static Selection forTimestamp(Map<String, NcProfileMetadata> metadata, OffsetDateTime timestamp,
                                  ReportNode reportNode) {
        return select(metadata, timestamp, reportNode);
    }

    private static Selection select(Map<String, NcProfileMetadata> metadata, OffsetDateTime timestamp,
                                    ReportNode reportNode) {
        Map<String, Set<String>> contextsByKeyword = new LinkedHashMap<>();
        Map<String, NcProfileMetadata> selectedMetadata = new LinkedHashMap<>();
        metadata.values().forEach(profile -> {
            boolean selected = timestamp == null ? !isOverridingProfile(profile) : isApplicable(profile, timestamp);
            if (selected) {
                selectedMetadata.put(profile.contextName(), profile);
                contextsByKeyword.computeIfAbsent(profile.keyword().toString(), ignored -> new LinkedHashSet<>())
                    .add(profile.contextName());
            } else if (timestamp != null) {
                LOGGER.warn("NC profile {} is not applicable to {} and will be ignored", profile.contextName(), timestamp);
                NcModelReports.ncProfileNotApplicable(reportNode, profile.contextName(), timestamp);
            }
        });
        contextsByKeyword.replaceAll((keyword, contexts) -> Set.copyOf(contexts));
        return new Selection(Map.copyOf(contextsByKeyword), Map.copyOf(selectedMetadata));
    }

    private static boolean isApplicable(NcProfileMetadata metadata, OffsetDateTime timestamp) {
        if (metadata.startDate() != null || metadata.endDate() != null) {
            return metadata.startDate() != null && metadata.endDate() != null
                && !timestamp.isBefore(metadata.startDate()) && !timestamp.isAfter(metadata.endDate());
        }
        return true;
    }

    private static boolean isOverridingProfile(NcProfileMetadata metadata) {
        return metadata.keyword() == NcKeyword.STEADY_STATE_INSTRUCTION;
    }

    private static NcKeyword keyword(String context, Map<String, Set<String>> contextsByKeyword) {
        return contextsByKeyword.entrySet().stream()
            .filter(entry -> entry.getValue().contains(context))
            .map(Map.Entry::getKey)
            .map(NcKeyword::fromString)
            .findFirst()
            .orElseThrow(() -> new NcException("No profile keyword for context " + context));
    }

    private static String profileUri(PropertyBag header) {
        if (header == null) {
            return null;
        }
        String conformsTo = header.get(NcConstants.REQUEST_HEADER_CONFORMS_TO);
        return conformsTo != null ? conformsTo : header.get(NcConstants.REQUEST_HEADER_CGMES_PROFILE);
    }

    private static OffsetDateTime date(PropertyBag header, String field, String context) {
        if (header == null) {
            return null;
        }
        String value = header.get(field);
        if (value == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value);
        } catch (DateTimeParseException e) {
            throw new NcException("Cannot parse " + field + " in NC profile " + context + ": " + value, e);
        }
    }

    record Selection(Map<String, Set<String>> contextsByKeyword,
                     Map<String, NcProfileMetadata> profileMetadata) {
        Set<String> contexts(NcKeyword keyword) {
            return contextsByKeyword.getOrDefault(keyword.toString(), Set.of());
        }
    }
}
