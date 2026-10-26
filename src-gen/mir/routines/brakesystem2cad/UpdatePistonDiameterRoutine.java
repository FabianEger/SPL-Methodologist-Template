package mir.routines.brakesystem2cad;

import brakesystem.BrakeCaliper;
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
public class UpdatePistonDiameterRoutine extends AbstractRoutine {
  private UpdatePistonDiameterRoutine.InputValues inputValues;

  private UpdatePistonDiameterRoutine.Match.RetrievedValues retrievedValues;

  private UpdatePistonDiameterRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeCaliper caliper;

    public InputValues(final BrakeCaliper caliper) {
      this.caliper = caliper;
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

    public EObject getCorrepondenceSourceNamespace(final BrakeCaliper caliper) {
      return caliper;
    }

    public UpdatePistonDiameterRoutine.Match.RetrievedValues match(final BrakeCaliper caliper) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(caliper), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdatePistonDiameterRoutine.Match.RetrievedValues(namespace);
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

    public UpdatePistonDiameterRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.NumericParameter numericParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createNumericParameter();
      });
      return new UpdatePistonDiameterRoutine.Create.CreatedValues(numericParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeCaliper caliper, final Namespace namespace, final NumericParameter numericParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Piston Diameter"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      NumericParameter pPistonDiameter = ((NumericParameter) _findFirst);
      if ((pPistonDiameter == null)) {
        pPistonDiameter = numericParameter;
        pPistonDiameter.setName("Piston Diameter");
        pPistonDiameter.setUnit(Unit.MM);
        namespace.getParameters().add(pPistonDiameter);
      }
      pPistonDiameter.setValue(caliper.getPistonDiameterInMM());
    }
  }

  public UpdatePistonDiameterRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeCaliper caliper) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdatePistonDiameterRoutine.InputValues(caliper);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdatePistonDiameterRoutine with input:");
    	getLogger().trace("   inputValues.caliper: " + inputValues.caliper);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdatePistonDiameterRoutine.Match(getExecutionState()).match(inputValues.caliper);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdatePistonDiameterRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdatePistonDiameterRoutine.Update(getExecutionState()).updateModels(inputValues.caliper, retrievedValues.namespace, createdValues.numericParameter, getRoutinesFacade());
    return true;
  }
}
