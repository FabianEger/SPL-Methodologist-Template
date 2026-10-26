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
import tools.vitruv.methodologisttemplate.model.cad.Assembly;
import tools.vitruv.methodologisttemplate.model.cad.Box;
import tools.vitruv.methodologisttemplate.model.cad.CADDocument;
import tools.vitruv.methodologisttemplate.model.cad.CadFactory;
import tools.vitruv.methodologisttemplate.model.electrical.Circuit;
import tools.vitruv.methodologisttemplate.model.electrical.Device;
import tools.vitruv.methodologisttemplate.model.electrical.DeviceKind;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalDesign;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalFactory;
import tools.vitruv.methodologisttemplate.model.requirements.RequirementsSpecification;
import tools.vitruv.methodologisttemplate.model.simulink.SimulinkModel;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalAnalysisModel;
import tools.vitruv.methodologisttemplate.model.bom.BOM;

/**
 * Scenario 1: a new feature is added to the variability model of one domain (concrete instances
 * 1.1-A to 1.4-A of EvolutionScenarios/concrete_scenarios.tex).
 *
 * <p>Each test performs the evolution steps up to the point where the inconsistency would arise and
 * checks that the consistency preservation rules resolve it: the Feature CPRs create or map the
 * realisation of the new feature in the role's model, the overlap CPRs propagate to overlapping
 * artefacts of other roles, the reverse Feature CPRs represent new artefacts in the problem space, and
 * the ConfigCPRs add the feature to all configurations (not selected). Whether a configuration is still
 * a valid product is not checked here (VitruvOCL).
 */
public class Scenario1Test extends KARaceIngTestBase {

    @BeforeEach
    void loadInitialState(@TempDir Path storageFolder) {
        // initial state of the product line: the persisted VSUM in src/test/resources/initialstate
        openInitialState(storageFolder);
    }


    /**
     * 1.1-A New rule set FS2026 in PRJ. The Variability Engineer PRJ adds the alternative FS2026 to RuleSet.
     * Without consistency preservation the requirements specification and the configurations would not
     * reflect the new rule set. Expected: a new rule-set section FS2026 in the requirements specification
     * that corresponds to the feature, and all three configurations contain FS2026 (not selected).
     */
    @Test
    @DisplayName("1.1-A New rule set FS2026 in PRJ")
    public void scenario11A() {
        answerNewElementForFeature("FS2026", "Section");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatureToGroup(model, "RuleSet", 0, "FS2026"));

