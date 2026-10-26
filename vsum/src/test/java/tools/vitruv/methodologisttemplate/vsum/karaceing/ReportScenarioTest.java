package tools.vitruv.methodologisttemplate.vsum.karaceing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.UVLModel;
import brakesystem.BrakeCaliper;
import brakesystem.BrakeDisk;
import brakesystem.BrakePad;
import brakesystem.Brakesystem;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.model.bom.BOM;
import tools.vitruv.methodologisttemplate.model.bom.Item;
import tools.vitruv.methodologisttemplate.model.bom.ParameterValue;
import tools.vitruv.methodologisttemplate.model.bom.StringParameterValue;
import tools.vitruv.methodologisttemplate.model.cad.Assembly;
import tools.vitruv.methodologisttemplate.model.cad.BooleanOperation;
import tools.vitruv.methodologisttemplate.model.cad.BooleanType;
import tools.vitruv.methodologisttemplate.model.cad.Box;
import tools.vitruv.methodologisttemplate.model.cad.CADDocument;
import tools.vitruv.methodologisttemplate.model.cad.CadFactory;
import tools.vitruv.methodologisttemplate.model.cad.Cylinder;
import tools.vitruv.methodologisttemplate.model.cad.Material;
import tools.vitruv.methodologisttemplate.model.cad.Solid;
import tools.vitruv.methodologisttemplate.model.electrical.Circuit;
import tools.vitruv.methodologisttemplate.model.electrical.Device;
import tools.vitruv.methodologisttemplate.model.electrical.DeviceKind;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalDesign;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalFactory;
import tools.vitruv.methodologisttemplate.model.electrical.Pin;
import tools.vitruv.methodologisttemplate.model.requirements.Requirement;
import tools.vitruv.methodologisttemplate.model.requirements.RequirementKind;
import tools.vitruv.methodologisttemplate.model.requirements.RequirementsFactory;
import tools.vitruv.methodologisttemplate.model.requirements.RequirementsSpecification;
import tools.vitruv.methodologisttemplate.model.requirements.Section;
import tools.vitruv.methodologisttemplate.model.simulink.Block;
import tools.vitruv.methodologisttemplate.model.simulink.Parameter;
import tools.vitruv.methodologisttemplate.model.simulink.SimulinkModel;
import tools.vitruv.methodologisttemplate.model.testbench.TestBenchConfiguration;
import tools.vitruv.methodologisttemplate.model.testbench.TestCase;
import tools.vitruv.methodologisttemplate.model.thermal.AnalysisCase;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalAnalysisModel;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalComponent;

/**
 * The KA-RaceIng scenarios of the Inconsistency Scenarios TechReport (KA-RaceIng-1 to KA-RaceIng-10), executed
 * as the "proposed consistency preservation" on the initial state. Each test performs the stakeholder steps of
 * the report on the models of the VSUM and checks what Vitruvius does in between: the values propagated over
 * the semantic overlaps (rules AR1-AR13 in KARaceIngOverlapRules, overlap kinds K1-K11 in
 * docs/OVERLAP_CATALOGUE.md) and the notifications to the stakeholder roles (the messages "[Notify → role]").
 *
 * <p>Report artefacts are mapped onto the models of the VSUM: Brake Specification Model = des.brakesystem,
 * SysML architecture / electrical design = elec.electrical, supplier data and procurement data = BOM item
 * parameters, wear data = brake specification attributes. The Wiki model, performance analysis reports and the
 * inventory model are not part of the VSUM: steps on them are documented, not executed.
 */
public class ReportScenarioTest extends KARaceIngTestBase {

    @BeforeEach
    void loadInitialState(@TempDir Path storageFolder) {
        // initial state of the product line: the persisted VSUM in src/test/resources/initialstate
        openInitialState(storageFolder);
    }

    // ------------------------------------------------------------------ helpers

    /** Answers of the reverse FCPR for a new requirement: map it to an existing feature (of any role). Register before the defaults. */
    private void answerRequirementMapping(String id, String feature) {
        answer("Existing feature", "[FCPR] The new Requirement " + id);
        answer(feature, "[FCPR] To which existing feature should " + id);
    }

    /** The Project Manager adds a requirement to the DES requirements (mapped as scripted by answerRequirementMapping). */
    private void addRequirement(String id, String text) {
        change(List.of(RequirementsSpecification.class), view -> {
            Section section = get(root(view, RequirementsSpecification.class), "DESRequirements");
            Requirement requirement = RequirementsFactory.eINSTANCE.createRequirement();
            requirement.setName(id);
            requirement.setText(text);
            requirement.setKind(RequirementKind.REQUIREMENT);
            requirement.setRole("PRJ");
            section.getRequirements().add(requirement);
        });
    }

