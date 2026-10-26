package mir.routines.feature2config;

import UVLPackage.Feature;
import configurationPackage.Configuration;
import configurationPackage.ConfigurationModel;
import java.io.IOException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.xbase.lib.Extension;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutine;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;

@SuppressWarnings("all")
public class CreateConfigForFeatureRoutine extends AbstractRoutine {
  private CreateConfigForFeatureRoutine.InputValues inputValues;

  private CreateConfigForFeatureRoutine.Match.RetrievedValues retrievedValues;

  public class InputValues {
    public final Feature feature;

    public InputValues(final Feature feature) {
      this.feature = feature;
    }
  }

  private static class Match extends AbstractRoutine.Match {
    public class RetrievedValues {
      public final ConfigurationModel configmodel;

      public RetrievedValues(final ConfigurationModel configmodel) {
        this.configmodel = configmodel;
      }
    }

    public Match(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public EObject getCorrepondenceSource1(final Feature feature) {
      return feature;
    }

    public EObject getCorrepondenceSourceConfigmodel(final Feature feature) {
      EObject _rootContainer = EcoreUtil.getRootContainer(feature);
      return _rootContainer;
    }

    public CreateConfigForFeatureRoutine.Match.RetrievedValues match(final Feature feature) throws IOException {
      if (hasCorrespondingElements(
      	getCorrepondenceSource1(feature), // correspondence source supplier
      	configurationPackage.Feature.class,
      	null, // correspondence precondition checker
      	null
      )) {
      	return null;
      }
      configurationPackage.ConfigurationModel configmodel = getCorrespondingElement(
      	getCorrepondenceSourceConfigmodel(feature), // correspondence source supplier
      	configurationPackage.ConfigurationModel.class,
      	null, // correspondence precondition checker
      	null, 
      	false // asserted
      );
      if (configmodel == null) {
      	return null;
      }
      return new mir.routines.feature2config.CreateConfigForFeatureRoutine.Match.RetrievedValues(configmodel);
    }
  }

  private static class Update extends AbstractRoutine.Update {
    public Update(final ReactionExecutionState reactionExecutionState) {
      super(reactionExecutionState);
    }

    public void updateModels(final Feature feature, final ConfigurationModel configmodel, @Extension final Feature2configRoutinesFacade _routinesFacade) {
      EList<Configuration> _configuration = configmodel.getConfiguration();
      for (final Configuration config : _configuration) {
        _routinesFacade.createFeatureinEveryConfig(feature, config);
      }
    }
  }

  public CreateConfigForFeatureRoutine(final Feature2configRoutinesFacade routinesFacade, final ReactionExecutionState reactionExecutionState, final CallHierarchyHaving calledBy, final Feature feature) {
    super(routinesFacade, reactionExecutionState, calledBy);
    this.inputValues = new CreateConfigForFeatureRoutine.InputValues(feature);
  }

  protected boolean executeRoutine() throws IOException {
    if (getLogger().isTraceEnabled()) {
    	getLogger().trace("Called routine CreateConfigForFeatureRoutine with input:");
    	getLogger().trace("   inputValues.feature: " + inputValues.feature);
    }
    retrievedValues = new mir.routines.feature2config.CreateConfigForFeatureRoutine.Match(getExecutionState()).match(inputValues.feature);
    if (retrievedValues == null) {
    	return false;
    }
    // This execution step is empty
    new mir.routines.feature2config.CreateConfigForFeatureRoutine.Update(getExecutionState()).updateModels(inputValues.feature, retrievedValues.configmodel, getRoutinesFacade());
    return true;
  }
}
