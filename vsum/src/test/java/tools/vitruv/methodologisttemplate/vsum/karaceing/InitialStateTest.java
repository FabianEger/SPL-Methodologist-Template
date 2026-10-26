package tools.vitruv.methodologisttemplate.vsum.karaceing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.Feature;
import UVLPackage.UVLModel;
import configurationPackage.Configuration;
import configurationPackage.ConfigurationModel;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.model.cad.CADDocument;

/**
 * Checks the initial state of the KA-RaceIng product line after the VSUM was opened on the persisted
 * state in src/test/resources/initialstate: all models are loaded, every feature of a stakeholder role
 * is realised by an artefact of the role's model (correspondence from vsum/correspondences.correspondence),
 * overlapping artefacts and requirement traces are linked, and the configurations contain all features.
 */
public class InitialStateTest extends KARaceIngTestBase {

    private static final Map<String, String> DOMAIN_FILES = Map.of(
            "ProjectAndRequirements", "prj.requirements", "DesignEngineering", "des.cad", "CrashStructure", "crash.cad",
            "Kinematics", "kin.cad", "Aerodynamics", "aero.cad", "Thermodynamics", "thermo.thermal",
            "ElectricalEngineering", "elec.electrical", "Simulation", "sim.simulink", "Validation", "val.testbench");

    @BeforeEach
    void loadInitialState(@TempDir Path storageFolder) {
        // initial state of the product line: the persisted VSUM in src/test/resources/initialstate
        openInitialState(storageFolder);
    }

    @Test
    public void allModelsAreLoaded() {
        for (String file : InitialStateLoader.MODEL_FILES) {
            assertNotNull(inVsum(file, null), "model " + file + " is in the VSUM");
        }
        read(List.of(UVLModel.class, ConfigurationModel.class, CADDocument.class), view -> {
            UVLModel uvl = root(view, UVLModel.class);
            long features = 0;
            for (var it = uvl.eAllContents(); it.hasNext();) {
                if (it.next() instanceof Feature) {
                    features++;
                }
            }
            assertEquals(259, features, "features incl. the root feature");
            assertEquals(4, view.getRootObjects(CADDocument.class).size(), "CAD documents of DES, CRASH, KIN, AERO");
            assertEquals(3, root(view, ConfigurationModel.class).getConfiguration().size(), "EV3, CV2, CV1");
            return null;
        });
    }

    @Test
    public void everyFeatureIsRealisedInItsDomain() {
        EObject uvl = inVsum("karaceing.uvl", null);
        for (var it = uvl.eAllContents(); it.hasNext();) {
            if (it.next() instanceof Feature feature && KARaceIngUtil.domainRootOf(feature) != null) {
                String file = DOMAIN_FILES.get(KARaceIngUtil.domainRootOf(feature).getName());
                assertFalse(artefactsOf(feature.getName(), file).isEmpty(), "feature " + feature.getName() + " is realised in " + file);
            }
        }
    }

    /** Rule: there is no artefact without a feature (also checked after every scenario test, see KARaceIngTestBase). */
    @Test
    public void everyArtefactCorrespondsToAFeature() {
        assertTrue(artefactsWithoutFeature().isEmpty(), "artefacts without feature: " + artefactsWithoutFeature());
    }

    @Test
    public void featuresAreLinkedToConfigurationFeatures() {
        assertEquals(3, correspondingNames("karaceing.uvl", "MaxVehicleMass", "", "karaceing.config").size());
        assertTrue(correspondences().getCorrespondingEObjects(inVsum("karaceing.uvl", null), "").stream()
                .anyMatch(o -> o instanceof ConfigurationModel), "UVL model corresponds to the configuration model");
    }

    @Test
    public void overlappingArtefactsAreLinked() {
        // interface constraint elec.SDCHydrogen => des.HydrogenTank
        assertTrue(correspondingNames("elec.electrical", "SDCHydrogen", "overlap", "des.cad").contains("HydrogenTank"));
        // scenario overlaps
        assertTrue(correspondingNames("crash.cad", "LidarBracket", "overlap", "sim.simulink").contains("LidarPoseMainHoop"));
        assertTrue(correspondingNames("des.cad", "HydrogenTank", "overlap", "karaceing.bom").contains("DES-HydrogenTank"));
        assertTrue(correspondingNames("des.brakesystem", "FrontBrakeDisk", "overlap", "des.cad").contains("FrontBrakeDisc"));
        assertTrue(correspondingNames("des.brakesystem", "BRAKE", "overlap", "des.cad").contains("HydraulicBrakes"));
        // report overlaps (element level, see docs/OVERLAP_CATALOGUE.md)
        assertTrue(correspondingNames("des.cad", "FrontBrakeDisc", "overlap", "sim.simulink").contains("FrontBrakeDiscModel"));
        assertTrue(correspondingNames("karaceing.bom", "DES-FrontCaliperPiston", "overlap", "thermo.thermal").contains("FrontCaliperPistonThermalModel"));
        assertTrue(correspondingNames("elec.electrical", "ABSControlUnit", "overlap", "sim.simulink").contains("ABSControl"));
        // requirement trace ANF-1001 (analogue connection of tank sensors to the SDC) -> realisation of elec.SDCHydrogen
        assertTrue(correspondingNames("prj.requirements", "ANF-1001", "requirement", "elec.electrical").contains("SDCHydrogen"));
        // requirements trace to every artefact of their features, BOM and brake specification included
        List<String> pedal = correspondingNames("prj.requirements", "ANF-217", "requirement", null);
        assertTrue(pedal.containsAll(List.of("BrakePedal", "BrakePedalPart", "DES-BrakePedalPart", "BrakePedalMaterialCompliance")), pedal.toString());
        assertTrue(correspondingNames("prj.requirements", "ANF-205", "requirement", "des.brakesystem").contains("FrontBrakeDisk"));
        assertTrue(correspondingNames("prj.requirements", "ANF-205", "requirement", "karaceing.bom").contains("DES-FrontBrakeDisc"));
    }

    @Test
    public void configurationsContainTheDecisionsOfTheCars() {
        Configuration ev3 = readConfiguration("EV3");
        Configuration cv2 = readConfiguration("CV2");
        Configuration cv1 = readConfiguration("CV1");
        for (Configuration configuration : List.of(ev3, cv2, cv1)) {
            assertEquals(259, configuration.getFeature().size());
        }
        assertTrue(configFeature(ev3, "Electric").isIsSelected());
        assertTrue(configFeature(cv2, "H2Combustion").isIsSelected());
        assertTrue(configFeature(cv1, "Petrol").isIsSelected());
        assertFalse(configFeature(cv1, "FS2025").isIsSelected());
    }
}
