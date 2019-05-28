/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

import com.powsybl.commons.config.PlatformConfig;

/**
 * @author Chamseddine Benhamed <chamseddine.benhamed at rte-france.com>
 */
public class KafkaConfig {

    private static final String DEFAULT_KAFKA_BROKERS = "localhost:9092";

    private static final String DEFAULT_KAFKA_CLIENT_ID = "client1";

    private String kafkaBrokers;

    private String clientId;

    public static KafkaConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static KafkaConfig load(PlatformConfig platformConfig) {

        return platformConfig.getOptionalModuleConfig("kafka-app-file-system")
                .map(moduleConfig -> {
                    KafkaConfig config = new KafkaConfig("", "");
                    if (moduleConfig.hasProperty("kafka-brokers")) {
                        String kafkaBrokers = moduleConfig.getStringProperty("kafka-brokers");
                        config.setKafkaBrokers(kafkaBrokers);
                    } else {
                        config.setKafkaBrokers(DEFAULT_KAFKA_BROKERS);
                    }
                    if (moduleConfig.hasProperty("client-id")) {
                        String clientId = moduleConfig.getStringProperty("client-id");
                        config.setClientId(clientId);
                    } else {
                        config.setClientId(DEFAULT_KAFKA_CLIENT_ID);
                    }
                    return config;
                })
                .orElse(new KafkaConfig(DEFAULT_KAFKA_BROKERS, DEFAULT_KAFKA_CLIENT_ID));
    }

    public KafkaConfig(String kafkaBrokers, String clientId) {
        this.kafkaBrokers = kafkaBrokers;
        this.clientId = clientId;
    }

    void setKafkaBrokers(String kafkaBrokers) {
        this.kafkaBrokers = kafkaBrokers;
    }

    void setClientId(String clientId) {
        this.clientId = clientId;
    }

    String getKafkaBrokers() {
        return kafkaBrokers;
    }

    String getClientId() {
        return clientId;
    }
}
