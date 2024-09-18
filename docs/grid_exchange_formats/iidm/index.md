# IIDM exchange formats

```{toctree}
:hidden:
import.md
export.md
```

The [IIDM (**i**Tesla **I**nternal **D**ata **M**odel)](../../grid_model/index.md) format was designed during the [iTesla project](http://www.itesla-project.eu).
IIDM is the internal format used in Powsybl because it is designed for running simulations.

Several exchange formats result from this internal format:
- XIIDM, which corresponds to an XML export of IIDM,
- JIIDM, which corresponds to a JSON export of IIDM,
- BIIDM, which corresponds to a binary export (this is still a beta-feature).

Below are two exports from the same network:
- one XML export (XIIDM exchange format)
- one JSON export (JIIDM exchange format)

## XIIDM
```xml
<?xml version="1.0" encoding="UTF-8"?>
<iidm:network xmlns:iidm="http://www.powsybl.org/schema/iidm/1_12" id="sim1" caseDate="2013-01-15T18:45:00.000+01:00" forecastDistance="0" sourceFormat="test" minimumValidationLevel="STEADY_STATE_HYPOTHESIS">
    <iidm:substation id="P1" country="FR" tso="RTE" geographicalTags="A">
        <iidm:voltageLevel id="VLGEN" nominalV="24.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="NGEN"/>
            </iidm:busBreakerTopology>
            <iidm:generator id="GEN" energySource="OTHER" minP="-9999.99" maxP="9999.99" voltageRegulatorOn="true" targetP="607.0" targetV="24.5" targetQ="301.0" bus="NGEN" connectableBus="NGEN">
                <iidm:minMaxReactiveLimits minQ="-9999.99" maxQ="9999.99"/>
            </iidm:generator>
        </iidm:voltageLevel>
        <iidm:voltageLevel id="VLHV1" nominalV="380.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="NHV1"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
        <iidm:twoWindingsTransformer id="NGEN_NHV1" r="0.26658461538461536" x="11.104492831516762" g="0.0" b="0.0" ratedU1="24.0" ratedU2="400.0" voltageLevelId1="VLGEN" bus1="NGEN" connectableBus1="NGEN" voltageLevelId2="VLHV1" bus2="NHV1" connectableBus2="NHV1"/>
    </iidm:substation>
    <iidm:substation id="P2" country="FR" tso="RTE" geographicalTags="B">
        <iidm:voltageLevel id="VLHV2" nominalV="380.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="NHV2"/>
            </iidm:busBreakerTopology>
        </iidm:voltageLevel>
        <iidm:voltageLevel id="VLLOAD" nominalV="150.0" topologyKind="BUS_BREAKER">
            <iidm:busBreakerTopology>
                <iidm:bus id="NLOAD"/>
            </iidm:busBreakerTopology>
            <iidm:load id="LOAD" loadType="UNDEFINED" p0="600.0" q0="200.0" bus="NLOAD" connectableBus="NLOAD"/>
        </iidm:voltageLevel>
        <iidm:twoWindingsTransformer id="NHV2_NLOAD" r="0.04724999999999999" x="4.049724365620455" g="0.0" b="0.0" ratedU1="400.0" ratedU2="158.0" voltageLevelId1="VLHV2" bus1="NHV2" connectableBus1="NHV2" voltageLevelId2="VLLOAD" bus2="NLOAD" connectableBus2="NLOAD">
            <iidm:ratioTapChanger regulating="true" lowTapPosition="0" tapPosition="1" targetDeadband="0.0" loadTapChangingCapabilities="true" regulationMode="VOLTAGE" regulationValue="158.0">
                <iidm:terminalRef id="NHV2_NLOAD" side="TWO"/>
                <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="0.8505666905244191"/>
                <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="1.0006666666666666"/>
                <iidm:step r="0.0" x="0.0" g="0.0" b="0.0" rho="1.150766642808914"/>
            </iidm:ratioTapChanger>
        </iidm:twoWindingsTransformer>
    </iidm:substation>
    <iidm:line id="NHV1_NHV2_1" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" voltageLevelId1="VLHV1" bus1="NHV1" connectableBus1="NHV1" voltageLevelId2="VLHV2" bus2="NHV2" connectableBus2="NHV2"/>
    <iidm:line id="NHV1_NHV2_2" r="3.0" x="33.0" g1="0.0" b1="1.93E-4" g2="0.0" b2="1.93E-4" voltageLevelId1="VLHV1" bus1="NHV1" connectableBus1="NHV1" voltageLevelId2="VLHV2" bus2="NHV2" connectableBus2="NHV2"/>
</iidm:network>
```

## JIIDM
```json
{
  "version" : "1.12",
  "id" : "sim1",
  "caseDate" : "2013-01-15T18:45:00.000+01:00",
  "forecastDistance" : 0,
  "sourceFormat" : "test",
  "minimumValidationLevel" : "STEADY_STATE_HYPOTHESIS",
  "substations" : [ {
    "id" : "P1",
    "country" : "FR",
    "tso" : "RTE",
    "geographicalTags" : [ "A" ],
    "voltageLevels" : [ {
      "id" : "VLGEN",
      "nominalV" : 24.0,
      "topologyKind" : "BUS_BREAKER",
      "busBreakerTopology" : {
        "buses" : [ {
          "id" : "NGEN"
        } ]
      },
      "generators" : [ {
        "id" : "GEN",
        "energySource" : "OTHER",
        "minP" : -9999.99,
        "maxP" : 9999.99,
        "voltageRegulatorOn" : true,
        "targetP" : 607.0,
        "targetV" : 24.5,
        "targetQ" : 301.0,
        "bus" : "NGEN",
        "connectableBus" : "NGEN",
        "minMaxReactiveLimits" : {
          "minQ" : -9999.99,
          "maxQ" : 9999.99
        }
      } ]
    }, {
      "id" : "VLHV1",
      "nominalV" : 380.0,
      "topologyKind" : "BUS_BREAKER",
      "busBreakerTopology" : {
        "buses" : [ {
          "id" : "NHV1"
        } ]
      }
    } ],
    "twoWindingsTransformers" : [ {
      "id" : "NGEN_NHV1",
      "r" : 0.26658461538461536,
      "x" : 11.104492831516762,
      "g" : 0.0,
      "b" : 0.0,
      "ratedU1" : 24.0,
      "ratedU2" : 400.0,
      "voltageLevelId1" : "VLGEN",
      "bus1" : "NGEN",
      "connectableBus1" : "NGEN",
      "voltageLevelId2" : "VLHV1",
      "bus2" : "NHV1",
      "connectableBus2" : "NHV1"
    } ]
  }, {
    "id" : "P2",
    "country" : "FR",
    "tso" : "RTE",
    "geographicalTags" : [ "B" ],
    "voltageLevels" : [ {
      "id" : "VLHV2",
      "nominalV" : 380.0,
      "topologyKind" : "BUS_BREAKER",
      "busBreakerTopology" : {
        "buses" : [ {
          "id" : "NHV2"
        } ]
      }
    }, {
      "id" : "VLLOAD",
      "nominalV" : 150.0,
      "topologyKind" : "BUS_BREAKER",
      "busBreakerTopology" : {
        "buses" : [ {
          "id" : "NLOAD"
        } ]
      },
      "loads" : [ {
        "id" : "LOAD",
        "loadType" : "UNDEFINED",
        "p0" : 600.0,
        "q0" : 200.0,
        "bus" : "NLOAD",
        "connectableBus" : "NLOAD"
      } ]
    } ],
    "twoWindingsTransformers" : [ {
      "id" : "NHV2_NLOAD",
      "r" : 0.04724999999999999,
      "x" : 4.049724365620455,
      "g" : 0.0,
      "b" : 0.0,
      "ratedU1" : 400.0,
      "ratedU2" : 158.0,
      "voltageLevelId1" : "VLHV2",
      "bus1" : "NHV2",
      "connectableBus1" : "NHV2",
      "voltageLevelId2" : "VLLOAD",
      "bus2" : "NLOAD",
      "connectableBus2" : "NLOAD",
      "ratioTapChanger" : {
        "regulating" : true,
        "lowTapPosition" : 0,
        "tapPosition" : 1,
        "targetDeadband" : 0.0,
        "loadTapChangingCapabilities" : true,
        "regulationMode" : "VOLTAGE",
        "regulationValue" : 158.0,
        "terminalRef" : {
          "id" : "NHV2_NLOAD",
          "side" : "TWO"
        },
        "steps" : [ {
          "r" : 0.0,
          "x" : 0.0,
          "g" : 0.0,
          "b" : 0.0,
          "rho" : 0.8505666905244191
        }, {
          "r" : 0.0,
          "x" : 0.0,
          "g" : 0.0,
          "b" : 0.0,
          "rho" : 1.0006666666666666
        }, {
          "r" : 0.0,
          "x" : 0.0,
          "g" : 0.0,
          "b" : 0.0,
          "rho" : 1.150766642808914
        } ]
      }
    } ]
  } ],
  "lines" : [ {
    "id" : "NHV1_NHV2_1",
    "r" : 3.0,
    "x" : 33.0,
    "g1" : 0.0,
    "b1" : 1.93E-4,
    "g2" : 0.0,
    "b2" : 1.93E-4,
    "voltageLevelId1" : "VLHV1",
    "bus1" : "NHV1",
    "connectableBus1" : "NHV1",
    "voltageLevelId2" : "VLHV2",
    "bus2" : "NHV2",
    "connectableBus2" : "NHV2"
  }, {
    "id" : "NHV1_NHV2_2",
    "r" : 3.0,
    "x" : 33.0,
    "g1" : 0.0,
    "b1" : 1.93E-4,
    "g2" : 0.0,
    "b2" : 1.93E-4,
    "voltageLevelId1" : "VLHV1",
    "bus1" : "NHV1",
    "connectableBus1" : "NHV1",
    "voltageLevelId2" : "VLHV2",
    "bus2" : "NHV2",
    "connectableBus2" : "NHV2"
  } ]
}
```
