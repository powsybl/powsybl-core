_cmd() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
        --case-file)
            COMPREPLY=($(compgen -f -- $cur))
            return 0
            ;;
        --output-dir)
            COMPREPLY=($(compgen -d -- $cur))
            return 0
            ;;
        --host)
            COMPREPLY=($(compgen -A hostname -- $cur))
            return 0
            ;;
        --type)
            COMPREPLY=($(compgen -W "TYPE1 TYPE2 " -- $cur))
            return 0
            ;;
        *)
            COMPREPLY=($(compgen -W "--case-file --output-dir --host --type " -- $cur))
            return 0
            ;;
    esac
}

_itools() {
    compopt -o filenames

    if [[ "${#COMP_WORDS[@]}" == 2 ]]; then
        local cur=${COMP_WORDS[COMP_CWORD]}
        COMPREPLY=($(compgen -W "cmd " -- $cur))
    else
        local cmd=${COMP_WORDS[1]}
        case "$cmd" in
            cmd)
                _cmd
                return 0
                ;;
        esac
    fi
}

complete -F _itools itools
