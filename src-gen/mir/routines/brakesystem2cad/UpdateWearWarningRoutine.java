package mir.routines.brakesystem2cad;

import brakesystem.BrakePad;
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
public class UpdateWearWarningRoutine extends AbstractRoutine {
  private UpdateWearWarningRoutine.InputValues inputValues;

  private UpdateWearWarningRoutine.Match.RetrievedValues retrievedValues;

  private UpdateWearWarningRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakePad pad;

    public InputValues(final BrakePad pad) {
      this.pad = pad;
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

    public EObject getCorrepondenceSourceNamespace(final BrakePad pad) {
      return pad;
    }

    public UpdateWearWarningRoutine.Match.RetrievedValues match(final BrakePad pad) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(pad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateWearWarningRoutine.Match.RetrievedValues(namespace);
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

    public UpdateWearWarningRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.BooleanParameter booleanParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBooleanParameter();
      });
      return new UpdateWearWarningRoutine.Create.CreatedValues(booleanParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakePad pad, final Namespace namespace, final BooleanParameter booleanParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Wear Warning"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      BooleanParameter pWearWarning = ((BooleanParameter) _findFirst);
      if ((pWearWarning == null)) {
        pWearWarning = booleanParameter;
        pWearWarning.setName("Wear Warning");
        namespace.getParameters().add(pWearWarning);
      }
      pWearWarning.setValue(pad.isWearWarning());
    }
  }

  public UpdateWearWarningRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakePad pad) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateWearWarningRoutine.InputValues(pad);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateWearWarningRoutine with input:");
    	getLogger().trace("   inputValues.pad: " + inputValues.pad);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateWearWarningRoutine.Match(getExecutionState()).match(inputValues.pad);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateWearWarningRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateWearWarningRoutine.Update(getExecutionState()).updateModels(inputValues.pad, retrievedValues.namespace, createdValues.booleanParameter, getRoutinesFacade());
    return true;
  }
}
