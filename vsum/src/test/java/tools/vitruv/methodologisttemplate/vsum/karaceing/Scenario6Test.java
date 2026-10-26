package tools.vitruv.methodologisttemplate.vsum.karaceing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.UVLModel;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.model.bom.BOM;
import tools.vitruv.methodologisttemplate.model.cad.CADDocument;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalDesign;
import tools.vitruv.methodologisttemplate.model.simulink.SimulinkModel;
import tools.vitruv.methodologisttemplate.model.testbench.TestBenchConfiguration;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalAnalysisModel;

/**
 * Scenario 6: a solution-space artefact is removed from one domain (concrete instances 6.1-A to 6.4-A).
 * The reverse Feature CPRs remove the trace links of the removed artefact and ask whether a feature
 * without any remaining realisation is removed; the overlap CPRs ask whether overlapping artefacts of
 * other domains (and BOM items) are removed as well; requirement traces are cleaned up.
 */
public class Scenario6Test extends KARaceIngTestBase {

    @BeforeEach
    void loadInitialState(@TempDir Path storageFolder) {
        // initial state of the product line: the persisted VSUM in src/test/resources/initialstate
        openInitialState(storageFolder);
    }


    /** Removes the element with the given name from the first root of the given type (or the root with the given name). */
    private void removeElement(Class<? extends EObject> rootType, String rootName, String element) {
        change(List.of(rootType), view -> {
            EObject root = rootName == null ? root(view, rootType) : root(view, rootType, rootName);
            EcoreUtil.remove(get(root, element));
        });
    }

    private boolean existsIn(Class<? extends EObject> rootType, String rootName, String element) {
        return read(List.of(rootType), view -> find(rootName == null ? root(view, rootType) : root(view, rootType, rootName), element) != null);
    }

    /**
     * 6.1-A Lidar bracket on the main hoop (CRASH / SIM). CRASH removes the lidar bracket. The trace link to
     * crash.MainHoop is removed (MainHoop keeps its other realisation, so the problem space is unchanged);
     * the perception pose in SIM and the BOM item are removed via the overlap CPRs.
     */
    @Test
    @DisplayName("6.1-A Lidar bracket on the main hoop (CRASH / SIM)")
    public void scenario61A() {
        answer("Delete counterpart", "[Overlap] LidarBracket was removed", "LidarPoseMainHoop (SIM)");
        answer("Delete counterpart", "[Overlap] LidarBracket was removed", "(KA-RaceIng BOM)");
        answerRemainingDialogsWithDefaults();

        removeElement(CADDocument.class, "CRASH", "LidarBracket");

        assertFalse(artefactsOf("MainHoop", "crash.cad").contains("LidarBracket"), "dangling feature link removed");
        assertTrue(artefactsOf("MainHoop", "crash.cad").contains("MainHoop"));
        assertFalse(existsIn(SimulinkModel.class, null, "LidarPoseMainHoop"));
        assertFalse(existsIn(BOM.class, null, "CRASH-LidarBracket"));
    }

    /**
     * 6.1-B Over-pressure relays of the H2 shutdown circuit (ELEC / DES). ELEC removes the over-pressure relay
     * chain from elec.SDCHydrogen. The overlap CPR asks DES about the relay contacts of the pressure
     * regulator, which are removed (to be replaced by a normally-closed switch).
     */
    @Test
    @DisplayName("6.1-B Over-pressure relays of the H2 shutdown circuit (ELEC / DES)")
    public void scenario61B() {
        answer("Delete counterpart", "[Overlap] OverPressureRelayChain was removed", "RelayContacts (DES)");
        answerAlways("Delete counterpart", "[Overlap]", "was removed", "(KA-RaceIng BOM)");
        answerRemainingDialogsWithDefaults();

        removeElement(ElectricalDesign.class, null, "OverPressureRelayChain");

        assertFalse(existsIn(CADDocument.class, "DES", "RelayContacts"));
        assertFalse(existsIn(BOM.class, null, "DES-RelayContacts"));
        assertFalse(artefactsOf("SDCHydrogen", "elec.electrical").contains("OverPressureRelayChain"));
        assertTrue(artefactsOf("SDCHydrogen", "elec.electrical").contains("SDCHydrogen"));
    }

