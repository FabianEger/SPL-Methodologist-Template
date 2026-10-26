package mir.routines.brakesystem2cad;

import brakesystem.BrakeCaliper;
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
public class CreateAndRegisterBrakeCaliperRoutine extends AbstractRoutine {
  private CreateAndRegisterBrakeCaliperRoutine.InputValues inputValues;

  private CreateAndRegisterBrakeCaliperRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterBrakeCaliperRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeCaliper caliper;

    public InputValues(final BrakeCaliper caliper) {
      this.caliper = caliper;
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

    public EObject getCorrepondenceSourceMCad(final BrakeCaliper caliper) {
      EObject _eContainer = caliper.eContainer();
      return _eContainer;
    }

    public EObject getCorrepondenceSource1(final BrakeCaliper caliper, final CAD_Model mCad) {
      return caliper;
    }

    public CreateAndRegisterBrakeCaliperRoutine.Match.RetrievedValues match(final BrakeCaliper caliper) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.CAD_Model mCad = getCorrespondingElement(
      	getCorrepondenceSourceMCad(caliper), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.CAD_Model.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mCad == null) {
      	return null;
      }
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(caliper, mCad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.CreateAndRegisterBrakeCaliperRoutine.Match.RetrievedValues(mCad);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Namespace namespace;

      public final StringParameter pOEM_Number;

      public final StringParameter pSpecificationType;

      public final StringParameter pFittingPosition;

      public final NumericParameter pPistonDiameter;

      public final NumericParameter pBrakeDiskThickness;

      public CreatedValues(final Namespace namespace, final StringParameter pOEM_Number, final StringParameter pSpecificationType, final StringParameter pFittingPosition, final NumericParameter pPistonDiameter, final NumericParameter pBrakeDiskThickness) {
        this.namespace = namespace;
        this.pOEM_Number = pOEM_Number;
        this.pSpecificationType = pSpecificationType;
        this.pFittingPosition = pFittingPosition;
        this.pPistonDiameter = pPistonDiameter;
        this.pBrakeDiskThickness = pBrakeDiskThickness;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterBrakeCaliperRoutine.Create.CreatedValues createElements() {
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
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pPistonDiameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pBrakeDiskThickness = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new CreateAndRegisterBrakeCaliperRoutine.Create.CreatedValues(namespace, pOEM_Number, pSpecificationType, pFittingPosition, pPistonDiameter, pBrakeDiskThickness);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeCaliper caliper, final CAD_Model mCad, final Namespace namespace, final StringParameter pOEM_Number, final StringParameter pSpecificationType, final StringParameter pFittingPosition, final NumericParameter pPistonDiameter, final NumericParameter pBrakeDiskThickness, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      namespace.getParameters().add(pSpecificationType);
      pSpecificationType.setName("Specification Type");
      pSpecificationType.setValue(caliper.getSpecificationType());
      namespace.getParameters().add(pOEM_Number);
      pOEM_Number.setName("OEM Number");
      pOEM_Number.setValue(caliper.getOEM_number());
      namespace.getParameters().add(pFittingPosition);
      pFittingPosition.setName("Fitting Position");
      pFittingPosition.setValue(caliper.getFittingPosition());
      namespace.getParameters().add(pPistonDiameter);
      pPistonDiameter.setName("Piston Diameter");
      pPistonDiameter.setValue(caliper.getPistonDiameterInMM());
      pPistonDiameter.setUnit(Unit.MM);
      namespace.getParameters().add(pBrakeDiskThickness);
      pBrakeDiskThickness.setName("Brake Disk Thickness");
      pBrakeDiskThickness.setValue(caliper.getBrakeDiskThickness());
      pBrakeDiskThickness.setUnit(Unit.MM);
      namespace.getParameters().add(pBrakeDiskThickness);
      namespace.setId(caliper.getId());
      String _string = new File("").toString();
      String _plus = (_string + "example.cad");
      this.persistProjectRelative(caliper, namespace, _plus);
      mCad.getNamespaces().add(namespace);
      this.addCorrespondenceBetween(caliper, namespace);
    }
  }

  public CreateAndRegisterBrakeCaliperRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeCaliper caliper) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterBrakeCaliperRoutine.InputValues(caliper);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterBrakeCaliperRoutine with input:");
    	getLogger().trace("   inputValues.caliper: " + inputValues.caliper);
    }
    retrievedValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakeCaliperRoutine.Match(getExecutionState()).match(inputValues.caliper);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakeCaliperRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.CreateAndRegisterBrakeCaliperRoutine.Update(getExecutionState()).updateModels(inputValues.caliper, retrievedValues.mCad, createdValues.namespace, createdValues.pOEM_Number, createdValues.pSpecificationType, createdValues.pFittingPosition, createdValues.pPistonDiameter, createdValues.pBrakeDiskThickness, getRoutinesFacade());
    return true;
  }
}
