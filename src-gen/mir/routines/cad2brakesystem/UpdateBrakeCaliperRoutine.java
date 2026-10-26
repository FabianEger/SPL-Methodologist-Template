package mir.routines.cad2brakesystem;

import brakesystem.BrakeCaliper;
import edu.kit.ipd.sdq.metamodels.cad.NumericParameter;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateBrakeCaliperRoutine extends AbstractRoutine {
  private UpdateBrakeCaliperRoutine.InputValues inputValues;

  private UpdateBrakeCaliperRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Parameter parameter;

    public InputValues(final Parameter parameter) {
      this.parameter = parameter;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final BrakeCaliper brakeCaliper;

      public RetrievedValues(final BrakeCaliper brakeCaliper) {
        this.brakeCaliper = brakeCaliper;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceBrakeCaliper(final Parameter parameter) {
      EObject _eContainer = parameter.eContainer();
      return _eContainer;
    }

    public UpdateBrakeCaliperRoutine.Match.RetrievedValues match(final Parameter parameter) throws IOException {
      brakesystem.BrakeCaliper brakeCaliper = getCorrespondingElement(
      	getCorrepondenceSourceBrakeCaliper(parameter), // correspondence source supplier
      	brakesystem.BrakeCaliper.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (brakeCaliper == null) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.UpdateBrakeCaliperRoutine.Match.RetrievedValues(brakeCaliper);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Parameter parameter, final BrakeCaliper brakeCaliper, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      if ((parameter instanceof NumericParameter)) {
        final NumericParameter numericParam = ((NumericParameter) parameter);
        String _name = numericParam.getName();
        if (_name != null) {
          switch (_name) {
            case "Piston Diameter":
              brakeCaliper.setPistonDiameterInMM(Math.round(numericParam.getValue()));
              break;
            case "Brake Disk Thickness":
              brakeCaliper.setBrakeDiskThickness(Math.round(numericParam.getValue()));
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

  public UpdateBrakeCaliperRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Parameter parameter) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateBrakeCaliperRoutine.InputValues(parameter);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateBrakeCaliperRoutine with input:");
    	getLogger().trace("   inputValues.parameter: " + inputValues.parameter);
    }
    retrievedValues = new mir.routines.cad2brakesystem.UpdateBrakeCaliperRoutine.Match(getExecutionState()).match(inputValues.parameter);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.UpdateBrakeCaliperRoutine.Update(getExecutionState()).updateModels(inputValues.parameter, retrievedValues.brakeCaliper, getRoutinesFacade());
    return true;
  }
}