    private <T> T readBom(java.util.function.Function<BOM, T> reader) {
        return read(List.of(BOM.class), view -> reader.apply(root(view, BOM.class)));
    }

    private static String bomValue(BOM bom, String itemId, String parameter) {
        Item item = get(bom, itemId);
        for (ParameterValue value : item.getParameterValues()) {
            if (value.getDefinition() != null && parameter.equals(value.getDefinition().getName())) {
                return value instanceof StringParameterValue s ? s.getValue()
                        : String.valueOf(value.eGet(value.eClass().getEStructuralFeature("value")));
            }
        }
        return null;
    }

    private String bomValue(String itemId, String parameter) {
        return readBom(bom -> bomValue(bom, itemId, parameter));
    }

    private String simParameter(String block, String parameter) {
        return read(List.of(SimulinkModel.class), view -> {
            Block b = get(root(view, SimulinkModel.class), block);
            return b.getParameters().stream().filter(p -> parameter.equals(p.getName())).map(Parameter::getValue).findFirst().orElse(null);
        });
    }

    private static Material material(CADDocument document, String name) {
        return document.getMaterials().stream().filter(m -> name.equals(m.getName())).findFirst().orElseThrow();
    }

    private static Box box(String name, double length, double width, double height, Material material) {
        Box box = CadFactory.eINSTANCE.createBox();
        box.setName(name);
        box.setLabel(name);
        box.setLength(length);
        box.setWidth(width);
        box.setHeight(height);
        box.setMaterial(material);
        return box;
    }

    private static Device device(String name, DeviceKind kind, String partNumber, String... pinsAndSignals) {
        Device device = ElectricalFactory.eINSTANCE.createDevice();
        device.setName(name);
        device.setKind(kind);
        device.setPartNumber(partNumber);
        for (int i = 0; i < pinsAndSignals.length; i += 2) {
            Pin pin = ElectricalFactory.eINSTANCE.createPin();
            pin.setName(pinsAndSignals[i]);
            pin.setSignal(pinsAndSignals[i + 1]);
            device.getPins().add(pin);
        }
        return device;
    }

    private static double number(String value) {
        return Double.parseDouble(value);
    }

    // ------------------------------------------------------------------ R1

