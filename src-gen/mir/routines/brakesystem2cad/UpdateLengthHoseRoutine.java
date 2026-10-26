package mir.routines.brakesystem2cad;

import brakesystem.BrakeHose;
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
public class UpdateLengthHoseRoutine extends AbstractRoutine {
  private UpdateLengthHoseRoutine.InputValues inputValues;

  private UpdateLengthHoseRoutine.Match.RetrievedValues retrievedValues;

  private UpdateLengthHoseRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeHose hose;

    public InputValues(final BrakeHose hose) {
      this.hose = hose;
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

    public EObject getCorrepondenceSourceNamespace(final BrakeHose hose) {
      return hose;
    }

    public UpdateLengthHoseRoutine.Match.RetrievedValues match(final BrakeHose hose) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(hose), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateLengthHoseRoutine.Match.RetrievedValues(namespace);
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

    public UpdateLengthHoseRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter numericParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new UpdateLengthHoseRoutine.Create.CreatedValues(numericParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeHose hose, final Namespace namespace, final NumericParameter numericParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Length"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      NumericParameter pLength = ((NumericParameter) _findFirst);
      if ((pLength == null)) {
        pLength = numericParameter;
        pLength.setName("Length");
        pLength.setUnit(Unit.MM);
        namespace.getParameters().add(pLength);
      }
      pLength.setValue(hose.getLengthInMM());
    }
  }

  public UpdateLengthHoseRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeHose hose) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateLengthHoseRoutine.InputValues(hose);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateLengthHoseRoutine with input:");
    	getLogger().trace("   inputValues.hose: " + inputValues.hose);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateLengthHoseRoutine.Match(getExecutionState()).match(inputValues.hose);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateLengthHoseRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateLengthHoseRoutine.Update(getExecutionState()).updateModels(inputValues.hose, retrievedValues.namespace, createdValues.numericParameter, getRoutinesFacade());
    return true;
  }
}
