current_limits {


    branch('NHV1_NHV2_1') {
        contingencies(['contingency1', 'contingency2']) {
            factor 0.95
        }
    }

    branches(['NGEN_NHV1', 'NGEN_LOAD']) {
        contingency('contingency1') {
            factor 0.8
        }
    }

}
