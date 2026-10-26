package mir.routines.brakesystem2cad;

import brakesystem.BrakeComponent;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateIdRoutine extends AbstractRoutine {
  private UpdateIdRoutine.InputValues inputValues;

  private UpdateIdRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final BrakeComponent comp;

    public InputValues(final BrakeComponent comp) {
      this.comp = comp;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Namespace namespace;

      public RetrievedValues(final Namespace namespace) {
        this.namespace = namespace;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceNamespace(final BrakeComponent comp) {
      return comp;
    }

    public UpdateIdRoutine.Match.RetrievedValues match(final BrakeComponent comp) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(comp), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateIdRoutine.Match.RetrievedValues(namespace);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeComponent comp, final Namespace namespace, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      namespace.setId(comp.getId());
    }
  }

  public UpdateIdRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeComponent comp) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateIdRoutine.InputValues(comp);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateIdRoutine with input:");
    	getLogger().trace("   inputValues.comp: " + inputValues.comp);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateIdRoutine.Match(getExecutionState()).match(inputValues.comp);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.brakesystem2cad.UpdateIdRoutine.Update(getExecutionState()).updateModels(inputValues.comp, retrievedValues.namespace, getRoutinesFacade());
    return true;
  }
}
