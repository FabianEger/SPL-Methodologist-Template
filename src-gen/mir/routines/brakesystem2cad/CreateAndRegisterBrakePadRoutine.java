package mir.routines.brakesystem2cad;

import brakesystem.BrakePad;
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
public class CreateAndRegisterBrakePadRoutine extends AbstractRoutine {
  private CreateAndRegisterBrakePadRoutine.InputValues inputValues;

  private CreateAndRegisterBrakePadRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterBrakePadRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakePad pad;

    public InputValues(final BrakePad pad) {
      this.pad = pad;
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

    public EObject getCorrepondenceSourceMCad(final BrakePad pad) {
      EObject _eContainer = pad.eContainer();
      return _eContainer;
    }

    public EObject getCorrepondenceSource1(final BrakePad pad, final CAD_Model mCad) {
      return pad;
    }

    public CreateAndRegisterBrakePadRoutine.Match.RetrievedValues match(final BrakePad pad) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.CAD_Model mCad = getCorrespondingElement(
      	getCorrepondenceSourceMCad(pad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.CAD_Model.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mCad == null) {
      	return null;
      }
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(pad, mCad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.CreateAndRegisterBrakePadRoutine.Match.RetrievedValues(mCad);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Namespace namespace;

      public final StringParameter pSpecificationType;

      public final StringParameter pOEM_Number;

      public final StringParameter pFittingPosition;

      public final BooleanParameter pWearWarning;

      public final NumericParameter pHeight;

      public final NumericParameter pWidth;

      public final NumericParameter pThickness;

      public CreatedValues(final Namespace namespace, final StringParameter pSpecificationType, final StringParameter pOEM_Number, final StringParameter pFittingPosition, final BooleanParameter pWearWarning, final NumericParameter pHeight, final NumericParameter pWidth, final NumericParameter pThickness) {
        this.namespace = namespace;
        this.pSpecificationType = pSpecificationType;
        this.pOEM_Number = pOEM_Number;
        this.pFittingPosition = pFittingPosition;
        this.pWearWarning = pWearWarning;
        this.pHeight = pHeight;
        this.pWidth = pWidth;
        this.pThickness = pThickness;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterBrakePadRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNamespace();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pSpecificationType = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pOEM_Number = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pFittingPosition = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.BooleanParameter pWearWarning = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createBooleanParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pHeight = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pWidth = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pThickness = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new CreateAndRegisterBrakePadRoutine.Create.CreatedValues(namespace, pSpecificationType, pOEM_Number, pFittingPosition, pWearWarning, pHeight, pWidth, pThickness);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakePad pad, final CAD_Model mCad, final Namespace namespace, final StringParameter pSpecificationType, final StringParameter pOEM_Number, final StringParameter pFittingPosition, final BooleanParameter pWearWarning, final NumericParameter pHeight, final NumericParameter pWidth, final NumericParameter pThickness, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      namespace.getParameters().add(pSpecificationType);
      pSpecificationType.setName("Specification Type");
      pSpecificationType.setValue(pad.getSpecificationType());
      namespace.getParameters().add(pOEM_Number);
      pOEM_Number.setName("OEM Number");
      pOEM_Number.setValue(pad.getOEM_number());
      namespace.getParameters().add(pFittingPosition);
      pFittingPosition.setName("Fitting Position");
      pFittingPosition.setValue(pad.getFittingPosition());
      namespace.getParameters().add(pWearWarning);
      pWearWarning.setName("Wear Warning");
      pWearWarning.setValue(pad.isWearWarning());
      namespace.getParameters().add(pHeight);
      pHeight.setName("Height");
      pHeight.setValue(pad.getHeightInMM());
      pHeight.setUnit(Unit.MM);
      namespace.getParameters().add(pWidth);
      pWidth.setName("Width");
      pWidth.setValue(pad.getWidthInMM());
      pWidth.setUnit(Unit.MM);
      namespace.getParameters().add(pThickness);
      pThickness.setName("Thickness");
      pThickness.setValue(pad.getThicknessInMM());
      pThickness.setUnit(Unit.MM);
      namespace.setId(pad.getId());
      String _string = new File("").toString();
      String _plus = (_string + "example.cad");
      this.persistProjectRelative(pad, namespace, _plus);
      mCad.getNamespaces().add(namespace);
      this.addCorrespondenceBetween(pad, namespace);
    }
  }

  public CreateAndRegisterBrakePadRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakePad pad) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterBrakePadRoutine.InputValues(pad);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterBrakePadRoutine with input:");
    	getLogger().trace("   inputValues.pad: " + inputValues.pad);
    }
    retrievedValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakePadRoutine.Match(getExecutionState()).match(inputValues.pad);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakePadRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.CreateAndRegisterBrakePadRoutine.Update(getExecutionState()).updateModels(inputValues.pad, retrievedValues.mCad, createdValues.namespace, createdValues.pSpecificationType, createdValues.pOEM_Number, createdValues.pFittingPosition, createdValues.pWearWarning, createdValues.pHeight, createdValues.pWidth, createdValues.pThickness, getRoutinesFacade());
    return true;
  }
}
