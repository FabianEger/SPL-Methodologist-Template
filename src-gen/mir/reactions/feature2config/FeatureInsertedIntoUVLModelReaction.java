package mir.reactions.feature2config;

import UVLPackage.Feature;
import UVLPackage.Group;
import java.util.function.Function;
import mir.routines.feature2config.Feature2configRoutinesFacade;
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
public class FeatureInsertedIntoUVLModelReaction extends AbstractReaction {
  private InsertEReference<EObject> insertChange;

  public FeatureInsertedIntoUVLModelReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertEReference insertChange, final Group affectedEObject, final EReference affectedFeature, final Feature newValue, final int index, @Extension final Feature2configRoutinesFacade _routinesFacade) {
      _routinesFacade.createConfigForFeature(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertEReference<?>)) {
    	return false;
    }
    
    InsertEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof UVLPackage.Group)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("feature")) {
    	return false;
    }
    if (!(_localTypedChange.getNewValue() instanceof UVLPackage.Feature)) {
    	return false;
    }
    this.insertChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.feature2config.Feature2configRoutinesFacade routinesFacade = (mir.routines.feature2config.Feature2configRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    UVLPackage.Group affectedEObject = (UVLPackage.Group)insertChange.getAffectedElement();
    EReference affectedFeature = insertChange.getAffectedFeature();
    UVLPackage.Feature newValue = (UVLPackage.Feature)insertChange.getNewValue();
    int index = insertChange.getIndex();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.feature2config.FeatureInsertedIntoUVLModelReaction.Call(executionState).updateModels(insertChange, affectedEObject, affectedFeature, newValue, index, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
