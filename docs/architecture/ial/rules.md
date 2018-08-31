# iAL Rules

## Syntax

The generic syntax to describe a rule is:

```
rule('rule-id') {
  description "This is a description" // optional
  when (condition1 || condition2) && condition3
  apply 'action1-id', 'action2-id'
  life N // optional
}
```

A rule associates logical conditions and [actions](actions.md) IDs.  
The ```description``` attribute is optional.   
The "life" parameter is optional and determines how many times a rule can be evaluated to true (if not set, the default is ```life = 1```) 

## Logical conditions

### Basic logical conditions

Reminder: the priority of logical operators is first "NOT", then "AND", finally "OR".

Here are a few examples :

```
Day_hours = (network.caseDate.hourOfDay > 6 && network.caseDate.hourOfDay < 18)
Night_hours = !Day_hours

Switch1_is_open = switch_('switch1-id').open
```

The logical conditions need to be defined in the header.

### Chaining actions

In the example below, action2 is applied only if action1 was applied in a previous round AND if switch1 is open.

```
rule('rule2-id') {
  when actionTaken('action1-id') && Switch1_is_open
  apply 'action2-id'
}
```

In the current implementation, there is no notion of timeline. The only way to simulate consecutive actions is to chain them.

*Nota bene*: action1 is considered taken if the associated rule is evaluated to true, even if it consists in closing a switch which is already closed for example.*

### N and N-1 states

Here is an example of action applied on the pre contingency state.
```
rule('rule-id') {
  when !contingencyOccurred()
  apply 'pre-contingency-action-id'
}
```

Here is an example of action applied on a post contingency state (after contingency1).
```
rule('rule-id') {
  when contingencyOccurred('contingency1')
  apply 'post-contingency1-action-id'
}
```

