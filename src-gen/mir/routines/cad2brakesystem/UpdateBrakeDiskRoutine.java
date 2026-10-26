package mir.routines.cad2brakesystem;

import brakesystem.BrakeDisk;
import edu.kit.ipd.sdq.metamodels.cad.BooleanParameter;
import edu.kit.ipd.sdq.metamodels.cad.NumericParameter;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import java.io.IOException;
import java.util.Objects;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateBrakeDiskRoutine extends AbstractRoutine {
  private UpdateBrakeDiskRoutine.InputValues inputValues;

  private UpdateBrakeDiskRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Parameter parameter;

    public InputValues(final Parameter parameter) {
      this.parameter = parameter;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final BrakeDisk brakeDisk;

      public RetrievedValues(final BrakeDisk brakeDisk) {
        this.brakeDisk = brakeDisk;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceBrakeDisk(final Parameter parameter) {
      EObject _eContainer = parameter.eContainer();
      return _eContainer;
    }

    public UpdateBrakeDiskRoutine.Match.RetrievedValues match(final Parameter parameter) throws IOException {
      brakesystem.BrakeDisk brakeDisk = getCorrespondingElement(
      	getCorrepondenceSourceBrakeDisk(parameter), // correspondence source supplier
      	brakesystem.BrakeDisk.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (brakeDisk == null) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.UpdateBrakeDiskRoutine.Match.RetrievedValues(brakeDisk);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Parameter parameter, final BrakeDisk brakeDisk, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      if ((parameter instanceof NumericParameter)) {
        final NumericParameter numericParam = ((NumericParameter) parameter);
        String _name = numericParam.getName();
        if (_name != null) {
          switch (_name) {
            case "Diameter":
              brakeDisk.setDiameterInMM(Math.round(numericParam.getValue()));
              break;
            case "Centering Diameter":
              brakeDisk.setCenteringDiameterInMM(Math.round(numericParam.getValue()));
              break;
            case "Rim Hole Number":
              brakeDisk.setRimHoleNumber(Math.round(numericParam.getValue()));
              break;
            case "Hole Arrangement Number":
              brakeDisk.setHoleArrangementNumber(Math.round(numericParam.getValue()));
              break;
            case "Bolt Hole Circle":
              brakeDisk.setBoltHoleCircleInMM(Math.round(numericParam.getValue()));
              break;
            case "Brake Disk Thickness":
              brakeDisk.setBrakeDiskThicknessInMM(Math.round(numericParam.getValue()));
              break;
            case "Minimum Thickness":
              brakeDisk.setMinimumThicknessInMM(Math.round(numericParam.getValue()));
              break;
            default:
              return;
          }
        } else {
          return;
        }
        return;
      }
      if ((parameter instanceof BooleanParameter)) {
        final BooleanParameter booleanParam = ((BooleanParameter) parameter);
        String _name_1 = booleanParam.getName();
        boolean _equals = Objects.equals(_name_1, "Ventilated");
        if (_equals) {
          brakeDisk.setVentilated(booleanParam.isValue());
        }
        return;
      }
    }
  }

  public UpdateBrakeDiskRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Parameter parameter) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateBrakeDiskRoutine.InputValues(parameter);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateBrakeDiskRoutine with input:");
    	getLogger().trace("   inputValues.parameter: " + inputValues.parameter);
    }
    retrievedValues = new mir.routines.cad2brakesystem.UpdateBrakeDiskRoutine.Match(getExecutionState()).match(inputValues.parameter);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.UpdateBrakeDiskRoutine.Update(getExecutionState()).updateModels(inputValues.parameter, retrievedValues.brakeDisk, getRoutinesFacade());
    return true;
  }
}
