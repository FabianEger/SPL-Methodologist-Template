package mir.reactions.combinedUVLToCAD;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class CombinedUVLToCADChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public CombinedUVLToCADChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("http://vitruv.tools/methodologisttemplate/uvl")), 
    	MetamodelDescriptor.with(Set.of("http://www.example.org/cad")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.combinedUVLToCAD.CombinedUVLToCADRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    addReaction(new mir.reactions.feature2cad.UVLFeatureInsertedIntoDomainRootReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("combinedUVLToCAD.feature2cad"))));
    addReaction(new mir.reactions.featureRoot2cad.UVLDomainRootInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("combinedUVLToCAD.featureRoot2cad"))));
  }
}
