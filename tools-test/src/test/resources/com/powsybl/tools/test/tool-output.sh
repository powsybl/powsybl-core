_tool1() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
        --case-file)
            COMPREPLY=($(compgen -f -- $cur))
            return 0
            ;;
        *)
            COMPREPLY=($(compgen -W "-I --case-file " -- $cur))
            return 0
            ;;
    esac
}

_tool2() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
        --hostname)
            COMPREPLY=($(compgen -A hostname -- $cur))
            return 0
            ;;
        --dir)
            COMPREPLY=($(compgen -d -- $cur))
            return 0
            ;;
        *)
            COMPREPLY=($(compgen -W "--hostname --dir " -- $cur))
            return 0
            ;;
    esac
}

_tool3() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
        --case-file)
            COMPREPLY=($(compgen -f -- $cur))
            return 0
            ;;
        *)
            COMPREPLY=($(compgen -W "-I --case-file " -- $cur))
            return 0
            ;;
    esac
}

_itools() {
    compopt -o filenames

    if [[ "${#COMP_WORDS[@]}" == 2 ]]; then
        local cur=${COMP_WORDS[COMP_CWORD]}
        COMPREPLY=($(compgen -W "tool1 tool2 tool3 " -- $cur))
    else
        local cmd=${COMP_WORDS[1]}
        case "$cmd" in
            tool1)
                _tool1
                return 0
                ;;
            tool2)
                _tool2
                return 0
                ;;
            tool3)
                _tool3
                return 0
                ;;
        esac
    fi
}

complete -F _itools itools
