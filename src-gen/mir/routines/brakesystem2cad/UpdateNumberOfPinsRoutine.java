package mir.routines.brakesystem2cad;

import brakesystem.ABSSensor;
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
public class UpdateNumberOfPinsRoutine extends AbstractRoutine {
  private UpdateNumberOfPinsRoutine.InputValues inputValues;

  private UpdateNumberOfPinsRoutine.Match.RetrievedValues retrievedValues;

  private UpdateNumberOfPinsRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final ABSSensor sensor;

    public InputValues(final ABSSensor sensor) {
      this.sensor = sensor;
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

    public EObject getCorrepondenceSourceNamespace(final ABSSensor sensor) {
      return sensor;
    }

    public UpdateNumberOfPinsRoutine.Match.RetrievedValues match(final ABSSensor sensor) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(sensor), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateNumberOfPinsRoutine.Match.RetrievedValues(namespace);
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

    public UpdateNumberOfPinsRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter numericParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new UpdateNumberOfPinsRoutine.Create.CreatedValues(numericParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ABSSensor sensor, final Namespace namespace, final NumericParameter numericParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Number of Pins"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      NumericParameter pNumberOfPins = ((NumericParameter) _findFirst);
      if ((pNumberOfPins == null)) {
        pNumberOfPins = numericParameter;
        pNumberOfPins.setName("Number of Pins");
        pNumberOfPins.setUnit(Unit.COUNT);
        namespace.getParameters().add(pNumberOfPins);
      }
      pNumberOfPins.setValue(sensor.getNumberOfPins());
    }
  }

  public UpdateNumberOfPinsRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final ABSSensor sensor) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateNumberOfPinsRoutine.InputValues(sensor);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateNumberOfPinsRoutine with input:");
    	getLogger().trace("   inputValues.sensor: " + inputValues.sensor);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateNumberOfPinsRoutine.Match(getExecutionState()).match(inputValues.sensor);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateNumberOfPinsRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateNumberOfPinsRoutine.Update(getExecutionState()).updateModels(inputValues.sensor, retrievedValues.namespace, createdValues.numericParameter, getRoutinesFacade());
    return true;
  }
}
