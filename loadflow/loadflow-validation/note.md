https://powsybl.readthedocs.io/projects/powsybl-core/en/stable/user/itools/loadflow-validation.html

## Flows (BranchData) can be constructed from
    - Line Flows
    - TwoWindingsTransformer Flows
         - Used Extensions
            - TwoWindingsTransformerPhaseAngleClock
                - phaseAngleClock
    - TieLine Flows

## ThreeWindingsTransformer
    - Used Extensions
        - ThreeWindingsTransformerPhaseAngleClock
            - phaseAngleClock2
            - phaseAngleClock3

```sh
./distribution-core/target/powsybl/bin/itools loadflow-validation \
  --case-file ./distribution-core/test/network.xiidm \
  --output-folder /tmp/lv-out \
  --load-flow \
  --verbose
```