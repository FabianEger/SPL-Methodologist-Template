package mir.reactions.brakesystem2cad;

import brakesystem.ABSSensor;
import brakesystem.Brakesystem;
import java.util.function.Function;
import mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.feature.reference.InsertEReference;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class ABSSensorInsertedReaction extends AbstractReaction {
  private InsertEReference<EObject> insertChange;

  public ABSSensorInsertedReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertEReference insertChange, final Brakesystem affectedEObject, final EReference affectedFeature, final ABSSensor newValue, final int index, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      _routinesFacade.createAndRegisterABSSensor(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertEReference<?>)) {
    	return false;
    }
    
    InsertEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof brakesystem.Brakesystem)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("BrakeComponents")) {
    	return false;
    }
    if (!(_localTypedChange.getNewValue() instanceof brakesystem.ABSSensor)) {
    	return false;
    }
    this.insertChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade routinesFacade = (mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    brakesystem.Brakesystem affectedEObject = (brakesystem.Brakesystem)insertChange.getAffectedElement();
    EReference affectedFeature = insertChange.getAffectedFeature();
    brakesystem.ABSSensor newValue = (brakesystem.ABSSensor)insertChange.getNewValue();
    int index = insertChange.getIndex();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.brakesystem2cad.ABSSensorInsertedReaction.Call(executionState).updateModels(insertChange, affectedEObject, affectedFeature, newValue, index, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
