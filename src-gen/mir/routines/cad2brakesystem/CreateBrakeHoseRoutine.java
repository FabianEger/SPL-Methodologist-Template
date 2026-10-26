package mir.routines.cad2brakesystem;

import brakesystem.BrakeHose;
import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateBrakeHoseRoutine extends AbstractRoutine {
  private CreateBrakeHoseRoutine.InputValues inputValues;

  private CreateBrakeHoseRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final Namespace namespace;

    public final Brakesystem mBrake;

    public InputValues(final Namespace namespace, final Brakesystem mBrake) {
      this.namespace = namespace;
      this.mBrake = mBrake;
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final BrakeHose component;

      public CreatedValues(final BrakeHose component) {
        this.component = component;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateBrakeHoseRoutine.Create.CreatedValues createElements() {
      brakesystem.BrakeHose component = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBrakeHose();
      });
      return new CreateBrakeHoseRoutine.Create.CreatedValues(component);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Namespace namespace, final Brakesystem mBrake, final BrakeHose component, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      this.addCorrespondenceBetween(namespace, component);
      mBrake.getBrakeComponents().add(component);
    }
  }

  public CreateBrakeHoseRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Namespace namespace, final Brakesystem mBrake) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateBrakeHoseRoutine.InputValues(namespace, mBrake);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateBrakeHoseRoutine with input:");
    	getLogger().trace("   inputValues.namespace: " + inputValues.namespace);
    	getLogger().trace("   inputValues.mBrake: " + inputValues.mBrake);
    }
    // This execution step is empty
    createdValues = new mir.routines.cad2brakesystem.CreateBrakeHoseRoutine.Create(getExecutionState()).createElements();
    new mir.routines.cad2brakesystem.CreateBrakeHoseRoutine.Update(getExecutionState()).updateModels(inputValues.namespace, inputValues.mBrake, createdValues.component, getRoutinesFacade());
    return true;
  }
}
