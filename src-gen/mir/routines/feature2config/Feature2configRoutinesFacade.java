package mir.routines.feature2config;

import UVLPackage.Feature;
import UVLPackage.UVLModel;
import configurationPackage.Configuration;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class Feature2configRoutinesFacade extends AbstractRoutinesFacade {
  public Feature2configRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createConfigForUVLModel(final UVLModel uvlModel) {
    Feature2configRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateConfigForUVLModelRoutine routine = new CreateConfigForUVLModelRoutine(_routinesFacade, _executionState, _caller, uvlModel);
    return routine.execute();
  }

  public boolean createConfigForFeature(final Feature feature) {
    Feature2configRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateConfigForFeatureRoutine routine = new CreateConfigForFeatureRoutine(_routinesFacade, _executionState, _caller, feature);
    return routine.execute();
  }

  public boolean createFeatureinEveryConfig(final Feature feature, final Configuration config) {
    Feature2configRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateFeatureinEveryConfigRoutine routine = new CreateFeatureinEveryConfigRoutine(_routinesFacade, _executionState, _caller, feature, config);
    return routine.execute();
  }
}