    /**
     * R1 Brake disc becomes vented (KA-RaceIng-1). The project goals dictate weight saving, so the Project Manager
     * adds the requirement that brake discs must be vented (step 1) and records it in the brake specification.
     * Vitruvius updates the BOM (vented discs must be ordered, Procurement Manager) and notifies the Design Engineer
     * that the CAD disc has no ventilation holes (not automatically resolvable, steps 2-3). The Design Engineer adds
     * the holes (step 4); Vitruvius derives mass, inertia and heat capacity from the geometry and propagates them to
     * the brake model of the simulation and to the BOM, and informs the Simulation, Kinematics (mass difference),
     * Aerodynamics (notification only), Thermodynamics and Validation Engineers (step 5). The simulation no longer
     * assumes a solid disc.
     */
    @Test
    @DisplayName("R1 Brake disc becomes vented (KA-RaceIng-1)")
    public void reportScenario1VentedBrakeDisc() {
        answerRequirementMapping("ANF-R01", "HydraulicBrakes");
        answerRemainingDialogsWithDefaults();
        String massBefore = simParameter("FrontBrakeDiscModel", "mass");

        // step 1: Project Manager - requirement "brake discs must be vented" (mapped to HydraulicBrakes)
        addRequirement("ANF-R01", "Brake discs must be vented (weight reduction and cooling)");
        assertTrue(correspondingNames("prj.requirements", "ANF-R01", "requirement", "des.cad").contains("FrontBrakeDisc"));
        assertTrue(correspondingNames("prj.requirements", "ANF-R01", "requirement", "karaceing.bom").contains("DES-FrontBrakeDisc"));
        assertNotified("Design Engineer", "New requirement ANF-R01", "HydraulicBrakes");
        assertNotified("Procurement Manager", "New requirement ANF-R01");

        // the Project Manager records the decision in the brake specification
        change(List.of(Brakesystem.class), view -> ((BrakeDisk) get(root(view, Brakesystem.class), "FrontBrakeDisk")).setVentilated(true));
        assertEquals("vented", bomValue("DES-FrontBrakeDisc", "specification"), "AR1: the BOM orders vented discs");
        assertNotified("Procurement Manager", "DES-FrontBrakeDisc", "specification solid -> vented", "ANF-R01");
        assertNotified("Design Engineer", "requires FrontBrakeDisk to be ventilated", "has no ventilation holes");
        assertEquals("false", simParameter("FrontBrakeDiscModel", "ventilated"), "the simulation follows the CAD model, not the specification");

        // step 4: Design Engineer adds six ventilation holes to the solid disc
        change(List.of(CADDocument.class), view -> {
            Assembly disc = get(root(view, CADDocument.class, "DES"), "FrontBrakeDisc");
            Cylinder body = get(disc, "FrontBrakeDiscBody");
            BooleanOperation cut = CadFactory.eINSTANCE.createBooleanOperation();
            cut.setName("FrontBrakeDiscVented");
            cut.setOperation(BooleanType.CUT);
            cut.setBase(body);
            for (int i = 1; i <= 6; i++) {
                Cylinder hole = CadFactory.eINSTANCE.createCylinder();
                hole.setName("FrontBrakeDiscHole" + i);
                hole.setRadius(0.008);
                hole.setHeight(0.004);
                hole.setVisible(false);
                disc.getChildren().add(hole);
                cut.getTools().add(hole);
            }
            disc.getChildren().add(cut);
        });
        // step 5: propagated to the simulation (K1) and the BOM (K4)
        assertEquals("true", simParameter("FrontBrakeDiscModel", "ventilated"));
        assertTrue(number(simParameter("FrontBrakeDiscModel", "mass")) < number(massBefore), "lighter disc");
        assertTrue(number(bomValue("DES-FrontBrakeDisc", "mass")) < number(massBefore) + 1e-3);
        String sim = assertNotified("Simulation Engineer", "FrontBrakeDiscModel", "mass " + massBefore + " -> ", "inertia", "heatCapacity", "ventilated false -> true");
        assertTrue(sim.contains("ANF-R01"), "change origin: the requirement");
        assertNotified("Kinematics Engineer", "FrontBrakeDisc", "difference");
        assertNotified("Aerodynamics Engineer", "FrontBrakeDisc", "CFD");
        assertNotified("Thermodynamics Engineer", "FrontBrakeDiscHeatSource");
        assertNotified("Validation Engineer", "BrakeDiscSimulationCorrelation");
        assertNotNotified("Project Manager", "ventilation holes, but the brake specification of FrontBrakeDisk");
    }

    // ------------------------------------------------------------------ R2

