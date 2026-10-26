package tools.vitruv.methodologisttemplate.vsum.karaceing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.UVLModel;
import configurationPackage.Configuration;
import configurationPackage.ConfigurationModel;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.model.bom.BOM;
import tools.vitruv.methodologisttemplate.model.cad.Assembly;
import tools.vitruv.methodologisttemplate.model.cad.Box;
import tools.vitruv.methodologisttemplate.model.cad.CADDocument;
import tools.vitruv.methodologisttemplate.model.cad.CadFactory;
import tools.vitruv.methodologisttemplate.model.cad.DocumentObject;
import tools.vitruv.methodologisttemplate.model.cad.Sketch;
import tools.vitruv.methodologisttemplate.model.electrical.Circuit;
import tools.vitruv.methodologisttemplate.model.electrical.Device;
import tools.vitruv.methodologisttemplate.model.electrical.DeviceKind;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalDesign;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalFactory;
import tools.vitruv.methodologisttemplate.model.simulink.Block;
import tools.vitruv.methodologisttemplate.model.simulink.SimuLinkFactory;
import tools.vitruv.methodologisttemplate.model.simulink.SimulinkModel;
import tools.vitruv.methodologisttemplate.model.simulink.SubSystem;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalAnalysisModel;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalFactory;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalSystem;

/**
 * Scenario 3: a new solution-space artefact is added in one domain (concrete instances 3.1-A to 3.4-B).
 * The reverse Feature CPRs ask whether the new artefact is represented by a new feature, mapped to an
 * existing feature, or kept as a realisation detail; the overlap CPRs propagate the new artefact to the
 * overlapping artefacts of other domains; the ConfigCPRs add new features to all configurations.
 */
public class Scenario3Test extends KARaceIngTestBase {

    @BeforeEach
    void loadInitialState(@TempDir Path storageFolder) {
        // initial state of the product line: the persisted VSUM in src/test/resources/initialstate
        openInitialState(storageFolder);
    }


    private void addToCad(String document, String parent, DocumentObject element) {
        change(List.of(CADDocument.class), view -> {
            Assembly container = get(root(view, CADDocument.class, document), parent);
            container.getChildren().add(element);
        });
    }

    private static Assembly assembly(String name) {
        Assembly assembly = CadFactory.eINSTANCE.createAssembly();
        assembly.setName(name);
        return assembly;
    }

    private static Box box(String name) {
        Box box = CadFactory.eINSTANCE.createBox();
        box.setName(name);
        return box;
    }

    private void addDevice(String circuit, String name, DeviceKind kind) {
        change(List.of(ElectricalDesign.class), view -> {
            Circuit container = get(root(view, ElectricalDesign.class), circuit);
            Device device = ElectricalFactory.eINSTANCE.createDevice();
            device.setName(name);
            device.setKind(kind);
            container.getElements().add(device);
        });
    }

    private void addSimulinkElement(String subsystem, String name, boolean asSubSystem) {
        change(List.of(SimulinkModel.class), view -> {
            SubSystem container = get(root(view, SimulinkModel.class), subsystem);
            Block block = asSubSystem ? SimuLinkFactory.eINSTANCE.createSubSystem() : SimuLinkFactory.eINSTANCE.createBlock();
            block.setName(name);
            container.getSubBlocks().add(block);
        });
    }

