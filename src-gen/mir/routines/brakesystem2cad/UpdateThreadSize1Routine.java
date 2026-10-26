package mir.routines.brakesystem2cad;

import brakesystem.BrakeHose;
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
public class UpdateThreadSize1Routine extends AbstractRoutine {
  private UpdateThreadSize1Routine.InputValues inputValues;

  private UpdateThreadSize1Routine.Match.RetrievedValues retrievedValues;

  private UpdateThreadSize1Routine.Create.CreatedValues createdValues;

  public class InputValues {
    public final BrakeHose hose;

    public InputValues(final BrakeHose hose) {
      this.hose = hose;
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

    public EObject getCorrepondenceSourceNamespace(final BrakeHose hose) {
      return hose;
    }

    public UpdateThreadSize1Routine.Match.RetrievedValues match(final BrakeHose hose) throws IOException {
      edu.kit.ipd.sdq.metamodels.cad.Namespace namespace = getCorrespondingElement(
      	getCorrepondenceSourceNamespace(hose), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.Namespace.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (namespace == null) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.UpdateThreadSize1Routine.Match.RetrievedValues(namespace);
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

    public UpdateThreadSize1Routine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.StringParameter stringParameter = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createStringParameter();
      });
      return new UpdateThreadSize1Routine.Create.CreatedValues(stringParameter);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final BrakeHose hose, final Namespace namespace, final StringParameter stringParameter, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      final Function1<Parameter, Boolean> _function = (Parameter p) -> {
        String _name = p.getName();
        return Boolean.valueOf(Objects.equals(_name, "Thread 1"));
      };
      Parameter _findFirst = IterableExtensions.<Parameter>findFirst(namespace.getParameters(), _function);
      StringParameter pThreadSize1 = ((StringParameter) _findFirst);
      if ((pThreadSize1 == null)) {
        pThreadSize1 = stringParameter;
        pThreadSize1.setName("Thread 1");
        namespace.getParameters().add(pThreadSize1);
      }
      pThreadSize1.setValue(hose.getThreadSize1());
    }
  }

  public UpdateThreadSize1Routine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final BrakeHose hose) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new UpdateThreadSize1Routine.InputValues(hose);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine UpdateThreadSize1Routine with input:");
    	getLogger().trace("   inputValues.hose: " + inputValues.hose);
    }
    retrievedValues = new mir.routines.brakesystem2cad.UpdateThreadSize1Routine.Match(getExecutionState()).match(inputValues.hose);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.UpdateThreadSize1Routine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.UpdateThreadSize1Routine.Update(getExecutionState()).updateModels(inputValues.hose, retrievedValues.namespace, createdValues.stringParameter, getRoutinesFacade());
    return true;
  }
}
