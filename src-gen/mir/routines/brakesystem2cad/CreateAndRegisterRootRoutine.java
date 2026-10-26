package mir.routines.brakesystem2cad;

import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import java.io.File;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateAndRegisterRootRoutine extends AbstractRoutine {
  private CreateAndRegisterRootRoutine.InputValues inputValues;

  private CreateAndRegisterRootRoutine.Match.RetrievedValues retrievedValues;

  private CreateAndRegisterRootRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final Brakesystem repo;

    public InputValues(final Brakesystem repo) {
      this.repo = repo;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public RetrievedValues() {
        
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSource1(final Brakesystem repo) {
      return repo;
    }

    public CreateAndRegisterRootRoutine.Match.RetrievedValues match(final Brakesystem repo) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(repo), // correspondence source supplier
      	edu.kit.ipd.sdq.metamodels.cad.CAD_Model.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.brakesystem2cad.CreateAndRegisterRootRoutine.Match.RetrievedValues();
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final CAD_Model mCad;

      public CreatedValues(final CAD_Model mCad) {
        this.mCad = mCad;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateAndRegisterRootRoutine.Create.CreatedValues createElements() {
      edu.kit.ipd.sdq.metamodels.cad.CAD_Model mCad = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createCAD_Model();
      });
      return new CreateAndRegisterRootRoutine.Create.CreatedValues(mCad);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Brakesystem repo, final CAD_Model mCad, @Extension final Brakesystem2cadRoutinesFacade _routinesFacade) {
      String _string = new File("").toString();
      String _plus = (_string + "example.cad");
      this.persistProjectRelative(repo, mCad, _plus);
      this.addCorrespondenceBetween(repo, mCad);
    }
  }

  public CreateAndRegisterRootRoutine(final Brakesystem2cadRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Brakesystem repo) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateAndRegisterRootRoutine.InputValues(repo);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateAndRegisterRootRoutine with input:");
    	getLogger().trace("   inputValues.repo: " + inputValues.repo);
    }
    retrievedValues = new mir.routines.brakesystem2cad.CreateAndRegisterRootRoutine.Match(getExecutionState()).match(inputValues.repo);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.brakesystem2cad.CreateAndRegisterRootRoutine.Create(getExecutionState()).createElements();
    new mir.routines.brakesystem2cad.CreateAndRegisterRootRoutine.Update(getExecutionState()).updateModels(inputValues.repo, createdValues.mCad, getRoutinesFacade());
    return true;
  }
}
