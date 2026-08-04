/**
 * Copyright (c) 2026, Elia Group (https://www.eliagroup.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.export.NetworkEventRecorderSshExport;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkEventRecorder;
import com.powsybl.iidm.network.events.NetworkEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;

/**
 * Loads the same base model twice, applies changes to one of the two copies, exports them as a partial SSH file
 * and applies that file to the other copy. This is the exchange the partial SSH export is meant to support, so
 * every supported change is verified this way.
 *
 * @author Nico Westerbeck {@literal <nico.westerbeck at 50hertz.com>}
 */
final class NetworkEventRecorderSshRoundTripTestSupport {

    static final String USE_PREVIOUS_VALUES = "iidm.import.cgmes.use-previous-values-during-update";

    record RoundTripResult(Network sender, Network receiver, List<NetworkEvent> events, String sshXml) {
    }

    private NetworkEventRecorderSshRoundTripTestSupport() {
    }

    static RoundTripResult roundTrip(Path tmpDir, String dir, Consumer<Network> senderChanges, String... baselineFiles) throws IOException {
        return roundTrip(tmpDir, new Properties(), dir, senderChanges, baselineFiles);
    }

    static RoundTripResult roundTrip(Path tmpDir, Properties importParameters, String dir,
                                     Consumer<Network> senderChanges, String... baselineFiles) throws IOException {
        return roundTrip(tmpDir, importParameters,
                () -> readCgmesResources(importParameters, dir, baselineFiles), senderChanges);
    }

    /** Round trip on a whole grid model, for instance one of the CGMES conformity models. */
    static RoundTripResult roundTrip(Path tmpDir, ReadOnlyDataSource dataSource, Consumer<Network> senderChanges) throws IOException {
        return roundTrip(tmpDir, new Properties(), () -> Network.read(dataSource, new Properties()), senderChanges);
    }

    private static RoundTripResult roundTrip(Path tmpDir, Properties importParameters,
                                             Supplier<Network> load, Consumer<Network> senderChanges) throws IOException {
        Network sender = load.get();
        Network receiver = load.get();

        NetworkEventRecorder recorder = new NetworkEventRecorder();
        sender.addListener(recorder);
        senderChanges.accept(sender);

        String baseName = "network-event-recorder-partial-roundtrip";
        Path exportDir = tmpDir.toAbsolutePath();
        Path sshFile = exportDir.resolve(baseName + "_SSH.xml");
        NetworkEventRecorderSshExport.write(sender, recorder.getEvents(), sshFile);

        Properties updateParameters = new Properties();
        updateParameters.putAll(importParameters);
        updateParameters.put(USE_PREVIOUS_VALUES, "true");
        receiver.update(new GenericReadOnlyDataSource(exportDir, baseName), updateParameters);

        return new RoundTripResult(sender, receiver, List.copyOf(recorder.getEvents()), Files.readString(sshFile));
    }
}
