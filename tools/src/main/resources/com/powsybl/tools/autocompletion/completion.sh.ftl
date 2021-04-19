[#ftl strip_whitespace=true/]

[#macro compgenopt option]
[#if option.type.kind == "FILE"]
    -f[#t]
[#elseif option.type.kind == "DIRECTORY"]
    -d[#t]
[#elseif option.type.kind == "HOSTNAME"]
    -A hostname[#t]
[#elseif option.type.kind == "ENUMERATION"]
    -W "[#list option.possibleValues as e][=e] [/#list]"[#t]
[/#if]
[/#macro]

[#macro completionFunction functionName options]
_[=functionName]() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
[#list options as option]
    [#if option.hasArg()]
        [=option.name])
            COMPREPLY=($(compgen [@compgenopt option=option/] -- $cur))
            return 0
            ;;
    [/#if]
[/#list]
        *)
            COMPREPLY=($(compgen -W "[#list options as option][=option.name] [/#list]" -- $cur))
            return 0
            ;;
    esac
}
[/#macro]
[#list commands as command]
[@completionFunction functionName=command.name options=command.options/]

[/#list]
_[=toolName]() {
    compopt -o filenames

    [#noparse]if [[ "${#COMP_WORDS[@]}" == 2 ]]; then[/#noparse]
        local cur=${COMP_WORDS[COMP_CWORD]}
        COMPREPLY=($(compgen -W "[#list commands as c][=c.name] [/#list]" -- $cur))
    else
        local cmd=${COMP_WORDS[1]}
        case "$cmd" in
[#list commands as c]
            [=c.name])
                _[=c.name]
                return 0
                ;;
[/#list]
        esac
    fi
}

complete -F _[=toolName] [=toolName]
