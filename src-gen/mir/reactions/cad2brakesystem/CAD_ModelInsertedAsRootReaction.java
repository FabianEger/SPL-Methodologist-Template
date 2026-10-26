package mir.reactions.cad2brakesystem;

import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import java.util.function.Function;
import mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacade;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.atomic.EChange;
import tools.vitruv.change.atomic.root.InsertRootEObject;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReaction;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;

@SuppressWarnings("all")
public class CAD_ModelInsertedAsRootReaction extends AbstractReaction {
  private InsertRootEObject<CAD_Model> insertChange;

  public CAD_ModelInsertedAsRootReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertRootEObject insertChange, final CAD_Model newValue, final int index, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      _routinesFacade.createAndRegisterRoot(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertRootEObject<?>)) {
    	return false;
    }
    
    InsertRootEObject<edu.kit.ipd.sdq.metamodels.cad.CAD_Model> _localTypedChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.cad.CAD_Model>) change;
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.cad.CAD_Model)) {
    	return false;
    }
    this.insertChange = (InsertRootEObject<edu.kit.ipd.sdq.metamodels.cad.CAD_Model>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacade routinesFacade = (mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.cad.CAD_Model newValue = (edu.kit.ipd.sdq.metamodels.cad.CAD_Model)insertChange.getNewValue();
    int index = insertChange.getIndex();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.cad2brakesystem.CAD_ModelInsertedAsRootReaction.Call(executionState).updateModels(insertChange, newValue, index, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
