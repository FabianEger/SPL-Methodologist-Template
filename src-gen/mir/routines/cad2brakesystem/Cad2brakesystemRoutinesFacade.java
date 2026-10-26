package mir.routines.cad2brakesystem;

import brakesystem.Brakesystem;
import edu.kit.ipd.sdq.metamodels.cad.CAD_Model;
import edu.kit.ipd.sdq.metamodels.cad.Namespace;
import edu.kit.ipd.sdq.metamodels.cad.Parameter;
import tools.vitruv.dsls.reactions.runtime.routines.AbstractRoutinesFacade;
import tools.vitruv.dsls.reactions.runtime.routines.RoutinesFacadesProvider;
import tools.vitruv.dsls.reactions.runtime.state.ReactionExecutionState;
import tools.vitruv.dsls.reactions.runtime.structure.CallHierarchyHaving;
import tools.vitruv.dsls.reactions.runtime.structure.ReactionsImportPath;

@SuppressWarnings("all")
public class Cad2brakesystemRoutinesFacade extends AbstractRoutinesFacade {
  public Cad2brakesystemRoutinesFacade(final RoutinesFacadesProvider routinesFacadesProvider, final ReactionsImportPath reactionsImportPath) {
    super(routinesFacadesProvider, reactionsImportPath);
  }

  public boolean createAndRegisterRoot(final CAD_Model model) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterRootRoutine routine = new CreateAndRegisterRootRoutine(_routinesFacade, _executionState, _caller, model);
    return routine.execute();
  }

  public boolean createAndRegisterNamespace(final Namespace namespace) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateAndRegisterNamespaceRoutine routine = new CreateAndRegisterNamespaceRoutine(_routinesFacade, _executionState, _caller, namespace);
    return routine.execute();
  }

  public boolean createABSSensor(final Namespace namespace, final Brakesystem mBrake) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateABSSensorRoutine routine = new CreateABSSensorRoutine(_routinesFacade, _executionState, _caller, namespace, mBrake);
    return routine.execute();
  }

  public boolean createBrakeCaliper(final Namespace namespace, final Brakesystem mBrake) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateBrakeCaliperRoutine routine = new CreateBrakeCaliperRoutine(_routinesFacade, _executionState, _caller, namespace, mBrake);
    return routine.execute();
  }

  public boolean createBrakeHose(final Namespace namespace, final Brakesystem mBrake) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateBrakeHoseRoutine routine = new CreateBrakeHoseRoutine(_routinesFacade, _executionState, _caller, namespace, mBrake);
    return routine.execute();
  }

  public boolean createBrakeDisk(final Namespace namespace, final Brakesystem mBrake) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    CreateBrakeDiskRoutine routine = new CreateBrakeDiskRoutine(_routinesFacade, _executionState, _caller, namespace, mBrake);
    return routine.execute();
  }

  public boolean updateCorrespondingAttribute(final Parameter parameter) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateCorrespondingAttributeRoutine routine = new UpdateCorrespondingAttributeRoutine(_routinesFacade, _executionState, _caller, parameter);
    return routine.execute();
  }

  public boolean updateBrakeComponent(final Parameter parameter) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBrakeComponentRoutine routine = new UpdateBrakeComponentRoutine(_routinesFacade, _executionState, _caller, parameter);
    return routine.execute();
  }

  public boolean updateABSSensor(final Parameter parameter) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateABSSensorRoutine routine = new UpdateABSSensorRoutine(_routinesFacade, _executionState, _caller, parameter);
    return routine.execute();
  }

  public boolean updateBrakeCaliper(final Parameter parameter) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBrakeCaliperRoutine routine = new UpdateBrakeCaliperRoutine(_routinesFacade, _executionState, _caller, parameter);
    return routine.execute();
  }

  public boolean updateBrakeHose(final Parameter parameter) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBrakeHoseRoutine routine = new UpdateBrakeHoseRoutine(_routinesFacade, _executionState, _caller, parameter);
    return routine.execute();
  }

  public boolean updateBrakeDisk(final Parameter parameter) {
    Cad2brakesystemRoutinesFacade _routinesFacade = this;
    ReactionExecutionState _executionState = getExecutionState();
    CallHierarchyHaving _caller = this.getCurrentCaller();
    UpdateBrakeDiskRoutine routine = new UpdateBrakeDiskRoutine(_routinesFacade, _executionState, _caller, parameter);
    return routine.execute();
  }
}
