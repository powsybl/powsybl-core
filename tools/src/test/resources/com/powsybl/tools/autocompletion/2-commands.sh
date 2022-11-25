_cmd1() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
        *)
            COMPREPLY=($(compgen -W "" -- $cur))
            return 0
            ;;
    esac
}

_cmd2() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
        *)
            COMPREPLY=($(compgen -W "" -- $cur))
            return 0
            ;;
    esac
}

_itools() {
    compopt -o filenames

    if [[ "${#COMP_WORDS[@]}" == 2 ]]; then
        local cur=${COMP_WORDS[COMP_CWORD]}
        COMPREPLY=($(compgen -W "cmd1 cmd2 " -- $cur))
    else
        local cmd=${COMP_WORDS[1]}
        case "$cmd" in
            cmd1)
                _cmd1
                return 0
                ;;
            cmd2)
                _cmd2
                return 0
                ;;
        esac
    fi
}

complete -F _itools itools