    /**
     * R2 Design change in the brake caliper (KA-RaceIng-2). The Project Manager adds the requirement that the
     * calipers become two-piece (step 1); Vitruvius informs the Design Engineer about the new requirement traced to
     * the caliper (step 2). The Design Engineer replaces the monobloc housing by two halves and moves the mounting
     * bracket (step 3). Vitruvius creates BOM items for the halves, deletes the item of the old housing and marks
     * the caliper as two-piece in the BOM (step 4), propagates the changed mounting interface to the electrical
     * design (step 5), and informs the Validation Engineer (step 8) and the owner of the brake specification. The
     * Electrical Engineer reviews the sensor and changes its part number, which Vitruvius writes to the BOM (steps
     * 6-7).
     */
    @Test
    @DisplayName("R2 Design change in the brake caliper (KA-RaceIng-2)")
    public void reportScenario2TwoPieceCaliper() {
        answerRequirementMapping("ANF-R02", "HydraulicBrakes");
        answerAlways("Create counterpart", "[Overlap] FrontCaliper", "was added to FrontBrakeCaliper (DES)", "(KA-RaceIng BOM)");
        answerAlways("Item", "[Overlap] Which kind of element should the counterpart of FrontCaliper");
        answerAlways("Delete counterpart", "[Overlap] FrontCaliperBody was removed", "(KA-RaceIng BOM)");
        answerRemainingDialogsWithDefaults();

        addRequirement("ANF-R02", "The brake calipers must be two-piece instead of monobloc (cost, maintainability)");
        assertNotified("Design Engineer", "New requirement ANF-R02", "Vitruvius cannot infer");
        assertTrue(correspondingNames("prj.requirements", "ANF-R02", "requirement", "des.cad").contains("FrontBrakeCaliper"));

        change(List.of(CADDocument.class), view -> {
            CADDocument des = root(view, CADDocument.class, "DES");
            Assembly caliper = get(des, "FrontBrakeCaliper");
            caliper.getChildren().remove(get(caliper, "FrontCaliperBody"));
            caliper.getChildren().add(box("FrontCaliperInnerHalf", 0.12, 0.02, 0.05, material(des, "Aluminium")));
            caliper.getChildren().add(box("FrontCaliperOuterHalf", 0.12, 0.02, 0.05, material(des, "Aluminium")));
            Box bracket = get(caliper, "FrontCaliperMountingBracket");
            bracket.setX(0.012);
        });
        readBom(bom -> {
            assertNotNull(find(bom, "DES-FrontCaliperInnerHalf"), "BOM item for the inner half");
            assertNotNull(find(bom, "DES-FrontCaliperOuterHalf"), "BOM item for the outer half");
            assertNull(find(bom, "DES-FrontCaliperBody"), "the monobloc housing is no longer ordered");
            assertEquals("two-piece", bomValue(bom, "DES-FrontBrakeCaliper", "specification"));
            assertEquals("Aluminium", bomValue(bom, "DES-FrontCaliperInnerHalf", "material"));
            return null;
        });
        assertNotified("Procurement Manager", "DES-FrontBrakeCaliper", "monobloc -> two-piece");
        assertNotified("Electrical Engineer", "FrontCaliperTemperatureSensor", "FrontCaliperMountingBracket", "x 0.0 -> 0.012");
        assertNotified("Validation Engineer", "BrakeAssemblyBuild");
        assertNotified("Project Manager", "two-piece caliper", "says 'monobloc'");

        // steps 6-7: the Electrical Engineer adapts the sensor; the part number reaches the BOM (K11)
        change(List.of(ElectricalDesign.class), view -> ((Device) get(root(view, ElectricalDesign.class), "FrontCaliperTemperatureSensor")).setPartNumber("TS-110"));
        assertEquals("TS-110", bomValue("ELEC-FrontCaliperTemperatureSensor", "partNumber"));
        assertNotified("Procurement Manager", "ELEC-FrontCaliperTemperatureSensor", "partNumber TS-100 -> TS-110");

        // the Project Manager updates the brake specification: now consistent with CAD and BOM
        int before = notificationsTo("Design Engineer").size();
        change(List.of(Brakesystem.class), view -> ((BrakeCaliper) get(root(view, Brakesystem.class), "FrontBrakeCaliper")).setSpecificationType("two-piece"));
        assertEquals(before, notificationsTo("Design Engineer").size(), "CAD already matches the specification");
    }

    // ------------------------------------------------------------------ R3

