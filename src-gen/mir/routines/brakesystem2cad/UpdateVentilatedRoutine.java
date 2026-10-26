package mir.routines.brakesystem2cad;

import brakesystem.BrakeDisk;
import edu.kit.ipd.sdq.metamodels.cad.BooleanParameter;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import java.io.IOException;
import java.util.Objects;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class UpdateVentilatedRoutine extends AbstractRoutine {
  private UpdateVentilatedRoutine.InputValues inputValues;

  private UpdateVentilatedRoutine.Match.RetrievedValues retrievedValues;

  private UpdateVentilatedRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeDisk disk;

    public InputValues(final BrakeDisk disk) {
      this.disk = disk;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final Namespace namespace;

      public RetrievedValues(final Namespace namespace) {
        this.namespace = namespace;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceNamespace(final BrakeDisk disk) {
      return disk;
    }

    public UpdateVentilatedRoutine.Match.RetrievedValues match(final BrakeDisk disk) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(disk), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateVentilatedRoutine.Match.RetrievedValues(namespace);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final BooleanParameter booleanParameter;

      public CreatedValues(final BooleanParameter booleanParameter) {
        this.booleanParameter = booleanParameter;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public UpdateVentilatedRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.BooleanParameter booleanParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBooleanParameter();
      });
      return new UpdateVentilatedRoutine.Create.CreatedValues(booleanParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeDisk disk, final Namespace namespace, final BooleanParameter booleanParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Ventilated"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      BooleanParameter pVentilated = ((BooleanParameter) _findFirst);
      if ((pVentilated == null)) {
        pVentilated = booleanParameter;
        pVentilated.setName("Ventilated");
        namespace.getParameters().add(pVentilated);
      }
      pVentilated.setValue(disk.isVentilated());
    }
  }

  public UpdateVentilatedRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeDisk disk) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateVentilatedRoutine.InputValues(disk);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateVentilatedRoutine with input:");
    	getLogger().trace("   inputValues.disk: " + inputValues.disk);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateVentilatedRoutine.Match(getExecutionState()).match(inputValues.disk);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateVentilatedRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateVentilatedRoutine.Update(getExecutionState()).updateModels(inputValues.disk, retrievedValues.namespace, createdValues.booleanParameter, getRoutinesFacade());
    return true;
  }
}
