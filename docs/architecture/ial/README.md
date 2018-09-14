# iAL: the iTesla Action Language 

**iAL** (**i**Tesla **A**ction **L**anguage) is a DSL (Domain Specific Language) written in Java and Groovy for action simulation on a network. 
It was designed to be more flexible and scalable than existing software and easier to use than generic languages.


## What are iAL's key concepts?

The three iAL's concepts are:

- the [contingency list](contingencies.md)
- the definition of [actions](actions.md)
- the definition of [rules](rules.md)

## What does a iAL script look like?

Here is an example of a iAL script:

```
condition1 = logicalCondition1
condition2 = logicalCondition1

contingency('contingency-id') {
    equipments 'equipment1-id', 'equipement2-id'
}

rule('rule1-id') {
    description "this is rule 1"
    when condition1 || condition2
    apply 'action1-id'
}

action('action1-id') {
    description "this is action 1"
    tasks {
        predefinedAction1
    }
}


rule('rule2-id') {
    description "this is rule 2"
    when condition2 && actionTaken('action1-id')
    apply 'action2-id'
}

action('action2-id') {
    description "this is action 2"
    tasks {
        script {
            groovyAction2
        }
    }
}
```

In a iAL script, there is a mix between DSL language and Groovy Language: 
The syntax is restricted to the DSL, except than in the header and in the ```script``` part of the "action" section.

## How is a calculation performed?

In the static implementation (the only one for now, the dynamic implementation is still to come) there are rounds of calculation.

A first [load flow](../loadflow/README.md) is performed on the pre contingency state. All the rules are evaluated.
The actions associated with the rules which are evaluated to "true" are applied and a new load flow is performed.
The rules are evaluated on the new states and so on until no rule match.

Then, all the contingencies described in the script are simulated one by one.
For every contingency, the rules are evaluated as previously and as many load flows (rounds) as necessary are performed.

## References:

* 2017 iPST-day: [iAL - the iTesla Action Language](http://www.itesla-pst.org/pdf/iPST-day-2017/05%20-%20iPST%20day%20-%20iAL%20-%20the%20iTesla%20Action%20Language.pdf)