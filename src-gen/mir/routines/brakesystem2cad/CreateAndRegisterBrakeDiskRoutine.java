package mir.routines.brakesystem2cad;

import brakesystem.BrakeDisk;
import edu.kit.ipd.sdq.metamodels.cad.BooleanParameter;
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
public class CreateAndRegisterBrakeDiskRoutine extends AbstractRoutine {
  private CreateAndRegisterBrakeDiskRoutine.InputValues inputValues;

  private CreateAndRegisterBrakeDiskRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterBrakeDiskRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeDisk disk;

    public InputValues(final BrakeDisk disk) {
      this.disk = disk;
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

    public EObject getCorrepondenceSourceMCad(final BrakeDisk disk) {
      EObject _eContainer = disk.eContainer();
      return _eContainer;
    }

    public EObject getCorrepondenceSource1(final BrakeDisk disk, final CAD_Model mCad) {
      return disk;
    }

    public CreateAndRegisterBrakeDiskRoutine.Match.RetrievedValues match(final BrakeDisk disk) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.CAD_Model mCad = getCorrespondingElement(
      	getCorrepondenceSourceMCad(disk), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.CAD_Model.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mCad == null) {
      	return null;
      }
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(disk, mCad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.CreateAndRegisterBrakeDiskRoutine.Match.RetrievedValues(mCad);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Namespace namespace;

      public final StringParameter pOEM_Number;

      public final StringParameter pSpecificationType;

      public final StringParameter pFittingPosition;

      public final NumericParameter pDiameter;

      public final NumericParameter pCenteringDiameter;

      public final NumericParameter pRimHoleNumber;

      public final NumericParameter pHoleArrangementNumber;

      public final NumericParameter pBoltHoleCircle;

      public final NumericParameter pBrakeDiskThickness;

      public final NumericParameter pMinimumThickness;

      public final BooleanParameter pVentilated;

      public CreatedValues(final Namespace namespace, final StringParameter pOEM_Number, final StringParameter pSpecificationType, final StringParameter pFittingPosition, final NumericParameter pDiameter, final NumericParameter pCenteringDiameter, final NumericParameter pRimHoleNumber, final NumericParameter pHoleArrangementNumber, final NumericParameter pBoltHoleCircle, final NumericParameter pBrakeDiskThickness, final NumericParameter pMinimumThickness, final BooleanParameter pVentilated) {
        this.namespace = namespace;
        this.pOEM_Number = pOEM_Number;
        this.pSpecificationType = pSpecificationType;
        this.pFittingPosition = pFittingPosition;
        this.pDiameter = pDiameter;
        this.pCenteringDiameter = pCenteringDiameter;
        this.pRimHoleNumber = pRimHoleNumber;
        this.pHoleArrangementNumber = pHoleArrangementNumber;
        this.pBoltHoleCircle = pBoltHoleCircle;
        this.pBrakeDiskThickness = pBrakeDiskThickness;
        this.pMinimumThickness = pMinimumThickness;
        this.pVentilated = pVentilated;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterBrakeDiskRoutine.Create.CreatedValues createElements() {
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
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pDiameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pCenteringDiameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pRimHoleNumber = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pHoleArrangementNumber = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pBoltHoleCircle = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pBrakeDiskThickness = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pMinimumThickness = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.BooleanParameter pVentilated = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBooleanParameter();
      });
      return new CreateAndRegisterBrakeDiskRoutine.Create.CreatedValues(namespace, pOEM_Number, pSpecificationType, pFittingPosition, pDiameter, pCenteringDiameter, pRimHoleNumber, pHoleArrangementNumber, pBoltHoleCircle, pBrakeDiskThickness, pMinimumThickness, pVentilated);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeDisk disk, final CAD_Model mCad, final Namespace namespace, final StringParameter pOEM_Number, final StringParameter pSpecificationType, final StringParameter pFittingPosition, final NumericParameter pDiameter, final NumericParameter pCenteringDiameter, final NumericParameter pRimHoleNumber, final NumericParameter pHoleArrangementNumber, final NumericParameter pBoltHoleCircle, final NumericParameter pBrakeDiskThickness, final NumericParameter pMinimumThickness, final BooleanParameter pVentilated, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      namespace.getParameters().add(pDiameter);
      pDiameter.setName("Diameter");
      pDiameter.setValue(disk.getDiameterInMM());
      pDiameter.setUnit(Unit.MM);
      namespace.getParameters().add(pCenteringDiameter);
      pCenteringDiameter.setName("Centering Diameter");
      pCenteringDiameter.setValue(disk.getCenteringDiameterInMM());
      pCenteringDiameter.setUnit(Unit.MM);
      namespace.getParameters().add(pRimHoleNumber);
      pRimHoleNumber.setName("Rim Hole Number");
      pRimHoleNumber.setValue(disk.getRimHoleNumber());
      pRimHoleNumber.setUnit(Unit.COUNT);
      namespace.getParameters().add(pHoleArrangementNumber);
      pHoleArrangementNumber.setName("Hole Arrangement Number");
      pHoleArrangementNumber.setValue(disk.getHoleArrangementNumber());
      namespace.getParameters().add(pBoltHoleCircle);
      pBoltHoleCircle.setName("Bolt Hole Circle");
      pBoltHoleCircle.setValue(disk.getBoltHoleCircleInMM());
      pBoltHoleCircle.setUnit(Unit.MM);
      namespace.getParameters().add(pBrakeDiskThickness);
      pBrakeDiskThickness.setName("Brake Disk Thickness");
      pBrakeDiskThickness.setValue(disk.getBrakeDiskThicknessInMM());
      pBrakeDiskThickness.setUnit(Unit.MM);
      namespace.getParameters().add(pMinimumThickness);
      pMinimumThickness.setName("Minimum Thickness");
      pMinimumThickness.setValue(disk.getMinimumThicknessInMM());
      pMinimumThickness.setUnit(Unit.MM);
      namespace.getParameters().add(pVentilated);
      pVentilated.setName("Ventilated");
      pVentilated.setValue(disk.isVentilated());
      namespace.getParameters().add(pOEM_Number);
      pOEM_Number.setName("OEM Number");
      pOEM_Number.setValue(disk.getOEM_number());
      namespace.getParameters().add(pSpecificationType);
      pSpecificationType.setName("Specification Type");
      pSpecificationType.setValue(disk.getSpecificationType());
      namespace.getParameters().add(pFittingPosition);
      pFittingPosition.setName("Fitting Position");
      pFittingPosition.setValue(disk.getFittingPosition());
      namespace.setId(disk.getId());
      String _string = new File("").toString();
      String _plus = (_string + "example.cad");
      this.persistProjectRelative(disk, namespace, _plus);
      mCad.getNamespaces().add(namespace);
      this.addCorrespondenceBetween(disk, namespace);
    }
  }

  public CreateAndRegisterBrakeDiskRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeDisk disk) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterBrakeDiskRoutine.InputValues(disk);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterBrakeDiskRoutine with input:");
    	getLogger().trace("   inputValues.disk: " + inputValues.disk);
    }
    retrievedValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakeDiskRoutine.Match(getExecutionState()).match(inputValues.disk);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakeDiskRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.CreateAndRegisterBrakeDiskRoutine.Update(getExecutionState()).updateModels(inputValues.disk, retrievedValues.mCad, createdValues.namespace, createdValues.pOEM_Number, createdValues.pSpecificationType, createdValues.pFittingPosition, createdValues.pDiameter, createdValues.pCenteringDiameter, createdValues.pRimHoleNumber, createdValues.pHoleArrangementNumber, createdValues.pBoltHoleCircle, createdValues.pBrakeDiskThickness, createdValues.pMinimumThickness, createdValues.pVentilated, getRoutinesFacade());
    return true;
  }
}
