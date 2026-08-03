# Design choices

In the next section, we will provide examples that justify our design choices.  
This design is based on the existing PowSyBl model.  
We propose a modification to the existing PowSyBl model to add the application delay and the creation of a new object to
represent the operator strategy not yet optimized.

## Application delay

The application delay allows simulating chronologically ordered events.

For example, let's consider a PST range action that we want to optimize.  
To avoid any threshold crossing in case of any contingency, the PST has been optimized to a given position.  
This corresponds to a `ContingencyContextType.ALL`.

Now, let's consider that a contingency might occur.  
We want to optimize the PST set point to avoid threshold crossing based on the time after the contingency.  
In fact, thresholds vary depending on the time after the contingency.  
We might also have a limited range of possible set points due to the physical constraints of the system.  
Furthermore, some range actions might take some time to be applied.  
This delay can be a response time or an operator application time, for example.

## Optimizable Remedial Action

The optimizer will choose the optimal set point of the actions.  
This means that we need a new object to represent the operator strategy not yet optimized "OptimizableRemedialAction".  
This object will have:

- an ID;
- a condition: to model in which condition, the action will be applied;
- a contingency context: to model in which contingency context (N state, after a contingency, etc., ...), the action
  will be applied;
- an application delay (optional)
- some supplementary temporal contraints (optional)

An optimizable remedial action can either be a binary remedial action or a range remedial action.

## Binary remedial action

## Range remedial action

## Ranges

TODO

## Extra temporal contraints

If we need to optimize remedial actions with several timesteps, we might need some supplementary contraints.  
This object will store the contraints values.  
They have been sorted in three categories:
- simple constraints: contraints that are independent of the time step (level time for example)
- budget constraints: contraints that depend on the entire time window (max energy for example)
- variable contraints: contraints with bounds that varies over time (min power for example)
