package mir.routines.cad2brakesystem;

import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import java.io.IOException;
import java.util.Arrays;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.change.interaction.UserInteractionOptions;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateAndRegisterNamespaceRoutine extends AbstractRoutine {
  private CreateAndRegisterNamespaceRoutine.InputValues inputValues;

  private CreateAndRegisterNamespaceRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Namespace namespace;

    public InputValues(final Namespace namespace) {
      this.namespace = namespace;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Brakesystem mBrake;

      public RetrievedValues(final Brakesystem mBrake) {
        this.mBrake = mBrake;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceMBrake(final Namespace namespace) {
      EObject _eContainer = namespace.eContainer();
      return _eContainer;
    }

    public EObject getCorrepondenceSource1(final Namespace namespace, final Brakesystem mBrake) {
      return namespace;
    }

    public CreateAndRegisterNamespaceRoutine.Match.RetrievedValues match(final Namespace namespace) throws IOException {
      brakesystem.Brakesystem mBrake = getCorrespondingElement(
      	getCorrepondenceSourceMBrake(namespace), // correspondence source supplier
      	brakesystem.Brakesystem.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mBrake == null) {
      	return null;
      }
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(namespace, mBrake), // correspondence source supplier
      	brakesystem.BrakeComponent.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.CreateAndRegisterNamespaceRoutine.Match.RetrievedValues(mBrake);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Namespace namespace, final Brakesystem mBrake, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      final Integer componentType = this.executionState.userInteractor().getSingleSelectionDialogBuilder().message(
        "What brake component do you want to add to be correspondent to this namespace?").choices(
        Arrays.<String>asList("ABSSensor", "BrakeCaliper", "BrakeHose", "BrakeDisk")).windowModality(UserInteractionOptions.WindowModality.MODAL).startInteraction();
      if (componentType != null) {
        switch (componentType) {
          case 0:
            _routinesFacade.createABSSensor(namespace, mBrake);
            break;
          case 1:
            _routinesFacade.createBrakeCaliper(namespace, mBrake);
            break;
          case 2:
            _routinesFacade.createBrakeHose(namespace, mBrake);
            break;
          case 3:
            _routinesFacade.createBrakeDisk(namespace, mBrake);
            break;
          default:
            return;
        }
      } else {
        return;
      }
    }
  }

  public CreateAndRegisterNamespaceRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Namespace namespace) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterNamespaceRoutine.InputValues(namespace);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterNamespaceRoutine with input:");
    	getLogger().trace("   inputValues.namespace: " + inputValues.namespace);
    }
    retrievedValues = new mir.routines.cad2brakesystem.CreateAndRegisterNamespaceRoutine.Match(getExecutionState()).match(inputValues.namespace);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.CreateAndRegisterNamespaceRoutine.Update(getExecutionState()).updateModels(inputValues.namespace, retrievedValues.mBrake, getRoutinesFacade());
    return true;
  }
}
