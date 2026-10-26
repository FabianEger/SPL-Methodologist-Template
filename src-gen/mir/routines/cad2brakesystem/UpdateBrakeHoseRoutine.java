package mir.routines.cad2brakesystem;

import brakesystem.BrakeHose;
import edu.kit.ipd.sdq.metamodels.cad.NumericParameter;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import edu.kit.ipd.sdq.metamodels.cad.StringParameter;
import java.io.IOException;
import java.util.Objects;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateBrakeHoseRoutine extends AbstractRoutine {
  private UpdateBrakeHoseRoutine.InputValues inputValues;

  private UpdateBrakeHoseRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Parameter parameter;

    public InputValues(final Parameter parameter) {
      this.parameter = parameter;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final BrakeHose brakeHose;

      public RetrievedValues(final BrakeHose brakeHose) {
        this.brakeHose = brakeHose;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceBrakeHose(final Parameter parameter) {
      EObject _eContainer = parameter.eContainer();
      return _eContainer;
    }

    public UpdateBrakeHoseRoutine.Match.RetrievedValues match(final Parameter parameter) throws IOException {
      brakesystem.BrakeHose brakeHose = getCorrespondingElement(
      	getCorrepondenceSourceBrakeHose(parameter), // correspondence source supplier
      	brakesystem.BrakeHose.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (brakeHose == null) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.UpdateBrakeHoseRoutine.Match.RetrievedValues(brakeHose);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Parameter parameter, final BrakeHose brakeHose, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      if ((parameter instanceof StringParameter)) {
        final StringParameter stringParam = ((StringParameter) parameter);
        String _name = ((StringParameter)parameter).getName();
        if (_name != null) {
          switch (_name) {
            case "Thread 1":
              brakeHose.setThreadSize1(stringParam.getValue());
              break;
            case "Thread 2":
              brakeHose.setThreadSize2(stringParam.getValue());
              break;
            default:
              return;
          }
        } else {
          return;
        }
        return;
      }
      if ((parameter instanceof NumericParameter)) {
        final NumericParameter numericParam = ((NumericParameter) parameter);
        String _name_1 = numericParam.getName();
        boolean _equals = Objects.equals(_name_1, "Length");
        if (_equals) {
          brakeHose.setLengthInMM(Math.round(numericParam.getValue()));
        }
        return;
      }
    }
  }

  public UpdateBrakeHoseRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Parameter parameter) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateBrakeHoseRoutine.InputValues(parameter);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateBrakeHoseRoutine with input:");
    	getLogger().trace("   inputValues.parameter: " + inputValues.parameter);
    }
    retrievedValues = new mir.routines.cad2brakesystem.UpdateBrakeHoseRoutine.Match(getExecutionState()).match(inputValues.parameter);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.UpdateBrakeHoseRoutine.Update(getExecutionState()).updateModels(inputValues.parameter, retrievedValues.brakeHose, getRoutinesFacade());
    return true;
  }
}