    /**
     * 3.1-A XPulse inverter in the electrical design (ELEC / SIM). The Electrical Engineer adds the XPulse
     * inverter (ANF-608) to the HV system. The reverse Feature CPR adds the missing motor-controller
     * feature below elec.HighVoltage; the overlap CPR creates the inverter interface in the traction-system
     * control of SIM, whose power limitation and regeneration use the inverter parameters.
     */
    @Test
    @DisplayName("3.1-A XPulse inverter in the electrical design (ELEC / SIM)")
    public void scenario31A() {
        answer("New feature", "[FCPR] The new Device XPulseInverter in the ELEC model has no feature");
        answer("Optional", "[FCPR] In which kind of group below HighVoltage should the new feature XPulseInverter");
        answer("Create counterpart", "[Overlap] XPulseInverter was added to HighVoltage (ELEC)", "TractionSystemControl (SIM)");
        answer("SubSystem", "[Overlap] Which kind of element should the counterpart of XPulseInverter");
        answerRemainingDialogsWithDefaults();

        addDevice("HighVoltage", "XPulseInverter", DeviceKind.INVERTER);

        assertEquals(List.of("XPulseInverter"), artefactsOf("XPulseInverter", "elec.electrical"));
        assertTrue(correspondingNames("elec.electrical", "XPulseInverter", "overlap", "sim.simulink").contains("XPulseInverter"));
        read(List.of(SimulinkModel.class), view -> {
            assertTrue(childNames(get(root(view, SimulinkModel.class), "TractionSystemControl")).contains("XPulseInverter"));
            return null;
        });
        assertAddedUnselected("EV3", "XPulseInverter");
    }

    /**
     * 3.1-B Lidar mount at the nose (CRASH / AERO). The Crash Structure Engineer adds a lidar bracket at the
     * anti-intrusion plate. The reverse Feature CPR maps it to crash.AntiIntrusionPlate, the overlap CPR
     * reserves the space in front of the AIP in the AERO nose / front-wing model.
     */
    @Test
    @DisplayName("3.1-B Lidar mount at the nose (CRASH / AERO)")
    public void scenario31B() {
        answer("Existing feature", "[FCPR] The new Box NoseLidarBracket in the CRASH model has no feature");
        answer("AntiIntrusionPlate", "[FCPR] To which existing feature should NoseLidarBracket");
        answer("Create counterpart", "[Overlap] NoseLidarBracket was added to AntiIntrusionPlate (CRASH)", "AeroDevices (AERO)");
        answer("Box", "[Overlap] Which kind of element should the counterpart of NoseLidarBracket");
        answerRemainingDialogsWithDefaults();

        addToCad("CRASH", "AntiIntrusionPlate", box("NoseLidarBracket"));

        assertEquals(List.of("AntiIntrusionPlate"), featuresOf("crash.cad", "NoseLidarBracket"));
        assertTrue(correspondingNames("crash.cad", "NoseLidarBracket", "overlap", "aero.cad").contains("NoseLidarBracket"));
    }

    /**
     * 3.1-C ROS2 software stack (SIM). The Simulation Engineer adds ROS2 nodes for the AS state machine and
     * the safety monitoring. The reverse Feature CPR maps them to sim.ASStateMachine / sim.ASFunctionalSafety;
     * the overlap CPR links the state-machine node to the CarPC of the electrical design, whose network
     * configuration has to follow the migration.
     */
    @Test
    @DisplayName("3.1-C ROS2 software stack (SIM)")
    public void scenario31C() {
        answer("Existing feature", "[FCPR] The new Block ROS2StateMachineNode in the SIM model has no feature");
        answer("ASStateMachine", "[FCPR] To which existing feature should ROS2StateMachineNode");
        answer("Existing feature", "[FCPR] The new Block ROS2SafetyMonitorNode in the SIM model has no feature");
        answer("ASFunctionalSafety", "[FCPR] To which existing feature should ROS2SafetyMonitorNode");
        answer("Link existing element", "[Overlap] ROS2StateMachineNode was added to ASStateMachine (SIM)", "AutonomousSystemElectrics (ELEC)");
        answer("CarPC", "[Overlap] Which element of AutonomousSystemElectrics (ELEC) corresponds to ROS2StateMachineNode");
        answerRemainingDialogsWithDefaults();

        addSimulinkElement("ASStateMachine", "ROS2StateMachineNode", false);
        addSimulinkElement("ASFunctionalSafety", "ROS2SafetyMonitorNode", false);

        assertEquals(List.of("ASStateMachine"), featuresOf("sim.simulink", "ROS2StateMachineNode"));
        assertEquals(List.of("ASFunctionalSafety"), featuresOf("sim.simulink", "ROS2SafetyMonitorNode"));
        assertTrue(correspondingNames("sim.simulink", "ROS2StateMachineNode", "overlap", "elec.electrical").contains("CarPC"));
    }

