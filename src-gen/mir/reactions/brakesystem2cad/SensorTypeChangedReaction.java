package mir.reactions.brakesystem2cad;

import brakesystem.ABSSensor;
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
public class SensorTypeChangedReaction extends AbstractReaction {
  private ReplaceSingleValuedEAttribute<ABSSensor, String> replaceChange;

  public SensorTypeChangedReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ReplaceSingleValuedEAttribute replaceChange, final ABSSensor affectedEObject, final EAttribute affectedFeature, final String oldValue, final String newValue, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      _routinesFacade.updateSensorType(affectedEObject);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof ReplaceSingleValuedEAttribute<?, ?>)) {
    	return false;
    }
    
    ReplaceSingleValuedEAttribute<brakesystem.ABSSensor, java.lang.String> _localTypedChange = (ReplaceSingleValuedEAttribute<brakesystem.ABSSensor, java.lang.String>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof brakesystem.ABSSensor)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("sensorType")) {
    	return false;
    }
    if (_localTypedChange.isFromNonDefaultValue() && !(_localTypedChange.getOldValue() instanceof java.lang.String)) {
    	return false;
    }
    if (_localTypedChange.isToNonDefaultValue() && !(_localTypedChange.getNewValue() instanceof java.lang.String)) {
    	return false;
    }
    this.replaceChange = (ReplaceSingleValuedEAttribute<brakesystem.ABSSensor, java.lang.String>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade routinesFacade = (mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    brakesystem.ABSSensor affectedEObject = (brakesystem.ABSSensor)replaceChange.getAffectedElement();
    EAttribute affectedFeature = replaceChange.getAffectedFeature();
    java.lang.String oldValue = (java.lang.String)replaceChange.getOldValue();
    java.lang.String newValue = (java.lang.String)replaceChange.getNewValue();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.brakesystem2cad.SensorTypeChangedReaction.Call(executionState).updateModels(replaceChange, affectedEObject, affectedFeature, oldValue, newValue, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