    /**
     * R3 Brake caliper piston supplier change (KA-RaceIng-3). The original supplier discontinues the titanium
     * pistons; the Project Manager approves steel pistons from a new supplier and updates the BOM (step 1).
     * Vitruvius propagates supplier and material (material data sheet) to the thermal model of the piston, marks the
     * thermal analysis as outdated (step 2), updates the CAD material and informs the Validation Engineer that new
     * pistons have to be integrated in the test bench (step 5). The Thermodynamics Engineer reruns the analysis and
     * lowers the temperature limit (step 3); Vitruvius updates the expected result of the validation test and
     * informs the Validation Engineer (step 4).
     */
    @Test
    @DisplayName("R3 Brake caliper piston supplier change (KA-RaceIng-3)")
    public void reportScenario3PistonSupplierChange() {
        answerRemainingDialogsWithDefaults();

        change(List.of(BOM.class), view -> {
            Item piston = get(root(view, BOM.class), "DES-FrontCaliperPiston");
            for (ParameterValue value : piston.getParameterValues()) {
                if (value instanceof StringParameterValue s && "supplier".equals(s.getDefinition().getName())) {
                    s.setValue("PistonSupplier B");
                } else if (value instanceof StringParameterValue s && "material".equals(s.getDefinition().getName())) {
                    s.setValue("Steel");
                }
            }
        });
        read(List.of(ThermalAnalysisModel.class), view -> {
            ThermalComponent piston = get(root(view, ThermalAnalysisModel.class), "FrontCaliperPistonThermalModel");
            assertEquals("PistonSupplier B", piston.getSupplier());
            assertEquals("Steel", piston.getMaterial());
            assertEquals(45.0, piston.getThermalConductivity(), 1e-9, "material data of steel");
            AnalysisCase analysis = get(root(view, ThermalAnalysisModel.class), "CaliperPistonTemperatureAnalysis");
            assertFalse(analysis.isPassed(), "the analysis is outdated");
            return null;
        });
        read(List.of(CADDocument.class), view -> {
            Solid piston = get(root(view, CADDocument.class, "DES"), "FrontCaliperPiston");
            assertEquals("Steel", piston.getMaterial().getName(), "AR6: CAD material follows the BOM");
            return null;
        });
        assertNotified("Thermodynamics Engineer", "DES-FrontCaliperPiston", "supplier PistonSupplier A -> PistonSupplier B", "CaliperPistonTemperatureAnalysis");
        assertNotified("Validation Engineer", "DES-FrontCaliperPiston", "BrakeAssemblyBuild");
        assertNotified("Design Engineer", "FrontCaliperPiston", "Titanium -> Steel");
        assertTrue(number(bomValue("DES-FrontCaliperPiston", "mass")) > 0.04, "the steel piston is heavier (mass derived from the CAD)");

        // the Thermodynamics Engineer reruns the analysis: lower temperature limit for the steel piston
        change(List.of(ThermalAnalysisModel.class), view -> {
            ThermalComponent piston = get(root(view, ThermalAnalysisModel.class), "FrontCaliperPistonThermalModel");
            piston.setTemperatureLimit(300.0);
            AnalysisCase analysis = get(root(view, ThermalAnalysisModel.class), "CaliperPistonTemperatureAnalysis");
            analysis.setPassed(true);
            analysis.setResult("piston temperature 290 C < limit 300 C");
        });
        read(List.of(TestBenchConfiguration.class), view -> {
            TestCase test = get(root(view, TestBenchConfiguration.class), "CaliperPistonTemperatureTest");
            assertEquals("FrontCaliperPistonThermalModel temperature <= 300.0 C", test.getSteps().get(0).getExpectedResult());
            return null;
        });
        assertNotified("Validation Engineer", "temperature limit", "350.0 -> 300.0", "CaliperPistonTemperatureTest");
    }

    // ------------------------------------------------------------------ R4

    /**
     * R4 Brake pedal material requirement change (KA-RaceIng-4). The Requirements Engineer restricts ANF-217 to
     * steel or aluminium (titanium no longer allowed) (step 1). Vitruvius checks the traced artefacts: the Design
     * Engineer (CAD part) and the Procurement Manager (BOM item of the ordered titanium pedal) are notified, and the
     * pass criterion of the compliance test is updated (steps 2-4). The Design Engineer changes the pedal to
     * aluminium; Vitruvius updates the BOM (material, mass) and confirms the compliance with the requirement
     * (steps 5-6).
     */
    @Test
    @DisplayName("R4 Brake pedal material requirement change (KA-RaceIng-4)")
    public void reportScenario4BrakePedalMaterial() {
        answerRemainingDialogsWithDefaults();

        change(List.of(RequirementsSpecification.class), view -> {
            Requirement anf217 = get(root(view, RequirementsSpecification.class), "ANF-217");
            anf217.getAllowedValues().remove("Titanium");
        });
        assertNotified("Design Engineer", "ANF-217", "now allows {Steel, Aluminium}", "BrakePedalPart (Titanium");
        assertNotified("Procurement Manager", "ANF-217", "DES-BrakePedalPart (Titanium)", "order a compliant part");
        assertNotified("Validation Engineer", "BrakePedalMaterialCompliance", "material in {Steel, Aluminium} (ANF-217)");
        read(List.of(TestBenchConfiguration.class), view -> {
            TestCase test = get(root(view, TestBenchConfiguration.class), "BrakePedalMaterialCompliance");
            assertEquals("material in {Steel, Aluminium} (ANF-217)", test.getPassCriterion());
            return null;
        });

        change(List.of(CADDocument.class), view -> {
            CADDocument des = root(view, CADDocument.class, "DES");
            ((Box) get(des, "BrakePedalPart")).setMaterial(material(des, "Aluminium"));
        });
        assertEquals("Aluminium", bomValue("DES-BrakePedalPart", "material"));
        assertNotified("Procurement Manager", "DES-BrakePedalPart", "material Titanium -> Aluminium");
        assertNotified("Project Manager", "BrakePedalPart", "complies with ANF-217");
    }

    // ------------------------------------------------------------------ R5

