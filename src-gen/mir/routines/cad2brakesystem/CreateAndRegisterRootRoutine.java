package mir.routines.cad2brakesystem;

import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import java.io.File;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateAndRegisterRootRoutine extends AbstractRoutine {
  private CreateAndRegisterRootRoutine.InputValues inputValues;

  private CreateAndRegisterRootRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterRootRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final CAD_Model model;

    public InputValues(final CAD_Model model) {
      this.model = model;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public RetrievedValues() {
        
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSource1(final CAD_Model model) {
      return model;
    }

    public CreateAndRegisterRootRoutine.Match.RetrievedValues match(final CAD_Model model) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(model), // correspondence source supplier
      	brakesystem.Brakesystem.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.cad2brakesystem.CreateAndRegisterRootRoutine.Match.RetrievedValues();
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Brakesystem mBrake;

      public CreatedValues(final Brakesystem mBrake) {
        this.mBrake = mBrake;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterRootRoutine.Create.CreatedValues createElements() {
      brakesystem.Brakesystem mBrake = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBrakesystem();
      });
      return new CreateAndRegisterRootRoutine.Create.CreatedValues(mBrake);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final CAD_Model model, final Brakesystem mBrake, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      String _string = new File("").toString();
      String _plus = (_string + "brake.bs");
      this.persistProjectRelative(model, mBrake, _plus);
      this.addCorrespondenceBetween(model, mBrake);
    }
  }

  public CreateAndRegisterRootRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final CAD_Model model) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterRootRoutine.InputValues(model);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterRootRoutine with input:");
    	getLogger().trace("   inputValues.model: " + inputValues.model);
    }
    retrievedValues = new mir.routines.cad2brakesystem.CreateAndRegisterRootRoutine.Match(getExecutionState()).match(inputValues.model);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.cad2brakesystem.CreateAndRegisterRootRoutine.Create(getExecutionState()).createElements();
    new mir.routines.cad2brakesystem.CreateAndRegisterRootRoutine.Update(getExecutionState()).updateModels(inputValues.model, createdValues.mBrake, getRoutinesFacade());
    return true;
  }
}
