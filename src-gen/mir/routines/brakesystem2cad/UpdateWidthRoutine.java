package mir.routines.brakesystem2cad;

import brakesystem.BrakePad;
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
public class UpdateWidthRoutine extends AbstractRoutine {
  private UpdateWidthRoutine.InputValues inputValues;

  private UpdateWidthRoutine.Match.RetrievedValues retrievedValues;

  private UpdateWidthRoutine.Create.CreatedValues createdValues;

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

    public UpdateWidthRoutine.Match.RetrievedValues match(final BrakePad pad) throws IOException {
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
      return new mir.routines.brakesystem2cad.UpdateWidthRoutine.Match.RetrievedValues(namespace);
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

    public UpdateWidthRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter numericParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new UpdateWidthRoutine.Create.CreatedValues(numericParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakePad pad, final Namespace namespace, final NumericParameter numericParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Width"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      NumericParameter pWidth = ((NumericParameter) _findFirst);
      if ((pWidth == null)) {
        pWidth = numericParameter;
        pWidth.setName("Width");
        pWidth.setUnit(Unit.MM);
        namespace.getParameters().add(pWidth);
      }
      pWidth.setValue(pad.getWidthInMM());
    }
  }

  public UpdateWidthRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakePad pad) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateWidthRoutine.InputValues(pad);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateWidthRoutine with input:");
    	getLogger().trace("   inputValues.pad: " + inputValues.pad);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateWidthRoutine.Match(getExecutionState()).match(inputValues.pad);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateWidthRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateWidthRoutine.Update(getExecutionState()).updateModels(inputValues.pad, retrievedValues.namespace, createdValues.numericParameter, getRoutinesFacade());
    return true;
  }
}