    /**
     * 3.1-D Analog manometer at the H2 tank (DES). The Design Engineer adds the manometer between the
     * on-tank device and the pressure regulator. The reverse Feature CPR maps it to des.PressureRegulator;
     * the overlap CPR adds the manometer to the BOM.
     */
    @Test
    @DisplayName("3.1-D Analog manometer at the H2 tank (DES)")
    public void scenario31D() {
        answer("Existing feature", "[FCPR] The new Box AnalogManometer in the DES model has no feature");
        answer("PressureRegulator", "[FCPR] To which existing feature should AnalogManometer");
        answer("Create counterpart", "[Overlap] AnalogManometer was added to HydrogenTank (DES)", "(KA-RaceIng BOM)");
        answer("Item", "[Overlap] Which kind of element should the counterpart of AnalogManometer");
        answerRemainingDialogsWithDefaults();

        addToCad("DES", "HydrogenTank", box("AnalogManometer"));

        assertEquals(List.of("PressureRegulator"), featuresOf("des.cad", "AnalogManometer"));
        read(List.of(BOM.class), view -> {
            assertNotNull(find(root(view, BOM.class), "DES-AnalogManometer"), "BOM item for the manometer (id convention <ROLE>-<part>)");
            return null;
        });
    }

    /**
     * 3.2-A Real-time battery cell model (SIM) and BMS interface (ELEC). SIM adds the real-time cell model
     * (linked to the BMS of the electrical design via the overlap CPR); ELEC adds the cell-data CAN
     * interface, which the reverse Feature CPR maps to elec.BatteryManagementSystem.
     */
    @Test
    @DisplayName("3.2-A Real-time battery cell model (SIM) and BMS interface (ELEC)")
    public void scenario32A() {
        answer("Link existing element", "[Overlap] RealTimeCellModel was added to TractionSystemControl (SIM)", "HighVoltage (ELEC)");
        answer("BatteryManagementSystem", "[Overlap] Which element of HighVoltage (ELEC) corresponds to RealTimeCellModel");
        answer("Existing feature", "[FCPR] The new Device CellDataCANInterface in the ELEC model has no feature");
        answer("BatteryManagementSystem", "[FCPR] To which existing feature should CellDataCANInterface");
        answerRemainingDialogsWithDefaults();

        addSimulinkElement("TractionSystemControl", "RealTimeCellModel", true);
        addDevice("BatteryManagementSystem", "CellDataCANInterface", DeviceKind.CONTROL_UNIT);

        assertTrue(correspondingNames("sim.simulink", "RealTimeCellModel", "overlap", "elec.electrical").contains("BatteryManagementSystem"));
        assertEquals(List.of("BatteryManagementSystem"), featuresOf("elec.electrical", "CellDataCANInterface"));
    }

