package mir.reactions.brakesystem2cad;

import brakesystem.BrakeHose;
import java.util.function.Function;
import mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.attribute.ReplaceSingleValuedEAttribute;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class ThreadSize2ChangedReaction extends AbstractReaction {
  private ReplaceSingleValuedEAttribute<BrakeHose, String> replaceChange;

  public ThreadSize2ChangedReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEAttribute replaceChange, final BrakeHose affectedEObject, final EAttribute affectedFeature, final String oldValue, final String newValue, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      _routinesFacade.updateThreadSize2(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEAttribute<?, ?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEAttribute<brakesystem.BrakeHose, java.lang.String> _localTypedChange = (ReplaceSingleValuedEAttribute<brakesystem.BrakeHose, java.lang.String>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof brakesystem.BrakeHose)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("threadSize2")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof java.lang.String)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof java.lang.String)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEAttribute<brakesystem.BrakeHose, java.lang.String>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade routinesFacade = (mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    brakesystem.BrakeHose affectedEObject = (brakesystem.BrakeHose)replaceChange.getAffectedElement();
    EAttribute affectedFeature = replaceChange.getAffectedFeature();
    java.lang.String oldValue = (java.lang.String)replaceChange.getOldValue();
    java.lang.String newValue = (java.lang.String)replaceChange.getNewValue();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.brakesystem2cad.ThreadSize2ChangedReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
