package mir.routines.brakesystem2cad;

import brakesystem.BrakeHose;
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
public class CreateAndRegisterBrakeHoseRoutine extends AbstractRoutine {
  private CreateAndRegisterBrakeHoseRoutine.InputValues inputValues;

  private CreateAndRegisterBrakeHoseRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterBrakeHoseRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeHose hose;

    public InputValues(final BrakeHose hose) {
      this.hose = hose;
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

    public EObject getCorrepondenceSourceMCad(final BrakeHose hose) {
      EObject _eContainer = hose.eContainer();
      return _eContainer;
    }

    public EObject getCorrepondenceSource1(final BrakeHose hose, final CAD_Model mCad) {
      return hose;
    }

    public CreateAndRegisterBrakeHoseRoutine.Match.RetrievedValues match(final BrakeHose hose) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.CAD_Model mCad = getCorrespondingElement(
      	getCorrepondenceSourceMCad(hose), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.CAD_Model.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (mCad == null) {
      	return null;
      }
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(hose, mCad), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.CreateAndRegisterBrakeHoseRoutine.Match.RetrievedValues(mCad);
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final Namespace namespace;

      public final StringParameter pSpecificationType;

      public final StringParameter pOEM_Number;

      public final StringParameter pFittingPosition;

      public final StringParameter pThreadSize1;

      public final StringParameter pThreadSize2;

      public final NumericParameter pLength;

      public CreatedValues(final Namespace namespace, final StringParameter pSpecificationType, final StringParameter pOEM_Number, final StringParameter pFittingPosition, final StringParameter pThreadSize1, final StringParameter pThreadSize2, final NumericParameter pLength) {
        this.namespace = namespace;
        this.pSpecificationType = pSpecificationType;
        this.pOEM_Number = pOEM_Number;
        this.pFittingPosition = pFittingPosition;
        this.pThreadSize1 = pThreadSize1;
        this.pThreadSize2 = pThreadSize2;
        this.pLength = pLength;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterBrakeHoseRoutine.Create.CreatedValues createElements() {
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
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pThreadSize1 = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.StringParameter pThreadSize2 = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter pLength = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new CreateAndRegisterBrakeHoseRoutine.Create.CreatedValues(namespace, pSpecificationType, pOEM_Number, pFittingPosition, pThreadSize1, pThreadSize2, pLength);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeHose hose, final CAD_Model mCad, final Namespace namespace, final StringParameter pSpecificationType, final StringParameter pOEM_Number, final StringParameter pFittingPosition, final StringParameter pThreadSize1, final StringParameter pThreadSize2, final NumericParameter pLength, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      namespace.getParameters().add(pSpecificationType);
      pSpecificationType.setName("Specification Type");
      pSpecificationType.setValue(hose.getSpecificationType());
      namespace.getParameters().add(pOEM_Number);
      pOEM_Number.setName("OEM Number");
      pOEM_Number.setValue(hose.getOEM_number());
      namespace.getParameters().add(pFittingPosition);
      pFittingPosition.setName("Fitting Position");
      pFittingPosition.setValue(hose.getFittingPosition());
      namespace.getParameters().add(pThreadSize1);
      pThreadSize1.setName("Thread 1");
      pThreadSize1.setValue(hose.getThreadSize1());
      namespace.getParameters().add(pThreadSize2);
      pThreadSize2.setName("Thread 2");
      pThreadSize2.setValue(hose.getThreadSize2());
      namespace.getParameters().add(pLength);
      pLength.setName("Length");
      pLength.setValue(hose.getLengthInMM());
      pLength.setUnit(Unit.MM);
      namespace.setId(hose.getId());
      String _string = new File("").toString();
      String _plus = (_string + "example.cad");
      this.persistProjectRelative(hose, namespace, _plus);
      mCad.getNamespaces().add(namespace);
      this.addCorrespondenceBetween(hose, namespace);
    }
  }

  public CreateAndRegisterBrakeHoseRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeHose hose) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterBrakeHoseRoutine.InputValues(hose);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterBrakeHoseRoutine with input:");
    	getLogger().trace("   inputValues.hose: " + inputValues.hose);
    }
    retrievedValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakeHoseRoutine.Match(getExecutionState()).match(inputValues.hose);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.CreateAndRegisterBrakeHoseRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.CreateAndRegisterBrakeHoseRoutine.Update(getExecutionState()).updateModels(inputValues.hose, retrievedValues.mCad, createdValues.namespace, createdValues.pSpecificationType, createdValues.pOEM_Number, createdValues.pFittingPosition, createdValues.pThreadSize1, createdValues.pThreadSize2, createdValues.pLength, getRoutinesFacade());
    return true;
  }
}
