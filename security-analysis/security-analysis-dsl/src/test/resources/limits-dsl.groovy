current_limits {

    N_situation {

        permanent {
            factor 0.95
        }

        temporary {
            factor 1.0
        }
    }

    contingency('contingency1') {
        factor 0.99
    }

    any_contingency {
        factor 0.98
    }
}
