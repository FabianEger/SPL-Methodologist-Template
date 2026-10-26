package mir.routines.brakesystem2cad;

import brakesystem.ABSSensor;
import brakesystem.BrakeCaliper;
import brakesystem.BrakeComponent;
import brakesystem.BrakeDisk;
import brakesystem.BrakeHose;
import brakesystem.BrakePad;
import brakesystem.Brakesystem;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class Brakesystem2cadRoutinesFacade extends AbstractRoutinesFacade {
  public Brakesystem2cadRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createAndRegisterRoot(final Brakesystem repo) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterRootRoutine routine = new CreateAndRegisterRootRoutine(_routinesFacade, _executionState, _caller, repo);
    return routine.execute();
  }

  public boolean updateSpecificationType(final BrakeComponent comp) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateSpecificationTypeRoutine routine = new UpdateSpecificationTypeRoutine(_routinesFacade, _executionState, _caller, comp);
    return routine.execute();
  }

  public boolean updateOEMNumber(final BrakeComponent comp) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateOEMNumberRoutine routine = new UpdateOEMNumberRoutine(_routinesFacade, _executionState, _caller, comp);
    return routine.execute();
  }

  public boolean updateFittingPosition(final BrakeComponent comp) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateFittingPositionRoutine routine = new UpdateFittingPositionRoutine(_routinesFacade, _executionState, _caller, comp);
    return routine.execute();
  }

  public boolean updateId(final BrakeComponent comp) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateIdRoutine routine = new UpdateIdRoutine(_routinesFacade, _executionState, _caller, comp);
    return routine.execute();
  }

  public boolean createAndRegisterBrakeDisk(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterBrakeDiskRoutine routine = new CreateAndRegisterBrakeDiskRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateDiameter(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateDiameterRoutine routine = new UpdateDiameterRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateCenteringDiameter(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateCenteringDiameterRoutine routine = new UpdateCenteringDiameterRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateRimHoleNumber(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateRimHoleNumberRoutine routine = new UpdateRimHoleNumberRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateHoleArrangementNumber(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateHoleArrangementNumberRoutine routine = new UpdateHoleArrangementNumberRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateBoltHoleCircle(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBoltHoleCircleRoutine routine = new UpdateBoltHoleCircleRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateBrakeDiskThickness(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBrakeDiskThicknessRoutine routine = new UpdateBrakeDiskThicknessRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateMinimumThickness(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateMinimumThicknessRoutine routine = new UpdateMinimumThicknessRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean updateVentilated(final BrakeDisk disk) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateVentilatedRoutine routine = new UpdateVentilatedRoutine(_routinesFacade, _executionState, _caller, disk);
    return routine.execute();
  }

  public boolean createAndRegisterBrakeCaliper(final BrakeCaliper caliper) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterBrakeCaliperRoutine routine = new CreateAndRegisterBrakeCaliperRoutine(_routinesFacade, _executionState, _caller, caliper);
    return routine.execute();
  }

  public boolean updatePistonDiameter(final BrakeCaliper caliper) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdatePistonDiameterRoutine routine = new UpdatePistonDiameterRoutine(_routinesFacade, _executionState, _caller, caliper);
    return routine.execute();
  }

  public boolean updateBrakeDiskThicknessCaliper(final BrakeCaliper caliper) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBrakeDiskThicknessCaliperRoutine routine = new UpdateBrakeDiskThicknessCaliperRoutine(_routinesFacade, _executionState, _caller, caliper);
    return routine.execute();
  }

  public boolean createAndRegisterBrakePad(final BrakePad pad) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterBrakePadRoutine routine = new CreateAndRegisterBrakePadRoutine(_routinesFacade, _executionState, _caller, pad);
    return routine.execute();
  }

  public boolean updateWearWarning(final BrakePad pad) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateWearWarningRoutine routine = new UpdateWearWarningRoutine(_routinesFacade, _executionState, _caller, pad);
    return routine.execute();
  }

  public boolean updateHeight(final BrakePad pad) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateHeightRoutine routine = new UpdateHeightRoutine(_routinesFacade, _executionState, _caller, pad);
    return routine.execute();
  }

  public boolean updateWidth(final BrakePad pad) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateWidthRoutine routine = new UpdateWidthRoutine(_routinesFacade, _executionState, _caller, pad);
    return routine.execute();
  }

  public boolean updateThickness(final BrakePad pad) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateThicknessRoutine routine = new UpdateThicknessRoutine(_routinesFacade, _executionState, _caller, pad);
    return routine.execute();
  }

  public boolean createAndRegisterBrakeHose(final BrakeHose hose) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterBrakeHoseRoutine routine = new CreateAndRegisterBrakeHoseRoutine(_routinesFacade, _executionState, _caller, hose);
    return routine.execute();
  }

  public boolean updateThreadSize1(final BrakeHose hose) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateThreadSize1Routine routine = new UpdateThreadSize1Routine(_routinesFacade, _executionState, _caller, hose);
    return routine.execute();
  }

  public boolean updateThreadSize2(final BrakeHose hose) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateThreadSize2Routine routine = new UpdateThreadSize2Routine(_routinesFacade, _executionState, _caller, hose);
    return routine.execute();
  }

  public boolean updateLengthHose(final BrakeHose hose) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateLengthHoseRoutine routine = new UpdateLengthHoseRoutine(_routinesFacade, _executionState, _caller, hose);
    return routine.execute();
  }

  public boolean createAndRegisterABSSensor(final ABSSensor sensor) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterABSSensorRoutine routine = new CreateAndRegisterABSSensorRoutine(_routinesFacade, _executionState, _caller, sensor);
    return routine.execute();
  }

  public boolean updateSensorType(final ABSSensor sensor) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateSensorTypeRoutine routine = new UpdateSensorTypeRoutine(_routinesFacade, _executionState, _caller, sensor);
    return routine.execute();
  }

  public boolean updateFittingDepth(final ABSSensor sensor) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateFittingDepthRoutine routine = new UpdateFittingDepthRoutine(_routinesFacade, _executionState, _caller, sensor);
    return routine.execute();
  }

  public boolean updateNumberOfPins(final ABSSensor sensor) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateNumberOfPinsRoutine routine = new UpdateNumberOfPinsRoutine(_routinesFacade, _executionState, _caller, sensor);
    return routine.execute();
  }

  public boolean updateLength(final ABSSensor sensor) {
    Brakesystem2cadRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateLengthRoutine routine = new UpdateLengthRoutine(_routinesFacade, _executionState, _caller, sensor);
    return routine.execute();
  }
}
