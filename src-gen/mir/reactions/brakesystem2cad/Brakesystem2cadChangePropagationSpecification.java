package mir.reactions.brakesystem2cad;

import java.util.Set;
import tools.vitruv.change.composite.MetamodelDescriptor;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.reactions.AbstractReactionsChangePropagationSpecification;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class Brakesystem2cadChangePropagationSpecification extends AbstractReactionsChangePropagationSpecification implements ChangePropagationSpecification {
  public Brakesystem2cadChangePropagationSpecification() {
    super(MetamodelDescriptor.with(Set.of("http://www.example.org/brakesystem")), 
    	MetamodelDescriptor.with(Set.of("http://www.example.org/cad")));
  }

  protected RoutinesFacadesProvider createRoutinesFacadesProvider(final ReactionExecutionState executionState) {
    return new mir.routines.brakesystem2cad.Brakesystem2cadRoutinesFacadesProvider(executionState);
  }

  protected void setup() {
    addReaction(new mir.reactions.brakesystem2cad.BrakesystemInsertedAsRootReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.SpecificationTypeChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.OEMNumberChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.FittingPositionChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.IdChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BrakeDiskInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.DiameterChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.CenteringDiameterChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.RimHoleNumberChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.HoleArrangementNumberChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BoltHoleCircleChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BrakeDiskThicknessChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.MinimumThicknessChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.VentilatedChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BrakeCaliperInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.PistonDiameterChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BrakeDiskThicknessChangedCaliperReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BrakePadInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.WearWarningChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.HeightChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.WidthChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.ThicknessChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.BrakeHoseInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.ThreadSize1ChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.ThreadSize2ChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.LengthChangedHoseReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.ABSSensorInsertedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.SensorTypeChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.FittingDepthChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.NumberOfPinsChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
    addReaction(new mir.reactions.brakesystem2cad.LengthChangedReaction((executionState) -> createRoutinesFacadesProvider(executionState).getRoutinesFacade(ReactionsImportPath.fromPathString("brakesystem2cad"))));
  }
}
