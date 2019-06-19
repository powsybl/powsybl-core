current_limits {


    permanent {
        where(voltage >= 300) {
            factor 0.9
        }

        where(voltage >= 10) {
            factor 0.8
        }
    }

    temporary {
        where(duration >= 1000) {
            factor 0.7
        }

        where(duration >= 500) {
            factor 0.6
        }
    }

    factor 0.5
}
