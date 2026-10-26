package mir.routines.brakesystem2cad;

import brakesystem.BrakeDisk;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import edu.kit.ipd.sdq.metamodels.cad.NumericParameter;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import edu.kit.ipd.sdq.metamodels.cad.Unit;
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
public class UpdateRimHoleNumberRoutine extends AbstractRoutine {
  private UpdateRimHoleNumberRoutine.InputValues inputValues;

  private UpdateRimHoleNumberRoutine.Match.RetrievedValues retrievedValues;

  private UpdateRimHoleNumberRoutine.Create.CreatedValues createdValues;

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

    public UpdateRimHoleNumberRoutine.Match.RetrievedValues match(final BrakeDisk disk) throws IOException {
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
      return new mir.routines.brakesystem2cad.UpdateRimHoleNumberRoutine.Match.RetrievedValues(namespace);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final NumericParameter numericParameter;

      public CreatedValues(final NumericParameter numericParameter) {
        this.numericParameter = numericParameter;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public UpdateRimHoleNumberRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter numericParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new UpdateRimHoleNumberRoutine.Create.CreatedValues(numericParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeDisk disk, final Namespace namespace, final NumericParameter numericParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Rim Hole Number"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      NumericParameter pRimHoleNumber = ((NumericParameter) _findFirst);
      if ((pRimHoleNumber == null)) {
        pRimHoleNumber = numericParameter;
        pRimHoleNumber.setName("Rim Hole Number");
        pRimHoleNumber.setUnit(Unit.COUNT);
        namespace.getParameters().add(pRimHoleNumber);
      }
      pRimHoleNumber.setValue(disk.getRimHoleNumber());
    }
  }

  public UpdateRimHoleNumberRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeDisk disk) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateRimHoleNumberRoutine.InputValues(disk);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateRimHoleNumberRoutine with input:");
    	getLogger().trace("   inputValues.disk: " + inputValues.disk);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateRimHoleNumberRoutine.Match(getExecutionState()).match(inputValues.disk);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateRimHoleNumberRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateRimHoleNumberRoutine.Update(getExecutionState()).updateModels(inputValues.disk, retrievedValues.namespace, createdValues.numericParameter, getRoutinesFacade());
    return true;
  }
}
