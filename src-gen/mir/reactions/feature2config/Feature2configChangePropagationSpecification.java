package mir.reactions.feature2config;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class Feature2configChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public Feature2configChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("http://vitruv.tools/methodologisttemplate/uvl")), 
    	MetamodelDescriptor.with(Set.of("http://vitruv.tools/methodologisttemplate/config")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.feature2config.Feature2configRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    addReaction(new mir.reactions.feature2config.UVLModelInsertedAsRootReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("feature2config"))));
    addReaction(new mir.reactions.feature2config.FeatureInsertedIntoUVLModelReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("feature2config"))));
  }
}
