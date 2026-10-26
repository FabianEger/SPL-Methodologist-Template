package mir.routines.brakesystem2cad;

import brakesystem.ABSSensor;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import edu.kit.ipd.sdq.metamodels.cad.NumericParameter;
import edu.kit.ipd.sdq.metamodels.cad.StringParameter;
import edu.kit.ipd.sdq.metamodels.cad.Unit;
import java.io.File;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateAndRegisterABSSensorRoutine extends AbstractRoutine {
  private CreateAndRegisterABSSensorRoutine.InputValues inputValues;

  private CreateAndRegisterABSSensorRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterABSSensorRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final ABSSensor sensor;

    public InputValues(final ABSSensor sensor) {
      this.sensor = sensor;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final CAD_Model mCad;

      public RetrievedValues(final CAD_Model mCad) {
        this.mCad = mCad;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSourceMCad(final ABSSensor sensor) {
      EObject _eContainer = sensor.eContainer();
      return _eContainer;
    }

    public EObject getCorrepondenceSource1(final ABSSensor sensor, final CAD_Model mCad) {
      return sensor;
    }

    public CreateAndRegisterABSSensorRoutine.Match.RetrievedValues match(final ABSSensor sensor) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.CAD_Model mCad = getCorrespondingElement(
      	getCorrepondenceSourceMCad(sensor), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.CAD_Model.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mCad == null) {
      	return null;
      }
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(sensor, mCad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.CreateAndRegisterABSSensorRoutine.Match.RetrievedValues(mCad);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Namespace namespace;

      public final StringParameter pOEM_Number;

      public final StringParameter pSpecificationType;

      public final StringParameter pFittingPosition;

      public final StringParameter pSensorType;

      public final NumericParameter pFittingDepth;

      public final NumericParameter pNumberOfPins;

      public final NumericParameter pLength;

      public CreatedValues(final Namespace namespace, final StringParameter pOEM_Number, final StringParameter pSpecificationType, final StringParameter pFittingPosition, final StringParameter pSensorType, final NumericParameter pFittingDepth, final NumericParameter pNumberOfPins, final NumericParameter pLength) {
        this.namespace = namespace;
        this.pOEM_Number = pOEM_Number;
        this.pSpecificationType = pSpecificationType;
        this.pFittingPosition = pFittingPosition;
        this.pSensorType = pSensorType;
        this.pFittingDepth = pFittingDepth;
        this.pNumberOfPins = pNumberOfPins;
        this.pLength = pLength;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterABSSensorRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNamespace();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pOEM_Number = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pSpecificationType = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pFittingPosition = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pSensorType = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pFittingDepth = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pNumberOfPins = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pLength = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new CreateAndRegisterABSSensorRoutine.Create.CreatedValues(namespace, pOEM_Number, pSpecificationType, pFittingPosition, pSensorType, pFittingDepth, pNumberOfPins, pLength);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final ABSSensor sensor, final CAD_Model mCad, final Namespace namespace, final StringParameter pOEM_Number, final StringParameter pSpecificationType, final StringParameter pFittingPosition, final StringParameter pSensorType, final NumericParameter pFittingDepth, final NumericParameter pNumberOfPins, final NumericParameter pLength, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      namespace.getParameters().add(pOEM_Number);
      pOEM_Number.setName("OEM Number");
      pOEM_Number.setValue(sensor.getOEM_number());
      namespace.getParameters().add(pSpecificationType);
      pSpecificationType.setName("Specification Type");
      pSpecificationType.setValue(sensor.getSpecificationType());
      namespace.getParameters().add(pFittingPosition);
      pFittingPosition.setName("Fitting Position");
      pFittingPosition.setValue(sensor.getFittingPosition());
      namespace.getParameters().add(pSensorType);
      pSensorType.setName("Sensor Type");
      pSensorType.setValue(sensor.getSensorType());
      namespace.getParameters().add(pFittingDepth);
      pFittingDepth.setName("Fitting Depth");
      pFittingDepth.setValue(sensor.getFittingDepth());
      pFittingDepth.setUnit(Unit.MM);
      namespace.getParameters().add(pNumberOfPins);
      pNumberOfPins.setName("Number of Pins");
      pNumberOfPins.setValue(sensor.getNumberOfPins());
      pNumberOfPins.setUnit(Unit.COUNT);
      namespace.getParameters().add(pLength);
      pLength.setName("Length");
      pLength.setValue(sensor.getLengthInMM());
      pLength.setUnit(Unit.MM);
      namespace.setId(sensor.getId());
      String _string = new File("").toString();
      String _plus = (_string + "example.cad");
      this.persistProjectRelative(sensor, namespace, _plus);
      mCad.getNamespaces().add(namespace);
      this.addCorrespondenceBetween(sensor, namespace);
    }
  }

  public CreateAndRegisterABSSensorRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final ABSSensor sensor) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterABSSensorRoutine.InputValues(sensor);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterABSSensorRoutine with input:");
    	getLogger().trace("   inputValues.sensor: " + inputValues.sensor);
    }
    retrievedValues = new mir.routines.brakesystem2cad.CreateAndRegisterABSSensorRoutine.Match(getExecutionState()).match(inputValues.sensor);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.CreateAndRegisterABSSensorRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.CreateAndRegisterABSSensorRoutine.Update(getExecutionState()).updateModels(inputValues.sensor, retrievedValues.mCad, createdValues.namespace, createdValues.pOEM_Number, createdValues.pSpecificationType, createdValues.pFittingPosition, createdValues.pSensorType, createdValues.pFittingDepth, createdValues.pNumberOfPins, createdValues.pLength, getRoutinesFacade());
    return true;
  }
}
