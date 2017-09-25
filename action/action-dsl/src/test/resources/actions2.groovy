rule ('Memoriser_Prise_Init_TD_Boutre') {
    when !contingencyOccurred()
    life 1
    apply 'someAction'
}

action ('someAction') {
    description 'asdf'
    tasks {
        script {
            transformer('NGEN_NHV1').r = 3
            closeSwitch('switchId')
        }
    }
}

action ('anotherAction') {
    tasks {
        closeSwitch('switchId')
    }
}
