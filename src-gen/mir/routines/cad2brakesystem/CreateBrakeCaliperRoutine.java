package mir.routines.cad2brakesystem;

import brakesystem.BrakeCaliper;
import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateBrakeCaliperRoutine extends AbstractRoutine {
  private CreateBrakeCaliperRoutine.InputValues inputValues;

  private CreateBrakeCaliperRoutine.Create.CreatedValues createdValues;

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
      public final BrakeCaliper component;

      public CreatedValues(final BrakeCaliper component) {
        this.component = component;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateBrakeCaliperRoutine.Create.CreatedValues createElements() {
      brakesystem.BrakeCaliper component = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBrakeCaliper();
      });
      return new CreateBrakeCaliperRoutine.Create.CreatedValues(component);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Namespace namespace, final Brakesystem mBrake, final BrakeCaliper component, @Extension final Cad2brakesystemRoutinesFacade _routinesFacade) {
      this.addCorrespondenceBetween(namespace, component);
      mBrake.getBrakeComponents().add(component);
    }
  }

  public CreateBrakeCaliperRoutine(final Cad2brakesystemRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Namespace namespace, final Brakesystem mBrake) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateBrakeCaliperRoutine.InputValues(namespace, mBrake);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateBrakeCaliperRoutine with input:");
    	getLogger().trace("   inputValues.namespace: " + inputValues.namespace);
    	getLogger().trace("   inputValues.mBrake: " + inputValues.mBrake);
    }
    // This execution step is empty
    createdValues = new mir.routines.cad2brakesystem.CreateBrakeCaliperRoutine.Create(getExecutionState()).createElements();
    new mir.routines.cad2brakesystem.CreateBrakeCaliperRoutine.Update(getExecutionState()).updateModels(inputValues.namespace, inputValues.mBrake, createdValues.component, getRoutinesFacade());
    return true;
  }
}
