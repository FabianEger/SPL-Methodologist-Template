package mir.reactions.cad2brakesystem;

import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import java.util.function.Function;
import mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacade;
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
public class NameSpaceInsertedReaction extends AbstractReaction {
  private InsertEReference<EObject> insertChange;

  public NameSpaceInsertedReaction(final Function<ReactionExecutionState, RoutinesFacade> routinesFacadeGenerator) {
    super(routinesFacadeGenerator);
  }

  private static class Call extends AbstractRoutine.Update {
    public Call(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final InsertEReference insertChange, final CAD_Model affectedEObject, final EReference affectedFeature, final Namespace newValue, final int index, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      _routinesFacade.createAndRegisterNamespace(newValue);
    }
  }

  public boolean isCurrentChangeMatchingTrigger(final EChange change) {
    if (!(change instanceof InsertEReference<?>)) {
    	return false;
    }
    
    InsertEReference<org.eclipse.emf.ecore.EObject> _localTypedChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    if (!(_localTypedChange.getAffectedElement() instanceof edu.kit.ipd.sdq.metamodels.cad.CAD_Model)) {
    	return false;
    }
    if (!_localTypedChange.getAffectedFeature().getName().equals("namespaces")) {
    	return false;
    }
    if (!(_localTypedChange.getNewValue() instanceof edu.kit.ipd.sdq.metamodels.cad.Namespace)) {
    	return false;
    }
    this.insertChange = (InsertEReference<org.eclipse.emf.ecore.EObject>) change;
    return true;
  }

  public void executeReaction(final EChange change, final ReactionExecutionState executionState, final RoutinesFacade routinesFacadeUntyped) {
    mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacade routinesFacade = (mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacade)routinesFacadeUntyped;
    if (!isCurrentChangeMatchingTrigger(change)) {
    	return;
    }
    edu.kit.ipd.sdq.metamodels.cad.CAD_Model affectedEObject = (edu.kit.ipd.sdq.metamodels.cad.CAD_Model)insertChange.getAffectedElement();
    EReference affectedFeature = insertChange.getAffectedFeature();
    edu.kit.ipd.sdq.metamodels.cad.Namespace newValue = (edu.kit.ipd.sdq.metamodels.cad.Namespace)insertChange.getNewValue();
    int index = insertChange.getIndex();
    			  ChangePropagationSpecification specification = (tools.vitruv.change.propagation.ChangePropagationSpecification) executionState.getChangePropagationObservable();
    specification.notifyChangePropagationStarted(specification, change);
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Passed complete precondition check of Reaction " + this.getClass().getName());
    }
    
    new mir.reactions.cad2brakesystem.NameSpaceInsertedReaction.Call(executionState).updateModels(insertChange, affectedEObject, affectedFeature, newValue, index, routinesFacade);
    specification.notifyChangePropagationStopped(specification, change);
  }
}