    /**
     * 6.1-C ROS1 nodes (SIM / ELEC). SIM deletes the ROS1 nodes of sim.ASStateMachine and sim.StatusIndication.
     * The overlap CPR removes the obsolete ROS1 bridge port of the CarPC in the electrical design.
     */
    @Test
    @DisplayName("6.1-C ROS1 nodes (SIM / ELEC)")
    public void scenario61C() {
        answerAlways("Delete counterpart", "[Overlap] ROS1", "ROS1BridgePort (ELEC)");
        answerRemainingDialogsWithDefaults();

        change(List.of(SimulinkModel.class), view -> {
            SimulinkModel model = root(view, SimulinkModel.class);
            EcoreUtil.remove(get(model, "ROS1StateMachineNode"));
            EcoreUtil.remove(get(model, "ROS1StatusNode"));
        });

        assertFalse(existsIn(ElectricalDesign.class, null, "ROS1BridgePort"));
        assertTrue(existsIn(ElectricalDesign.class, null, "CarPC"));
    }

    /**
     * 6.1-D TS-connector check (ELEC / SIM). ELEC removes the plug-detection circuit from elec.TSWiring. The
     * TS activation logic in SIM still waits for the plug-detection signal: the overlap CPR removes it.
     */
    @Test
    @DisplayName("6.1-D TS-connector check (ELEC / SIM)")
    public void scenario61D() {
        answer("Delete counterpart", "[Overlap] PlugDetectionCircuit was removed", "PlugDetectionSignal (SIM)");
        answerRemainingDialogsWithDefaults();

        removeElement(ElectricalDesign.class, null, "PlugDetectionCircuit");

        assertFalse(existsIn(SimulinkModel.class, null, "PlugDetectionSignal"));
        assertFalse(artefactsOf("TSWiring", "elec.electrical").contains("PlugDetectionCircuit"));
        assertTrue(artefactsOf("TSWiring", "elec.electrical").contains("TSWiring"));
    }

    /**
     * 6.2-A VDC Simulink model replaced by C++ (SIM / KIN). SIM deletes the Simulink model of
     * sim.VehicleDynamicsControl; KIN moves the vehicle parameters (kinematics tables) to the C++ parameter
     * file, so the overlapping sketch is deleted. The kinematics tables were the only realisation of
     * kin.KinematicsForTyreModel: the reverse Feature CPR asks, and the feature is removed (also from EV3, CV1).
     */
    @Test
    @DisplayName("6.2-A VDC Simulink model replaced by C++ (SIM / KIN)")
    public void scenario62A() {
        answer("Delete counterpart", "[Overlap] VDCParameterSet was removed", "KinematicsTables (KIN)");
        answer("Remove feature", "[FCPR] The feature KinematicsForTyreModel has no realisation");
        answerRemainingDialogsWithDefaults();

        change(List.of(SimulinkModel.class), view -> {
            SimulinkModel model = root(view, SimulinkModel.class);
            EcoreUtil.remove(get(model, "VDCSimulinkModel"));
            EcoreUtil.remove(get(model, "VDCParameterSet"));
        });

        assertFalse(existsIn(CADDocument.class, "KIN", "KinematicsTables"));
        boolean featureStillExists = read(List.of(UVLModel.class),
                view -> KARaceIngUtil.findFeature(root(view, UVLModel.class), "KinematicsForTyreModel") != null);
        assertFalse(featureStillExists);
        assertNull(configFeature(readConfiguration("EV3"), "KinematicsForTyreModel"));
    }

    /**
     * 6.2-B Old inverter after the XPulse change (ELEC / SIM). ELEC removes the old inverter. The overlap CPRs
     * remove the old inverter interface blocks of the power limitation and the regeneration in SIM and the
     * BOM item; sim.PowerLimitation and sim.Regeneration keep their realisation.
     */
    @Test
    @DisplayName("6.2-B Old inverter after the XPulse change (ELEC / SIM)")
    public void scenario62B() {
        answerAlways("Delete counterpart", "[Overlap] OldInverter was removed");
        answerRemainingDialogsWithDefaults();

        removeElement(ElectricalDesign.class, null, "OldInverter");

        assertFalse(existsIn(SimulinkModel.class, null, "OldInverterInterface"));
        assertFalse(existsIn(SimulinkModel.class, null, "OldInverterRegenInterface"));
        assertFalse(existsIn(BOM.class, null, "ELEC-OldInverter"));
        assertFalse(artefactsOf("PowerLimitation", "sim.simulink").contains("OldInverterInterface"));
        assertTrue(artefactsOf("PowerLimitation", "sim.simulink").contains("PowerLimitation"));
    }

