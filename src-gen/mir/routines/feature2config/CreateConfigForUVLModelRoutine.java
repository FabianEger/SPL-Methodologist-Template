package mir.routines.feature2config;

import UVLPackage.UVLModel;
import configurationPackage.ConfigurationModel;
import java.io.File;
import java.io.IOException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateConfigForUVLModelRoutine extends AbstractRoutine {
  private CreateConfigForUVLModelRoutine.InputValues inputValues;

  private CreateConfigForUVLModelRoutine.Match.RetrievedValues retrievedValues;

  private CreateConfigForUVLModelRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final UVLModel uvlModel;

    public InputValues(final UVLModel uvlModel) {
      this.uvlModel = uvlModel;
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

    public EObject getCorrepondenceSource1(final UVLModel uvlModel) {
      return uvlModel;
    }

    public CreateConfigForUVLModelRoutine.Match.RetrievedValues match(final UVLModel uvlModel) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(uvlModel), // correspondence source supplier
      	configurationPackage.ConfigurationModel.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      return new mir.routines.feature2config.CreateConfigForUVLModelRoutine.Match.RetrievedValues();
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final ConfigurationModel configModel;

      public CreatedValues(final ConfigurationModel configModel) {
        this.configModel = configModel;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateConfigForUVLModelRoutine.Create.CreatedValues createElements() {
      configurationPackage.ConfigurationModel configModel = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createConfigurationModel();
      });
      return new CreateConfigForUVLModelRoutine.Create.CreatedValues(configModel);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final UVLModel uvlModel, final ConfigurationModel configModel, @Extension final Feature2configRoutinesFacade _routinesFacade) {
      String _string = new File("").toString();
      String _plus = (_string + "brake.config");
      this.persistProjectRelative(uvlModel, configModel, _plus);
      this.addCorrespondenceBetween(uvlModel, configModel);
    }
  }

  public CreateConfigForUVLModelRoutine(final Feature2configRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final UVLModel uvlModel) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateConfigForUVLModelRoutine.InputValues(uvlModel);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateConfigForUVLModelRoutine with input:");
    	getLogger().trace("   inputValues.uvlModel: " + inputValues.uvlModel);
    }
    retrievedValues = new mir.routines.feature2config.CreateConfigForUVLModelRoutine.Match(getExecutionState()).match(inputValues.uvlModel);
    if (retrievedValues == null) {
    	return false;
    }
    createdValues = new mir.routines.feature2config.CreateConfigForUVLModelRoutine.Create(getExecutionState()).createElements();
    new mir.routines.feature2config.CreateConfigForUVLModelRoutine.Update(getExecutionState()).updateModels(inputValues.uvlModel, createdValues.configModel, getRoutinesFacade());
    return true;
  }
}
