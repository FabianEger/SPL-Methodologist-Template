package mir.routines.cad2brakesystem;

import brakesystem.BrakeComponent;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import edu.kit.ipd.sdq.metamodels.cad.StringParameter;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateBrakeComponentRoutine extends AbstractRoutine {
  private UpdateBrakeComponentRoutine.InputValues inputValues;

  private UpdateBrakeComponentRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Parameter parameter;

    public InputValues(final Parameter parameter) {
      this.parameter = parameter;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final BrakeComponent mBrakeComponent;

      public RetrievedValues(final BrakeComponent mBrakeComponent) {
        this.mBrakeComponent = mBrakeComponent;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceMBrakeComponent(final Parameter parameter) {
      EObject _eContainer = parameter.eContainer();
      return _eContainer;
    }

    public UpdateBrakeComponentRoutine.Match.RetrievedValues match(final Parameter parameter) throws IOException {
      brakesystem.BrakeComponent mBrakeComponent = getCorrespondingElement(
      	getCorrepondenceSourceMBrakeComponent(parameter), // correspondence source supplier
      	brakesystem.BrakeComponent.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mBrakeComponent == null) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.UpdateBrakeComponentRoutine.Match.RetrievedValues(mBrakeComponent);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Parameter parameter, final BrakeComponent mBrakeComponent, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      final StringParameter stringParam = ((StringParameter) parameter);
      String _name = parameter.getName();
      if (_name != null) {
        switch (_name) {
          case "Specification Type":
            mBrakeComponent.setSpecificationType(stringParam.getValue());
            break;
          case "OEM Number":
            mBrakeComponent.setOEM_number(stringParam.getValue());
            break;
          case "Fitting Position":
            mBrakeComponent.setFittingPosition(stringParam.getValue());
            break;
          default:
            return;
        }
      } else {
        return;
      }
    }
  }

  public UpdateBrakeComponentRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Parameter parameter) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateBrakeComponentRoutine.InputValues(parameter);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateBrakeComponentRoutine with input:");
    	getLogger().trace("   inputValues.parameter: " + inputValues.parameter);
    }
    retrievedValues = new mir.routines.cad2brakesystem.UpdateBrakeComponentRoutine.Match(getExecutionState()).match(inputValues.parameter);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.UpdateBrakeComponentRoutine.Update(getExecutionState()).updateModels(inputValues.parameter, retrievedValues.mBrakeComponent, getRoutinesFacade());
    return true;
  }
}
