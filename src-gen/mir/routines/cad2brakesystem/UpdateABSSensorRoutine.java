package mir.routines.cad2brakesystem;

import brakesystem.ABSSensor;
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
public class UpdateABSSensorRoutine extends AbstractRoutine {
  private UpdateABSSensorRoutine.InputValues inputValues;

  private UpdateABSSensorRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Parameter parameter;

    public InputValues(final Parameter parameter) {
      this.parameter = parameter;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final ABSSensor absSensor;

      public RetrievedValues(final ABSSensor absSensor) {
        this.absSensor = absSensor;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceAbsSensor(final Parameter parameter) {
      EObject _eContainer = parameter.eContainer();
      return _eContainer;
    }

    public UpdateABSSensorRoutine.Match.RetrievedValues match(final Parameter parameter) throws IOException {
      brakesystem.ABSSensor absSensor = getCorrespondingElement(
      	getCorrepondenceSourceAbsSensor(parameter), // correspondence source supplier
      	brakesystem.ABSSensor.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (absSensor == null) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.UpdateABSSensorRoutine.Match.RetrievedValues(absSensor);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Parameter parameter, final ABSSensor absSensor, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      if (((parameter instanceof StringParameter) && Objects.equals(parameter.getName(), "Sensor Type"))) {
        final StringParameter stringParam = ((StringParameter) parameter);
        absSensor.setSensorType(stringParam.getValue());
        return;
      }
      if ((parameter instanceof NumericParameter)) {
        final NumericParameter numericParam = ((NumericParameter) parameter);
        String _name = numericParam.getName();
        if (_name != null) {
          switch (_name) {
            case "Fitting Depth":
              absSensor.setFittingDepth(Math.round(numericParam.getValue()));
              break;
            case "Number of Pins":
              absSensor.setNumberOfPins(Math.round(numericParam.getValue()));
              break;
            case "Length":
              absSensor.setLengthInMM(Math.round(numericParam.getValue()));
              break;
            default:
              return;
          }
        } else {
          return;
        }
        return;
      }
    }
  }

  public UpdateABSSensorRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Parameter parameter) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateABSSensorRoutine.InputValues(parameter);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateABSSensorRoutine with input:");
    	getLogger().trace("   inputValues.parameter: " + inputValues.parameter);
    }
    retrievedValues = new mir.routines.cad2brakesystem.UpdateABSSensorRoutine.Match(getExecutionState()).match(inputValues.parameter);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.UpdateABSSensorRoutine.Update(getExecutionState()).updateModels(inputValues.parameter, retrievedValues.absSensor, getRoutinesFacade());
    return true;
  }
}
