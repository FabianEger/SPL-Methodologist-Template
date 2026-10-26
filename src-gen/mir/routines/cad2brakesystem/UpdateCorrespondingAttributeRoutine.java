package mir.routines.cad2brakesystem;

import brakesystem.ABSSensor;
import brakesystem.BrakeCaliper;
import brakesystem.BrakeComponent;
import brakesystem.BrakeDisk;
import brakesystem.BrakeHose;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import java.io.IOException;
import java.util.Objects;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateCorrespondingAttributeRoutine extends AbstractRoutine {
  private UpdateCorrespondingAttributeRoutine.InputValues inputValues;

  private UpdateCorrespondingAttributeRoutine.Match.RetrievedValues retrievedValues;

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

    public UpdateCorrespondingAttributeRoutine.Match.RetrievedValues match(final Parameter parameter) throws IOException {
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
      return new mir.routines.cad2brakesystem.UpdateCorrespondingAttributeRoutine.Match.RetrievedValues(mBrakeComponent);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Parameter parameter, final BrakeComponent mBrakeComponent, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      final String name = parameter.getName();
      if (((Objects.equals(name, "Specification Type") || Objects.equals(name, "OEM Number")) || Objects.equals(name, "Fitting Position"))) {
        _routinesFacade.updateBrakeComponent(parameter);
        return;
      }
      if ((mBrakeComponent instanceof ABSSensor)) {
        _routinesFacade.updateABSSensor(parameter);
      } else {
        if ((mBrakeComponent instanceof BrakeCaliper)) {
          _routinesFacade.updateBrakeCaliper(parameter);
        } else {
          if ((mBrakeComponent instanceof BrakeHose)) {
            _routinesFacade.updateBrakeHose(parameter);
          } else {
            if ((mBrakeComponent instanceof BrakeDisk)) {
              _routinesFacade.updateBrakeDisk(parameter);
            }
          }
        }
      }
    }
  }

  public UpdateCorrespondingAttributeRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Parameter parameter) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateCorrespondingAttributeRoutine.InputValues(parameter);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateCorrespondingAttributeRoutine with input:");
    	getLogger().trace("   inputValues.parameter: " + inputValues.parameter);
    }
    retrievedValues = new mir.routines.cad2brakesystem.UpdateCorrespondingAttributeRoutine.Match(getExecutionState()).match(inputValues.parameter);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.cad2brakesystem.UpdateCorrespondingAttributeRoutine.Update(getExecutionState()).updateModels(inputValues.parameter, retrievedValues.mBrakeComponent, getRoutinesFacade());
    return true;
  }
}
