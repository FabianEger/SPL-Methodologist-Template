package mir.routines.brakesystem2cad;

import brakesystem.BrakeComponent;
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
public class UpdateFittingPositionRoutine extends AbstractRoutine {
  private UpdateFittingPositionRoutine.InputValues inputValues;

  private UpdateFittingPositionRoutine.Match.RetrievedValues retrievedValues;

  private UpdateFittingPositionRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeComponent comp;

    public InputValues(final BrakeComponent comp) {
      this.comp = comp;
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

    public EObject getCorrepondenceSourceNamespace(final BrakeComponent comp) {
      return comp;
    }

    public UpdateFittingPositionRoutine.Match.RetrievedValues match(final BrakeComponent comp) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(comp), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateFittingPositionRoutine.Match.RetrievedValues(namespace);
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

    public UpdateFittingPositionRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.StringParameter stringParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      return new UpdateFittingPositionRoutine.Create.CreatedValues(stringParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeComponent comp, final Namespace namespace, final StringParameter stringParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Fitting Position"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      StringParameter pFittingPosition = ((StringParameter) _findFirst);
      if ((pFittingPosition == null)) {
        pFittingPosition = stringParameter;
        pFittingPosition.setName("Fitting Position");
        namespace.getParameters().add(pFittingPosition);
      }
      pFittingPosition.setValue(comp.getFittingPosition());
    }
  }

  public UpdateFittingPositionRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeComponent comp) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateFittingPositionRoutine.InputValues(comp);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateFittingPositionRoutine with input:");
    	getLogger().trace("   inputValues.comp: " + inputValues.comp);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateFittingPositionRoutine.Match(getExecutionState()).match(inputValues.comp);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateFittingPositionRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateFittingPositionRoutine.Update(getExecutionState()).updateModels(inputValues.comp, retrievedValues.namespace, createdValues.stringParameter, getRoutinesFacade());
    return true;
  }
}