    /**
     * R5 Removal of the scatter shield violates regulatory requirements (KA-RaceIng-5). The Design Engineer removes
     * the scatter shield assembly from the CAD model (step 1). Vitruvius identifies the violated requirements traced
     * to it (T 7.3.2 - T 7.3.4: ANF-259 to ANF-265) and notifies the Design Engineer and the Validation Engineer, whose
     * compliance check is affected (steps 2-3). Resolving it (restore or requirement change) is up to the Design
     * Engineer (step 4).
     */
    @Test
    @DisplayName("R5 Removal of the scatter shield violates regulatory requirements (KA-RaceIng-5)")
    public void reportScenario5ScatterShieldRemoval() {
        answerRemainingDialogsWithDefaults();

        change(List.of(CADDocument.class), view -> {
            Assembly guards = get(root(view, CADDocument.class, "DES"), "DrivetrainGuards");
            guards.getChildren().remove(get(guards, "ScatterShield"));
        });
        String violation = assertNotified("Design Engineer", "Removing", "ScatterShield", "violates", "ANF-259");
        assertTrue(violation.contains("ANF-263"), violation);
        assertTrue(notificationsTo("Design Engineer").stream().anyMatch(n -> n.contains("ANF-260")), "requirement of the steel variant");
        assertNotified("Validation Engineer", "ScatterShield", "ScatterShieldCompliance");
        assertEquals(List.of(), correspondingNames("prj.requirements", "ANF-259", "requirement", "des.cad").stream()
                .filter(n -> n.contains("ScatterShield") && !n.contains("Motor") && !n.contains("Split")).toList(), "traces of the removed elements are removed");
    }

    // ------------------------------------------------------------------ R6

    /**
     * R6 Integration of ABS and brake over-travel switch (KA-RaceIng-6). The Project Manager decides to integrate the
     * ABS function and the BOTS into one brake system component (step 1). The Electrical Engineer adds the
     * integrated unit with the signal interfaces of both (it realises the brake over-travel switch, so the BOTS
     * requirements trace to it) and then removes the two separate devices (step 2). Vitruvius
     * detects the changed structure and propagates it to the Simulink model: a block for the integrated unit with
     * its signal parameters is created, the blocks of the separate components are deleted (steps 3-4); the
     * Simulation Engineer is informed to adapt the control logic (step 5).
     */
    @Test
    @DisplayName("R6 Integration of ABS and brake over-travel switch (KA-RaceIng-6)")
    public void reportScenario6IntegratedAbsBots() {
        answerRequirementMapping("ANF-R06", "BrakeOverTravelSwitch");
        answer("Create counterpart", "[Overlap] IntegratedBrakeControlUnit was added to BrakeSensors (ELEC)", "(SIM)");
        answer("Block", "[Overlap] Which kind of element should the counterpart of IntegratedBrakeControlUnit");
        answerAlways("Delete counterpart", "[Overlap] ABSControlUnit was removed", "(SIM)");
        answerAlways("Delete counterpart", "[Overlap] BOTSSwitch was removed", "(SIM)");
        // the integrated unit realises the brake over-travel switch (as the BOTS device did before)
        answer("Existing feature", "[FCPR] The new Device IntegratedBrakeControlUnit");
        answer("BrakeOverTravelSwitch", "[FCPR] To which existing feature should IntegratedBrakeControlUnit");
        answerRemainingDialogsWithDefaults();

        addRequirement("ANF-R06", "ABS and brake over-travel switch shall be integrated into one brake system component");
        assertNotified("Electrical Engineer", "New requirement ANF-R06", "BOTSSwitch");

        change(List.of(ElectricalDesign.class), view -> {
            ElectricalDesign elec = root(view, ElectricalDesign.class);
            Circuit sensors = get(elec, "BrakeSensors");
            sensors.getElements().add(device("IntegratedBrakeControlUnit", DeviceKind.CONTROL_UNIT, "IBC-1", "WheelSpeedFront", "front_wheel_speed",
                    "WheelSpeedRear", "rear_wheel_speed", "ValveCommand", "abs_valve_command", "BOTSState", "bots_sdc_open"));
        });
        // then the two separate devices are removed (the requirements of the BOTS are realised by the integrated unit now)
        change(List.of(ElectricalDesign.class), view -> {
            ElectricalDesign elec = root(view, ElectricalDesign.class);
            Circuit sensors = get(elec, "BrakeSensors");
            sensors.getElements().remove(get(sensors, "ABSControlUnit"));
            Circuit sdc = get(elec, "ShutdownCircuit");
            sdc.getElements().remove(get(sdc, "BOTSSwitch"));
        });
        read(List.of(SimulinkModel.class), view -> {
            SimulinkModel sim = root(view, SimulinkModel.class);
            Block unit = get(sim, "IntegratedBrakeControlUnit");
            assertEquals("BrakeSystemModel", KARaceIngUtil.nameOf(unit.eContainer()));
            assertTrue(unit.getParameters().stream().anyMatch(p -> "signal:BOTSState".equals(p.getName()) && "bots_sdc_open".equals(p.getValue())));
            assertTrue(unit.getParameters().stream().anyMatch(p -> "signal:ValveCommand".equals(p.getName())));
            assertNull(find(sim, "ABSControl"), "the separate ABS block is removed");
            assertNull(find(sim, "BOTSSwitchLogic"), "the separate BOTS block is removed");
            return null;
        });
        assertNotified("Simulation Engineer", "New block", "IntegratedBrakeControlUnit", "signal:BOTSState");
        assertTrue(correspondingNames("elec.electrical", "IntegratedBrakeControlUnit", "overlap", "sim.simulink").contains("IntegratedBrakeControlUnit"));
        assertEquals(List.of("BrakeOverTravelSwitch"), featuresOf("elec.electrical", "IntegratedBrakeControlUnit"));
        assertTrue(correspondingNames("prj.requirements", "ANF-221", "requirement", "elec.electrical").contains("IntegratedBrakeControlUnit"),
                "the BOTS requirements trace to the integrated unit");
        assertNotNotified("Electrical Engineer", "Removing ELEC/ShutdownCircuit/BOTSSwitch");
    }