        read(List.of(RequirementsSpecification.class), view -> {
            var ruleSet = get(root(view, RequirementsSpecification.class), "RuleSet");
            assertTrue(childNames(ruleSet).contains("FS2026"), "rule-set section FS2026 created");
            return null;
        });
        assertEquals(List.of("FS2026"), artefactsOf("FS2026", "prj.requirements"));
        for (String car : List.of("EV3", "CV2", "CV1")) {
            Configuration configuration = readConfiguration(car);
            var fs2026 = configFeature(configuration, "FS2026");
            assertNotNull(fs2026, car + " contains FS2026");
            assertFalse(fs2026.isIsSelected(), car + " has not selected the new rule set");
        }
    }

    /**
     * 1.1-B Tank lifting device in DES. The Variability Engineer DES adds des.TankLiftingDevice (ANF-961,
     * ANF-962) below des.HydrogenTank (its presence condition is a constraint, which the UVL metamodel cannot
     * express). Expected: the tank assembly in the DES CAD model gets the lifting device, the BOM gets an item
     * for it, and the configuration of CV2 contains the new feature (not selected).
     */
    @Test
    @DisplayName("1.1-B Tank lifting device in DES")
    public void scenario11B() {
        answerNewElementForFeature("TankLiftingDevice", "Assembly");
        answer("Create counterpart", "[Overlap] TankLiftingDevice was added to HydrogenTank (DES)", "(KA-RaceIng BOM)");
        answer("Item", "[Overlap] Which kind of element should the counterpart of TankLiftingDevice");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "HydrogenTank", "Mandatory", "TankLiftingDevice"));

        read(List.of(CADDocument.class, BOM.class), view -> {
            Assembly tank = get(root(view, CADDocument.class, "DES"), "HydrogenTank");
            assertTrue(childNames(tank).contains("TankLiftingDevice"), "lifting device in the tank assembly");
            // the new item follows the id convention of the overlapping item (<ROLE>-<part>), rule AR13
            assertNotNull(find(root(view, BOM.class), "DES-TankLiftingDevice"), "BOM item for the lifting device");
            return null;
        });
        assertEquals(List.of("TankLiftingDevice"), artefactsOf("TankLiftingDevice", "des.cad"));
        assertTrue(correspondingNames("des.cad", "TankLiftingDevice", "overlap", "karaceing.bom").contains("DES-TankLiftingDevice"));
        assertAddedUnselected("CV2", "TankLiftingDevice");
    }

    /**
     * 1.1-C Pouch-cell option in DES. The Variability Engineer DES adds the optional design option
     * des.PouchCellPackaging (ANF-850 to 854) below des.AccumulatorContainer. The existing cell-fixation
     * sketch of the container CAD realises the option: the Feature CPR maps the feature to this existing
     * element. The configuration of EV3 contains the new option (not selected).
     */
    @Test
    @DisplayName("1.1-C Pouch-cell option in DES")
    public void scenario11C() {
        answerExistingElementForFeature("PouchCellPackaging", "CellFixationSketch");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "AccumulatorContainer", "Optional", "PouchCellPackaging"));

        assertEquals(List.of("CellFixationSketch"), artefactsOf("PouchCellPackaging", "des.cad"));
        assertAddedUnselected("EV3", "PouchCellPackaging");
    }

    /**
     * 1.2-A Hydrogen grounding in ELEC overlaps the tank CAD in DES. The Variability Engineer ELEC adds
     * elec.HydrogenGrounding (ANF-1013, 1015) below elec.SDCHydrogen; the Electrical Engineer realises it as a
     * grounding circuit. Since the shutdown circuit overlaps the H2 tank of DES, the overlap CPR asks for a
     * counterpart: the tank assembly gets the grounding lug (ANF-1014).
     */
    @Test
    @DisplayName("1.2-A Hydrogen grounding in ELEC overlaps the tank CAD in DES")
    public void scenario12A() {
        answerNewElementForFeature("HydrogenGrounding", "Circuit");
        answer("Create counterpart", "[Overlap] HydrogenGrounding was added to SDCHydrogen (ELEC)", "HydrogenTank (DES)");
        answer("Assembly", "[Overlap] Which kind of element should the counterpart of HydrogenGrounding below HydrogenTank");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "SDCHydrogen", "Optional", "HydrogenGrounding"));

        assertEquals(List.of("HydrogenGrounding"), artefactsOf("HydrogenGrounding", "elec.electrical"));
        read(List.of(CADDocument.class), view -> {
            Assembly tank = get(root(view, CADDocument.class, "DES"), "HydrogenTank");
            assertTrue(childNames(tank).contains("HydrogenGrounding"), "grounding lug at the tank");
            return null;
        });
        assertTrue(correspondingNames("elec.electrical", "HydrogenGrounding", "overlap", "des.cad").contains("HydrogenGrounding"));
        assertAddedUnselected("CV2", "HydrogenGrounding");
    }

    /**
     * 1.2-B Debris shield in DES overlaps the monocoque CAD in CRASH. The Variability Engineer DES adds
     * des.H2DebrisShield (ANF-939 to 942); the Design Engineer models the shield with four mounting points.
     * The H2 storage system of DES overlaps the primary structure of CRASH: the overlap CPRs create the
     * shield interface and the four inserts in the CRASH CAD model.
     */
    @Test
    @DisplayName("1.2-B Debris shield in DES overlaps the monocoque CAD in CRASH")
    public void scenario12B() {
        answerNewElementForFeature("H2DebrisShield", "Assembly");
        answer("Create counterpart", "[Overlap] H2DebrisShield was added to HydrogenStorageSystem (DES)", "PrimaryStructure (CRASH)");
        answer("Assembly", "[Overlap] Which kind of element should the counterpart of H2DebrisShield below PrimaryStructure");
        answerAlways("Create counterpart", "[Overlap] MountingPoint", "was added to H2DebrisShield (DES)", "H2DebrisShield (CRASH)");
        answerAlways("Box", "[Overlap] Which kind of element should the counterpart of MountingPoint");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "HydrogenStorageSystem", "Optional", "H2DebrisShield"));
        change(List.of(CADDocument.class), view -> {
            Assembly shield = get(root(view, CADDocument.class, "DES"), "H2DebrisShield");
            for (int i = 1; i <= 4; i++) {
                Box mountingPoint = CadFactory.eINSTANCE.createBox();
                mountingPoint.setName("MountingPoint" + i);
                shield.getChildren().add(mountingPoint);
            }
        });

        read(List.of(CADDocument.class), view -> {
            Assembly primaryStructure = get(root(view, CADDocument.class, "CRASH"), "PrimaryStructure");
            Assembly inserts = get(primaryStructure, "H2DebrisShield");
            assertEquals(List.of("MountingPoint1", "MountingPoint2", "MountingPoint3", "MountingPoint4"), childNames(inserts));
            return null;
        });
        assertTrue(correspondingNames("des.cad", "MountingPoint3", "overlap", "crash.cad").contains("MountingPoint3"));
    }

    /**
     * 1.2-C Shared cell temperature sensors in ELEC overlap the cell model in SIM. The Variability Engineer
     * ELEC adds the design option elec.SharedTemperatureSensors (ANF-883) below the cell temperature
     * monitoring of the BMS. The overlap CPR updates the signal assumptions of the derating in the SIM model.
     */
    @Test
    @DisplayName("1.2-C Shared cell temperature sensors in ELEC overlap the cell model in SIM")
    public void scenario12C() {
        answerNewElementForFeature("SharedTemperatureSensors", "Circuit");
        answer("Create counterpart", "[Overlap] SharedTemperatureSensors was added to CellTemperatureMonitoring (ELEC)", "CellTemperatureDerating (SIM)");
        answer("Block", "[Overlap] Which kind of element should the counterpart of SharedTemperatureSensors");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "CellTemperatureMonitoring", "Optional", "SharedTemperatureSensors"));

        read(List.of(SimulinkModel.class), view -> {
            var derating = get(root(view, SimulinkModel.class), "CellTemperatureDerating");
            assertTrue(childNames(derating).contains("SharedTemperatureSensors"), "SIM signal assumption updated");
            return null;
        });
        assertAddedUnselected("EV3", "SharedTemperatureSensors");
    }

    /**
     * 1.3-A Tank defuelling system in DES and valve power supply in ELEC. DES adds des.TankDefuellingSystem
     * (ANF-965, 966) and designs the manual valve and the Maximator port; ELEC adds an independent power
     * supply for the non-manual valves (ANF-967, FUN-114). The reverse Feature CPR represents the new
     * ELEC artefact in the ELEC variability model (new feature elec.ValvePowerSupply below SDCHydrogen).
     */
    @Test
    @DisplayName("1.3-A Tank defuelling system in DES and valve power supply in ELEC")
    public void scenario13A() {
        answerNewElementForFeature("TankDefuellingSystem", "Assembly");
        answer("New feature", "[FCPR] The new Device ValvePowerSupply in the ELEC model has no feature");
        answer("Optional", "[FCPR] In which kind of group below SDCHydrogen should the new feature ValvePowerSupply");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "HydrogenStorageSystem", "Optional", "TankDefuellingSystem"));
        change(List.of(CADDocument.class), view -> {
            Assembly defuelling = get(root(view, CADDocument.class, "DES"), "TankDefuellingSystem");
            for (String part : List.of("ManualValve", "MaximatorPort")) {
                Box box = CadFactory.eINSTANCE.createBox();
                box.setName(part);
                defuelling.getChildren().add(box);
            }
        });
        change(List.of(ElectricalDesign.class), view -> {
            Circuit sdc = get(root(view, ElectricalDesign.class), "SDCHydrogen");
            Device supply = ElectricalFactory.eINSTANCE.createDevice();
            supply.setName("ValvePowerSupply");
            supply.setKind(DeviceKind.RELAY);
            sdc.getElements().add(supply);
        });

        read(List.of(UVLModel.class), view -> {
            var feature = KARaceIngUtil.findFeature(root(view, UVLModel.class), "ValvePowerSupply");
            assertNotNull(feature, "feature for the valve power supply");
            assertEquals("SDCHydrogen", KARaceIngUtil.parentFeature(feature).getName());
            return null;
        });
        assertEquals(List.of("ValvePowerSupply"), artefactsOf("ValvePowerSupply", "elec.electrical"));
        assertAddedUnselected("CV2", "ValvePowerSupply");
    }

    /**
     * 1.3-B Structural side pod in CRASH and tank integration in DES. CRASH adds crash.StructuralSidePod
     * (ANF-932 to 934); DES moves the H2 tank into the side pod (ZIE-030). The reverse Feature CPR maps the
     * new side-pod tank assembly to the existing design option des.SidePodMounting, and the overlap CPR
     * links it to the new side pod of CRASH.
     */
    @Test
    @DisplayName("1.3-B Structural side pod in CRASH and tank integration in DES")
    public void scenario13B() {
        answerNewElementForFeature("StructuralSidePod", "Assembly");
        answer("Existing feature", "[FCPR] The new Assembly SidePodTankAssembly in the DES model has no feature");
        answer("SidePodMounting", "[FCPR] To which existing feature should SidePodTankAssembly");
        answer("Link existing element", "[Overlap] SidePodTankAssembly was added to HydrogenStorageSystem (DES)", "PrimaryStructure (CRASH)");
        answer("StructuralSidePod", "[Overlap] Which element of PrimaryStructure (CRASH) corresponds to SidePodTankAssembly");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "PrimaryStructure", "Optional", "StructuralSidePod"));
        change(List.of(CADDocument.class), view -> {
            Assembly storage = get(root(view, CADDocument.class, "DES"), "HydrogenStorageSystem");
            Assembly sidePodTank = CadFactory.eINSTANCE.createAssembly();
            sidePodTank.setName("SidePodTankAssembly");
            storage.getChildren().add(sidePodTank);
        });

        assertEquals(List.of("SidePodMounting"), featuresOf("des.cad", "SidePodTankAssembly"));
        assertTrue(correspondingNames("des.cad", "SidePodTankAssembly", "overlap", "crash.cad").contains("StructuralSidePod"));
        assertEquals(List.of("StructuralSidePod"), artefactsOf("StructuralSidePod", "crash.cad"));
    }

    /**
     * 1.3-C Dedicated vent lines in THERMO and their routing in DES. THERMO adds thermo.H2VentLines
     * (ANF-1010, 1011); the overlap CPR asks DES for the routing through the H2 storage compartment, and
     * the reverse Feature CPR maps the routing to des.HydrogenStorageSystem.
     */
    @Test
    @DisplayName("1.3-C Dedicated vent lines in THERMO and their routing in DES")
    public void scenario13C() {
        answerNewElementForFeature("H2VentLines", "ThermalSystem");
        answer("Create counterpart", "[Overlap] H2VentLines was added to HydrogenVentilation (THERMO)", "HydrogenStorageSystem (DES)");
        answer("Assembly", "[Overlap] Which kind of element should the counterpart of H2VentLines");
        answer("Existing feature", "[FCPR] The new Assembly H2VentLines in the DES model has no feature");
        answer("HydrogenStorageSystem", "[FCPR] To which existing feature should H2VentLines");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "HydrogenVentilation", "Optional", "H2VentLines"));

        assertEquals(List.of("H2VentLines"), artefactsOf("H2VentLines", "thermo.thermal"));
        assertTrue(correspondingNames("thermo.thermal", "H2VentLines", "overlap", "des.cad").contains("H2VentLines"));
        assertEquals(List.of("HydrogenStorageSystem"), featuresOf("des.cad", "H2VentLines"));
        read(List.of(ThermalAnalysisModel.class), view -> {
            assertNotNull(find(root(view, ThermalAnalysisModel.class), "H2VentLines"));
            return null;
        });
    }

    /**
     * 1.4-A Two-tank system in DES and tank-valve relay in ELEC. DES adds the alternative group
     * SingleTank | TwoTanks below des.HydrogenTank; ELEC adds elec.SecondTankValveRelay below SDCHydrogen
     * and links the relay to the two-tank layout (the constraint {@code elec.SecondTankValveRelay <=> des.TwoTanks}
     * is not part of the UVL model). The configuration of CV2 contains the new features (not selected).
     */
    @Test
    @DisplayName("1.4-A Two-tank system in DES and tank-valve relay in ELEC")
    public void scenario14A() {
        answerNewElementForFeature("SingleTank", "Assembly");
        answerNewElementForFeature("TwoTanks", "Assembly");
        answerNewElementForFeature("SecondTankValveRelay", "Device");
        answer("Link existing element", "[Overlap] SecondTankValveRelay was added to SDCHydrogen (ELEC)", "HydrogenTank (DES)");
        answer("TwoTanks", "[Overlap] Which element of HydrogenTank (DES) corresponds to SecondTankValveRelay");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> addFeatures(model, "HydrogenTank", "Alternative", "SingleTank", "TwoTanks"));
        changeFeatureModel(model -> addFeatures(model, "SDCHydrogen", "Optional", "SecondTankValveRelay"));

        assertTrue(correspondingNames("elec.electrical", "SecondTankValveRelay", "overlap", "des.cad").contains("TwoTanks"));
        assertAddedUnselected("CV2", "SingleTank");
        assertAddedUnselected("CV2", "TwoTanks");
        assertAddedUnselected("CV2", "SecondTankValveRelay");
    }
}
