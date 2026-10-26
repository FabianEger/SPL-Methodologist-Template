package mir.reactions.brakesystem2cad;

import brakesystem.BrakeDisk;
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
public class VentilatedChangedReaction extends AbstractReaction {
  private ReplaceSingleValuedEAttribute<BrakeDisk, Boolean> replaceChange;

  public VentilatedChangedReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEAttribute replaceChange, final BrakeDisk affectedEObject, final EAttribute affectedFeature, final Boolean oldValue, final Boolean newValue, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      _routinesFacade.updateVentilated(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEAttribute<?, ?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEAttribute<brakesystem.BrakeDisk, java.lang.Boolean> _localTypedChange = (ReplaceSingleValuedEAttribute<brakesystem.BrakeDisk, java.lang.Boolean>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof brakesystem.BrakeDisk)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("ventilated")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof java.lang.Boolean)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof java.lang.Boolean)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEAttribute<brakesystem.BrakeDisk, java.lang.Boolean>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade routinesFacade = (mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    brakesystem.BrakeDisk affectedEObject = (brakesystem.BrakeDisk)replaceChange.getAffectedElement();
    EAttribute affectedFeature = replaceChange.getAffectedFeature();
    java.lang.Boolean oldValue = (java.lang.Boolean)replaceChange.getOldValue();
    java.lang.Boolean newValue = (java.lang.Boolean)replaceChange.getNewValue();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.brakesystem2cad.VentilatedChangedReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