    // ------------------------------------------------------------------ R7

    /**
     * R7 Missing wear tracking (KA-RaceIng-7). After several races the maintenance team records the wear in the brake
     * specification: worn pads with wear warning and a disc at its minimum thickness (step 1). Vitruvius propagates the
     * degradation to the brake model of the simulation (step 2) and informs the Simulation Engineer to rerun the
     * performance analyses (step 3) and the Project Manager about the affected maintenance planning and operational
     * requirements (steps 4-6).
     */
    @Test
    @DisplayName("R7 Missing wear tracking (KA-RaceIng-7)")
    public void reportScenario7WearTracking() {
        answerRemainingDialogsWithDefaults();

        change(List.of(Brakesystem.class), view -> {
            Brakesystem brake = root(view, Brakesystem.class);
            BrakePad pads = get(brake, "FrontBrakePads");
            pads.setThicknessInMM(5);
            pads.setWearWarning(true);
            ((BrakeDisk) get(brake, "FrontBrakeDisk")).setBrakeDiskThicknessInMM(3);
        });
        assertEquals("5", simParameter("FrontBrakePadModel", "padThickness"));
        assertEquals("true", simParameter("FrontBrakePadModel", "wearWarning"));
        assertEquals("3", simParameter("FrontBrakeDiscModel", "discThickness"));
        assertNotified("Simulation Engineer", "Wear of FrontBrakePads", "padThickness 8 -> 5");
        assertNotified("Simulation Engineer", "FrontBrakeDiscModel", "discThickness 4 -> 3");
        assertNotified("Project Manager", "Wear of FrontBrakeDisk", "minimum thickness", "requirements traced to FrontBrakeDisk");
    }

    // ------------------------------------------------------------------ R8