    /**
     * 6.2-C BSPD inspection test (VAL / ELEC). VAL removes the BSPD signal-injection test; ELEC removes the
     * test-signal connector of the BSPD via the overlap CPR. The trace link from
     * elec.BrakeSystemPlausibilityDevice to the connector is removed; the feature keeps its circuit.
     */
    @Test
    @DisplayName("6.2-C BSPD inspection test (VAL / ELEC)")
    public void scenario62C() {
        answer("Delete counterpart", "[Overlap] BSPDSignalInjectionTest was removed", "BSPDTestConnector (ELEC)");
        answerRemainingDialogsWithDefaults();

        removeElement(TestBenchConfiguration.class, null, "BSPDSignalInjectionTest");

        assertFalse(existsIn(ElectricalDesign.class, null, "BSPDTestConnector"));
        assertEquals(List.of("BrakeSystemPlausibilityDevice"), artefactsOf("BrakeSystemPlausibilityDevice", "elec.electrical"));
    }

    /**
     * 6.3-A Backfire relief analysis (THERMO). THERMO deletes the backfire relief analysis (the relief valve in
     * DES is kept), then the Variability Engineer removes thermo.BackfireProtection and its realisation.
     * CV2 no longer contains the feature.
     */
    @Test
    @DisplayName("6.3-A Backfire relief analysis (THERMO)")
    public void scenario63A() {
        answerAlways("Delete artefact", "[FCPR] The feature BackfireProtection was removed");
        answerRemainingDialogsWithDefaults();

        removeElement(ThermalAnalysisModel.class, null, "BackfireReliefAnalysis");
        changeFeatureModel(model -> removeFeature(model, "BackfireProtection"));

        assertFalse(existsIn(ThermalAnalysisModel.class, null, "BackfireProtection"));
        assertTrue(existsIn(CADDocument.class, "DES", "ReliefValveAndFlameArrestor"), "kept in DES");
        assertNull(configFeature(readConfiguration("CV2"), "BackfireProtection"));
    }

    /**
     * 6.4-A CV1-chassis adapter (DES / THERMO). DES deletes the adapter frame (keeping the feature for now);
     * THERMO deletes the overlapping engine-conversion analysis (overlap CPR); the Variability Engineer THERMO
     * removes thermo.EngineConversionToHydrogen and its realisation. CV2 no longer contains the feature.
     */
    @Test
    @DisplayName("6.4-A CV1-chassis adapter (DES / THERMO)")
    public void scenario64A() {
        answer("Keep feature", "[FCPR] The feature TankSystemIntegration has no realisation");
        answer("Delete counterpart", "[Overlap] CV1ChassisAdapterFrame was removed", "EngineConversionAnalysis (THERMO)");
        answerAlways("Delete artefact", "[FCPR] The feature EngineConversionToHydrogen was removed");
        answerRemainingDialogsWithDefaults();

        removeElement(CADDocument.class, "DES", "CV1ChassisAdapterFrame");
        changeFeatureModel(model -> removeFeature(model, "EngineConversionToHydrogen"));

        assertFalse(existsIn(ThermalAnalysisModel.class, null, "EngineConversionAnalysis"));
        assertFalse(existsIn(ThermalAnalysisModel.class, null, "EngineConversionToHydrogen"));
        assertTrue(existsIn(CADDocument.class, "DES", "HydrogenConversionParts"), "kept (overlap answered with keep)");
        assertNull(configFeature(readConfiguration("CV2"), "EngineConversionToHydrogen"));
        assertTrue(artefactsOf("TankSystemIntegration", "des.cad").isEmpty(), "TankSystemIntegration kept without realisation");
    }
}