    /**
     * 3.2-B Side-pod tank dynamics (KIN) and tank position (DES). KIN adds a suspension set-up for the
     * shifted tank mass (mapped to kin.SuspensionAdaptationForTankMass); DES lowers the tank in the side pod
     * (mapped to the existing design option des.SidePodMounting).
     */
    @Test
    @DisplayName("3.2-B Side-pod tank dynamics (KIN) and tank position (DES)")
    public void scenario32B() {
        answer("Existing feature", "[FCPR] The new Sketch TankMassSuspensionSetup in the KIN model has no feature");
        answer("SuspensionAdaptationForTankMass", "[FCPR] To which existing feature should TankMassSuspensionSetup");
        answer("Existing feature", "[FCPR] The new Assembly LoweredTankPosition in the DES model has no feature");
        answer("SidePodMounting", "[FCPR] To which existing feature should LoweredTankPosition");
        answerRemainingDialogsWithDefaults();

        Sketch setup = CadFactory.eINSTANCE.createSketch();
        setup.setName("TankMassSuspensionSetup");
        addToCad("KIN", "SuspensionAdaptationForTankMass", setup);
        addToCad("DES", "HydrogenStorageSystem", assembly("LoweredTankPosition"));

        assertEquals(List.of("SuspensionAdaptationForTankMass"), featuresOf("kin.cad", "TankMassSuspensionSetup"));
        assertEquals(List.of("SidePodMounting"), featuresOf("des.cad", "LoweredTankPosition"));
    }

    /**
     * 3.2-C Water-injection system (THERMO) and intake (DES). THERMO adds the water-injection model (mapped
     * to thermo.WaterInjection); DES adds injector bosses (mapped to des.IntakeSystem and linked to the
     * THERMO model) and a water tank, for which a new feature des.WaterTank is added.
     */
    @Test
    @DisplayName("3.2-C Water-injection system (THERMO) and intake (DES)")
    public void scenario32C() {
        answer("Existing feature", "[FCPR] The new ThermalSystem WaterInjectionModel in the THERMO model has no feature");
        answer("WaterInjection", "[FCPR] To which existing feature should WaterInjectionModel");
        answer("Existing feature", "[FCPR] The new Box InjectorBosses in the DES model has no feature");
        answer("IntakeSystem", "[FCPR] To which existing feature should InjectorBosses");
        answer("Link existing element", "[Overlap] InjectorBosses was added to IntakeSystem (DES)", "WaterInjection (THERMO)");
        answer("WaterInjectionModel", "[Overlap] Which element of WaterInjection (THERMO) corresponds to InjectorBosses");
        answer("New feature", "[FCPR] The new Assembly WaterTank in the DES model has no feature");
        answer("Optional", "[FCPR] In which kind of group below IntakeSystem should the new feature WaterTank");
        answerRemainingDialogsWithDefaults();

        change(List.of(ThermalAnalysisModel.class), view -> {
            ThermalSystem waterInjection = get(root(view, ThermalAnalysisModel.class), "WaterInjection");
            ThermalSystem model = ThermalFactory.eINSTANCE.createThermalSystem();
            model.setName("WaterInjectionModel");
            waterInjection.getElements().add(model);
        });
        addToCad("DES", "IntakeSystem", box("InjectorBosses"));
        addToCad("DES", "IntakeSystem", assembly("WaterTank"));

        assertEquals(List.of("WaterInjection"), featuresOf("thermo.thermal", "WaterInjectionModel"));
        assertEquals(List.of("IntakeSystem"), featuresOf("des.cad", "InjectorBosses"));
        assertTrue(correspondingNames("des.cad", "InjectorBosses", "overlap", "thermo.thermal").contains("WaterInjectionModel"));
        assertEquals(List.of("WaterTank"), featuresOf("des.cad", "WaterTank"));
        assertAddedUnselected("CV2", "WaterTank");
    }

    /**
     * 3.3-A New accumulator attachment (DES). The Design Engineer adds the attachment brackets of the
     * accumulator container; the reverse Feature CPR adds the mandatory part des.AccumulatorMounting below
     * des.AccumulatorContainer. The configuration of EV3 contains the new part (not selected).
     */
    @Test
    @DisplayName("3.3-A New accumulator attachment (DES)")
    public void scenario33A() {
        answer("New feature", "[FCPR] The new Assembly AccumulatorMounting in the DES model has no feature");
        answer("Mandatory", "[FCPR] In which kind of group below AccumulatorContainer should the new feature AccumulatorMounting");
        answerRemainingDialogsWithDefaults();

        addToCad("DES", "AccumulatorContainer", assembly("AccumulatorMounting"));

        assertEquals(List.of("AccumulatorMounting"), featuresOf("des.cad", "AccumulatorMounting"));
        assertAddedUnselected("EV3", "AccumulatorMounting");
    }

