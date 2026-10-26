package mir.reactions.feature2config;

import UVLPackage.UVLModel;
import java.util.function.Function;
import mir.routines.feature2config.Feature2configRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class UVLModelInsertedAsRootReaction extends AbstractReaction {
  private InsertRootEObject<UVLModel> insertChange;

  public UVLModelInsertedAsRootReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertRootEObject insertChange, final UVLModel newValue, final int index, @Extension final Feature2configRoutinesFacade _routinesFacade) {
      _routinesFacade.createConfigForUVLModel(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertRootEObject<?>)) {
    	return false;
    }
    
    InsertRootEObject<UVLPackage.UVLModel> _localTypedChange = (InsertRootEObject<UVLPackage.UVLModel>) change;
    if (!(_localTypedChange.getNewValue() instanceof UVLPackage.UVLModel)) {
    	return false;
    }
    this.insertChange = (InsertRootEObject<UVLPackage.UVLModel>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.feature2config.Feature2configRoutinesFacade routinesFacade = (mir.routines.feature2config.Feature2configRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    UVLPackage.UVLModel newValue = (UVLPackage.UVLModel)insertChange.getNewValue();
    int index = insertChange.getIndex();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.feature2config.UVLModelInsertedAsRootReaction.Call(executionState).updateModels(insertChange, newValue, index, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
