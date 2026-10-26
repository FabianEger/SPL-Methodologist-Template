package mir.reactions.combinedUVLToBS;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class CombinedUVLToBSChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public CombinedUVLToBSChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("http://vitruv.tools/methodologisttemplate/uvl")), 
    	MetamodelDescriptor.with(Set.of("http://www.example.org/brakesystem")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.combinedUVLToBS.CombinedUVLToBSRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    addReaction(new mir.reactions.feature2brakesystem.UVLFeatureInsertedIntoDomainRootReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("combinedUVLToBS.feature2brakesystem"))));
    addReaction(new mir.reactions.featureRoot2brakesystem.UVLDomainRootInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("combinedUVLToBS.featureRoot2brakesystem"))));
  }
}