    /**
     * 3.4-A Merged CarPC/MCU (ELEC) and software deployment (SIM). ELEC adds the merged compute unit; the
     * overlap CPR re-targets the AS functions in SIM to it; the Variability Engineer SIM adds the
     * alternative sim.ComputePlatform = SeparateCarPCAndMCU | MergedComputeUnit, which is added to the
     * configuration of EV3 (not selected). The FCPR realises ComputePlatform as a Block of the SIM model;
     * since a Block cannot contain elements, the alternatives below it have no SIM realisation (the
     * pattern reports "cannot contain new elements").
     */
    @Test
    @DisplayName("3.4-A Merged CarPC/MCU (ELEC) and software deployment (SIM)")
    public void scenario34A() {
        answer("Create counterpart", "[Overlap] MergedComputeUnit was added to AutonomousSystemElectrics (ELEC)", "ASStateMachine (SIM)");
        answer("Block", "[Overlap] Which kind of element should the counterpart of MergedComputeUnit");
        // below the SIM model the FCPR offers the contained types of SimulinkModel (Block, connections, ...);
        // a Block contains nothing, so the two alternatives below ComputePlatform get no SIM element
        answerNewElementForFeature("ComputePlatform", "Block");
        answer(NEW, "map the new feature SeparateCarPCAndMCU to an existing element");
        answer(NEW, "map the new feature MergedComputeUnit to an existing element");
        answerRemainingDialogsWithDefaults();

        addDevice("AutonomousSystemElectrics", "MergedComputeUnit", DeviceKind.CONTROL_UNIT);
        changeFeatureModel(model -> addFeatures(model, "Simulation", "Mandatory", "ComputePlatform"));
        changeFeatureModel(model -> addFeatures(model, "ComputePlatform", "Alternative", "SeparateCarPCAndMCU", "MergedComputeUnit"));

        assertTrue(correspondingNames("elec.electrical", "MergedComputeUnit", "overlap", "sim.simulink").contains("MergedComputeUnit"));
        assertEquals(List.of("ComputePlatform"), artefactsOf("ComputePlatform", "sim.simulink"));
        assertAddedUnselected("EV3", "ComputePlatform");
    }

    /**
     * 3.4-B Halfcoque for the hydrogen line (CRASH) and frame-mounted tank (DES). CRASH adds the halfcoque and
     * the steel rear frame; DES attaches the tank to the frame with four mounting points; the Variability
     * Engineer DES adds the design option des.FrameMountedTank (ANF-945) and maps it to the mounting points.
     * The configuration of CV2 contains the new option (not selected).
     */
    @Test
    @DisplayName("3.4-B Halfcoque for the hydrogen line (CRASH) and frame-mounted tank (DES)")
    public void scenario34B() {
        answerExistingElementForFeature("FrameMountedTank", "FrameMountingPoints");
        answerRemainingDialogsWithDefaults();

        addToCad("CRASH", "PrimaryStructure", assembly("Halfcoque"));
        addToCad("CRASH", "PrimaryStructure", assembly("SteelRearFrame"));
        addToCad("DES", "HydrogenTank", assembly("FrameMountingPoints"));
        changeFeatureModel(model -> addFeatures(model, "HydrogenStorageSystem", "Optional", "FrameMountedTank"));

        assertEquals(List.of("FrameMountingPoints"), artefactsOf("FrameMountedTank", "des.cad"));
        read(List.of(UVLModel.class), view -> {
            assertNotNull(KARaceIngUtil.findFeature(root(view, UVLModel.class), "FrameMountedTank"));
            return null;
        });
        assertAddedUnselected("CV2", "FrameMountedTank");
    }
}