    /**
     * R8 Introduction of an intermediate tyre configuration (KA-RaceIng-8). The regulations permit an intermediate
     * tyre; the Design Engineer (here: the kinematics CAD model of the wheels) introduces the configuration (step 1),
     * which becomes a new optional feature below Wheels. Vitruvius detects the added configuration and creates a
     * parameter set in the tyre model of the simulation, initialised from the slick and rain tyre sets (step 2); the
     * Simulation Engineer reviews it (step 3) and the Project Manager is informed that the performance analyses for
     * slightly wet conditions have to be reevaluated (steps 4-5).
     */
    @Test
    @DisplayName("R8 Introduction of an intermediate tyre configuration (KA-RaceIng-8)")
    public void reportScenario8IntermediateTyre() {
        answer("New feature", "[FCPR] The new Assembly IntermediateTyres");
        answer("Optional", "[FCPR] In which kind of group below Wheels should the new feature IntermediateTyres");
        answer("Create counterpart", "[Overlap] IntermediateTyres was added to Wheels (KIN)", "TyreModel (SIM)");
        answer("Block", "[Overlap] Which kind of element should the counterpart of IntermediateTyres");
        answerRemainingDialogsWithDefaults();

        change(List.of(CADDocument.class), view -> {
            Assembly wheels = get(root(view, CADDocument.class, "KIN"), "Wheels");
            Assembly intermediate = CadFactory.eINSTANCE.createAssembly();
            intermediate.setName("IntermediateTyres");
            intermediate.setLabel("IntermediateTyres");
            wheels.getChildren().add(intermediate);
        });
        assertNotNull(read(List.of(UVLModel.class), view -> KARaceIngUtil.findFeature(root(view, UVLModel.class), "IntermediateTyres")));
        assertAddedUnselected("EV3", "IntermediateTyres");
        assertEquals("1.4", simParameter("IntermediateTyres", "grip"), "mean of slick (1.6) and rain (1.2) tyres");
        assertEquals("0.0175", simParameter("IntermediateTyres", "rollingResistance"));
        assertNotified("Simulation Engineer", "New parameter set", "IntermediateTyres", "DryTyreParameterSet, WetTyreParameterSet");
        assertNotified("Project Manager", "IntermediateTyres", "Performance analyses");
    }

    // ------------------------------------------------------------------ R9

    /**
     * R9 Addition of a brake over-travel monitoring sensor (KA-RaceIng-9). New requirements mandate over-travel
     * detection and a warning (step 1). The Electrical Engineer integrates a travel sensor with its signals (step
     * 2). Vitruvius detects the new component and propagates it with its signal interfaces to the brake model of the
     * Simulink model (step 3); the Simulation Engineer is asked to add the warning logic (step 4). Project
     * documentation (Wiki model, steps 5-6) is not part of the VSUM.
     */
    @Test
    @DisplayName("R9 Addition of a brake over-travel monitoring sensor (KA-RaceIng-9)")
    public void reportScenario9BotsSensor() {
        answerRequirementMapping("ANF-R09", "BrakeOverTravelSwitch");
        answer("Create counterpart", "[Overlap] BOTSTravelSensor was added to BrakeSensors (ELEC)", "(SIM)");
        answer("Block", "[Overlap] Which kind of element should the counterpart of BOTSTravelSensor");
        answerRemainingDialogsWithDefaults();

        addRequirement("ANF-R09", "Brake pedal over-travel shall be detected and a warning shown to the driver");
        assertNotified("Electrical Engineer", "New requirement ANF-R09", "BOTSSwitch");

        change(List.of(ElectricalDesign.class), view -> ((Circuit) get(root(view, ElectricalDesign.class), "BrakeSensors")).getElements()
                .add(device("BOTSTravelSensor", DeviceKind.SENSOR, "BTS-1", "PedalTravel", "bots_pedal_travel", "Warning", "bots_warning")));
        read(List.of(SimulinkModel.class), view -> {
            Block block = get(root(view, SimulinkModel.class), "BOTSTravelSensor");
            assertTrue(block.getParameters().stream().anyMatch(p -> "signal:Warning".equals(p.getName()) && "bots_warning".equals(p.getValue())));
            return null;
        });
        assertNotified("Simulation Engineer", "BOTSTravelSensor", "signal:PedalTravel");

        // a changed signal of the sensor reaches the simulation (AR11)
        change(List.of(ElectricalDesign.class), view -> {
            Device sensor = get(root(view, ElectricalDesign.class), "BOTSTravelSensor");
            sensor.getPins().stream().filter(p -> "Warning".equals(p.getName())).findFirst().orElseThrow().setSignal("bots_overtravel_warning");
        });
        assertEquals("bots_overtravel_warning", simParameter("BOTSTravelSensor", "signal:Warning"));
        assertNotified("Simulation Engineer", "signal:Warning bots_warning -> bots_overtravel_warning");
    }

    // ------------------------------------------------------------------ R10

    /**
     * R10 Update of the CAD tooling (KA-RaceIng-10). The CAD tool or exchange format of the brake system changes.
     * This is out of scope: the metamodels describe the content of the models, not the tool or file format they are
     * edited with, so there is no change in the VSUM that could be propagated.
     */
    @Test
    @Disabled("Out of scope: the CAD tool / exchange format is not represented in the metamodels of the VSUM")
    @DisplayName("R10 Update of the CAD tooling (KA-RaceIng-10)")
    public void reportScenario10CadTooling() {
        // intentionally empty
    }
}
