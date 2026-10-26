package mir.reactions.brakesystem2cad;

import brakesystem.Brakesystem;
import java.util.function.Function;
import mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class BrakesystemInsertedAsRootReaction extends AbstractReaction {
  private InsertRootEObject<Brakesystem> insertChange;

  public BrakesystemInsertedAsRootReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertRootEObject insertChange, final Brakesystem newValue, final int index, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      _routinesFacade.createAndRegisterRoot(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertRootEObject<?>)) {
    	return false;
    }
    
    InsertRootEObject<brakesystem.Brakesystem> _localTypedChange = (InsertRootEObject<brakesystem.Brakesystem>) change;
    if (!(_localTypedChange.getNewValue() instanceof brakesystem.Brakesystem)) {
    	return false;
    }
    this.insertChange = (InsertRootEObject<brakesystem.Brakesystem>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade routinesFacade = (mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    brakesystem.Brakesystem newValue = (brakesystem.Brakesystem)insertChange.getNewValue();
    int index = insertChange.getIndex();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.brakesystem2cad.BrakesystemInsertedAsRootReaction.Call(executionState).updateModels(insertChange, newValue, index, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
