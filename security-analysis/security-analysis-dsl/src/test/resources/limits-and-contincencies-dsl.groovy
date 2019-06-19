current_limits {

    N_situation {

        permanent {
            factor 0.95
        }

        temporary {
            factor 1.0
        }
    }

    contingency('HV line 1') {
        factor 0.99
    }
}

contingency('HV line 1') {

    equipments 'NHV1_NHV2_1'

}
