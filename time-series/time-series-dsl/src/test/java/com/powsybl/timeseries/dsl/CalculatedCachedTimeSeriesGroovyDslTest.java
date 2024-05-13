/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.timeseries.dsl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.timeseries.*;
import com.powsybl.timeseries.ast.NodeCalc;
import com.powsybl.timeseries.ast.NodeCalcVisitors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.extra.Interval;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class CalculatedCachedTimeSeriesGroovyDslTest {

    static ReadOnlyTimeSeriesStore store;
    static TimeSeriesNameResolver resolver;

    static String[] timeSeriesNames = {"foo", "bar", "baz", "toCache"};
    static double[] fooValues = new double[] {3d, 5d};
    static TimeSeriesIndex index = RegularTimeSeriesIndex.create(Interval.parse("2015-01-01T00:00:00Z/2015-07-20T00:00:00Z"), Duration.ofDays(200));
    static CalculatedTimeSeries tsCalc;
    static CalculatedTimeSeries tsCalcIterative;

    @BeforeAll
    static void setUp() {
        // Script to evaluate
        String script = "ts['toCache'] = (timeSeries['foo'] < 4.0)" +
            "+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD + 1)) +
            "\n" +
            "ts['test'] = (timeSeries['toCache'] + timeSeries['toCache'] * 2.0)" +
            "\n" +
            "ts['testIterative'] = ts['test']" +
            "+0".repeat(Math.max(0, NodeCalcVisitors.RECURSION_THRESHOLD + 1));

        // Time Series Store
        store = new ReadOnlyTimeSeriesStoreCache(
            TimeSeries.createDouble(timeSeriesNames[0], index, fooValues)
        );

        // TimeSeries name resolver
        resolver = new FromStoreTimeSeriesNameResolver(store, 0);

        // Nodes
        Map<String, NodeCalc> nodes = CalculatedTimeSeriesDslLoader.find().load(script, store);

        // NodeCalc
        NodeCalc testNodeCalc = nodes.get("test");
        NodeCalc testIterativeNodeCalc = nodes.get("testIterative");

        // Calculated TimeSeries creation
        tsCalc = new CalculatedTimeSeries("test_calc", testNodeCalc, resolver);
        tsCalcIterative = new CalculatedTimeSeries("test_calc_iterative", testIterativeNodeCalc, resolver);
    }

    @Test
    void evalCached() {
        assertArrayEquals(new double[] {3.0, 0.0}, tsCalc.toArray());
        assertArrayEquals(new double[] {3.0, 0.0}, tsCalcIterative.toArray());
    }

    @Test
    void jsonTests() throws JsonProcessingException {
        String json = TimeSeries.toJson(List.of(tsCalc));
        String jsonRef = """
            [ {
              "name" : "test_calc",
              "expr" : {
                "binaryOp" : {
                  "op" : "PLUS",
                  "binaryOp" : {
                    "op" : "PLUS",
                    "binaryOp" : {
                      "op" : "PLUS",
                      "binaryOp" : {
                        "op" : "PLUS",
                        "binaryOp" : {
                          "op" : "PLUS",
                          "binaryOp" : {
                            "op" : "PLUS",
                            "binaryOp" : {
                              "op" : "PLUS",
                              "binaryOp" : {
                                "op" : "PLUS",
                                "binaryOp" : {
                                  "op" : "PLUS",
                                  "binaryOp" : {
                                    "op" : "PLUS",
                                    "binaryOp" : {
                                      "op" : "PLUS",
                                      "binaryOp" : {
                                        "op" : "PLUS",
                                        "binaryOp" : {
                                          "op" : "LESS_THAN",
                                          "timeSeriesName" : "foo",
                                          "bigDecimal" : 4.0
                                        },
                                        "integer" : 0
                                      },
                                      "integer" : 0
                                    },
                                    "integer" : 0
                                  },
                                  "integer" : 0
                                },
                                "integer" : 0
                              },
                              "integer" : 0
                            },
                            "integer" : 0
                          },
                          "integer" : 0
                        },
                        "integer" : 0
                      },
                      "integer" : 0
                    },
                    "integer" : 0
                  },
                  "binaryOp" : {
                    "op" : "MULTIPLY",
                    "binaryOp" : {
                      "op" : "PLUS",
                      "binaryOp" : {
                        "op" : "PLUS",
                        "binaryOp" : {
                          "op" : "PLUS",
                          "binaryOp" : {
                            "op" : "PLUS",
                            "binaryOp" : {
                              "op" : "PLUS",
                              "binaryOp" : {
                                "op" : "PLUS",
                                "binaryOp" : {
                                  "op" : "PLUS",
                                  "binaryOp" : {
                                    "op" : "PLUS",
                                    "binaryOp" : {
                                      "op" : "PLUS",
                                      "binaryOp" : {
                                        "op" : "PLUS",
                                        "binaryOp" : {
                                          "op" : "PLUS",
                                          "binaryOp" : {
                                            "op" : "LESS_THAN",
                                            "timeSeriesName" : "foo",
                                            "bigDecimal" : 4.0
                                          },
                                          "integer" : 0
                                        },
                                        "integer" : 0
                                      },
                                      "integer" : 0
                                    },
                                    "integer" : 0
                                  },
                                  "integer" : 0
                                },
                                "integer" : 0
                              },
                              "integer" : 0
                            },
                            "integer" : 0
                          },
                          "integer" : 0
                        },
                        "integer" : 0
                      },
                      "integer" : 0
                    },
                    "bigDecimal" : 2.0
                  }
                }
              }
            } ]""";
        // Serialisation
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(mapper.readTree(jsonRef), mapper.readTree(json));
    }
}
