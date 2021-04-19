compgenopt(option) ::= <<
<if(option.file)>-f<\\>
<elseif(option.dir)>-d<\\>
<elseif(option.hostname)>-A hostname<\\>
<else>-W "<option.possibleValues; separator=" "> "<\\>
<endif>
>>

caseOption(option) ::= <<
<if(option.arg)>
        <option.name>)
            COMPREPLY=($(compgen <compgenopt(option)> -- $cur))
            return 0
            ;;
<endif>
>>

subcommandFunction(command) ::= <<
_<command.name>() {
    local cur=${COMP_WORDS[COMP_CWORD]}
    local prev=${COMP_WORDS[COMP_CWORD-1]}
    case "$prev" in
<command.options:caseOption()><\\>
        *)
            COMPREPLY=($(compgen -W "<command.options:{ option | <option.name> }>" -- $cur))
            return 0
            ;;
    esac
}
>>

commandFunction(toolName, commands) ::= <<
_<toolName>() {
    compopt -o filenames

    if [[ "${#COMP_WORDS[@]}" == 2 ]]; then
        local cur=${COMP_WORDS[COMP_CWORD]}
        COMPREPLY=($(compgen -W "<commands:{c | <c.name> }>" -- $cur))
    else
        local cmd=${COMP_WORDS[1]}
        case "$cmd" in
<commands:{ c |
            <c.name>)
                _<c.name>
                return 0
                ;;
}><\\>
        esac
    fi
}
>>

completionScript(toolName, commands) ::= <<
<commands:{c | <subcommandFunction(c)>}>

<commandFunction(toolName, commands)>

complete -F _<toolName> <toolName>

>>
