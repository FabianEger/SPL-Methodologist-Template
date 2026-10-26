package mir.routines.brakesystem2cad;

import brakesystem.ABSSensor;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import edu.kit.ipd.sdq.metamodels.cad.StringParameter;
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
public class UpdateSensorTypeRoutine extends AbstractRoutine {
  private UpdateSensorTypeRoutine.InputValues inputValues;

  private UpdateSensorTypeRoutine.Match.RetrievedValues retrievedValues;

  private UpdateSensorTypeRoutine.Create.CreatedValues createdValues;

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

    public UpdateSensorTypeRoutine.Match.RetrievedValues match(final ABSSensor sensor) throws IOException {
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
      return new mir.routines.brakesystem2cad.UpdateSensorTypeRoutine.Match.RetrievedValues(namespace);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final StringParameter stringParameter;

      public CreatedValues(final StringParameter stringParameter) {
        this.stringParameter = stringParameter;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public UpdateSensorTypeRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.StringParameter stringParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      return new UpdateSensorTypeRoutine.Create.CreatedValues(stringParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ABSSensor sensor, final Namespace namespace, final StringParameter stringParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Sensor Type"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      StringParameter pSensorType = ((StringParameter) _findFirst);
      if ((pSensorType == null)) {
        pSensorType = stringParameter;
        pSensorType.setName("Sensor Type");
        namespace.getParameters().add(pSensorType);
      }
      pSensorType.setValue(sensor.getSensorType());
    }
  }

  public UpdateSensorTypeRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final ABSSensor sensor) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateSensorTypeRoutine.InputValues(sensor);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateSensorTypeRoutine with input:");
    	getLogger().trace("   inputValues.sensor: " + inputValues.sensor);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateSensorTypeRoutine.Match(getExecutionState()).match(inputValues.sensor);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateSensorTypeRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateSensorTypeRoutine.Update(getExecutionState()).updateModels(inputValues.sensor, retrievedValues.namespace, createdValues.stringParameter, getRoutinesFacade());
    return true;
  }
}
