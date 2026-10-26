package mir.reactions.cad2brakesystem;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class Cad2brakesystemChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public Cad2brakesystemChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("http://www.example.org/cad")), 
    	MetamodelDescriptor.with(Set.of("http://www.example.org/brakesystem")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.cad2brakesystem.Cad2brakesystemRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    addReaction(new mir.reactions.cad2brakesystem.CAD_ModelInsertedAsRootReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("cad2brakesystem"))));
    addReaction(new mir.reactions.cad2brakesystem.NameSpaceInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("cad2brakesystem"))));
    addReaction(new mir.reactions.cad2brakesystem.ParameterInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("cad2brakesystem"))));
    addReaction(new mir.reactions.cad2brakesystem.ParameterValueChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("cad2brakesystem"))));
  }
}
