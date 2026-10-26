package mir.routines.feature2config;

import UVLPackage.Feature;
import configurationPackage.Configuration;
import java.io.IOException;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateFeatureinEveryConfigRoutine extends AbstractRoutine {
  private CreateFeatureinEveryConfigRoutine.InputValues inputValues;

  private CreateFeatureinEveryConfigRoutine.Create.CreatedValues createdValues;

  public class InputValues {
    public final Feature feature;

    public final Configuration config;

    public InputValues(final Feature feature, final Configuration config) {
      this.feature = feature;
      this.config = config;
    }
  }

  private static class Create extends AbstractRoutine.Create {
    public class CreatedValues {
      public final configurationPackage.Feature configFeature;

      public CreatedValues(final configurationPackage.Feature configFeature) {
        this.configFeature = configFeature;
      }
    }

    public Create(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public CreateFeatureinEveryConfigRoutine.Create.CreatedValues createElements() {
      configurationPackage.Feature configFeature = createObject(() -> {
      	return org.eclipse.emf.ecore.impl.EFactoryImpl.eINSTANCE.createFeature();
      });
      return new CreateFeatureinEveryConfigRoutine.Create.CreatedValues(configFeature);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Feature feature, final Configuration config, final configurationPackage.Feature configFeature, @Extension final Feature2configRoutinesFacade _routinesFacade) {
      configFeature.setIsSelected(false);
      configFeature.setName(feature.getName());
      config.getFeature().add(configFeature);
      this.addCorrespondenceBetween(feature, configFeature);
    }
  }

  public CreateFeatureinEveryConfigRoutine(final Feature2configRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Feature feature, final Configuration config) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateFeatureinEveryConfigRoutine.InputValues(feature, config);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateFeatureinEveryConfigRoutine with input:");
    	getLogger().trace("   inputValues.feature: " + inputValues.feature);
    	getLogger().trace("   inputValues.config: " + inputValues.config);
    }
    // This execution step is empty
    createdValues = new mir.routines.feature2config.CreateFeatureinEveryConfigRoutine.Create(getExecutionState()).createElements();
    new mir.routines.feature2config.CreateFeatureinEveryConfigRoutine.Update(getExecutionState()).updateModels(inputValues.feature, inputValues.config, createdValues.configFeature, getRoutinesFacade());
    return true;
  }
}
