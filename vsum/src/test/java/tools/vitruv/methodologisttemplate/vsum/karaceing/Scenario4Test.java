package tools.vitruv.methodologisttemplate.vsum.karaceing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import UVLPackage.UVLModel;
import configurationPackage.Configuration;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.model.bom.BOM;
import tools.vitruv.methodologisttemplate.model.cad.CADDocument;
import tools.vitruv.methodologisttemplate.model.requirements.RequirementsSpecification;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalAnalysisModel;

/**
 * Scenario 4: a feature is removed in the problem space of one domain (concrete instances 4.1-A to
 * 4.4-B). The Feature CPRs for removed features ask what happens with the realising artefacts (delete,
 * keep and re-map to the parent feature, keep without feature); the overlap CPRs propagate deletions to
 * overlapping artefacts and the BOM; the ConfigCPRs remove the feature from all configurations.
 */
public class Scenario4Test extends KARaceIngTestBase {

    @BeforeEach
    void loadInitialState(@TempDir Path storageFolder) {
        // initial state of the product line: the persisted VSUM in src/test/resources/initialstate
        openInitialState(storageFolder);
    }


    private boolean featureExists(String name) {
        return read(List.of(UVLModel.class), view -> KARaceIngUtil.findFeature(root(view, UVLModel.class), name) != null);
    }

    private void assertNotInAnyConfiguration(String feature) {
        for (String car : List.of("EV3", "CV2", "CV1")) {
            assertNull(configFeature(readConfiguration(car), feature), feature + " removed from " + car);
        }
    }

    private boolean existsIn(Class<? extends org.eclipse.emf.ecore.EObject> rootType, String rootName, String element) {
        return read(List.of(rootType), view -> find(rootName == null ? root(view, rootType) : root(view, rootType, rootName), element) != null);
    }

    /**
     * 4.1-A Upright mass reduction dropped for the hydrogen car (KIN). kin.UprightMassReduction is removed
     * (its value constraints are not part of the UVL model). The lightweight upright variant is still required for EV cars: it is kept in
     * the KIN CAD model and re-mapped to kin.Suspension. CV2 no longer carries the removed feature.
     */
    @Test
    @DisplayName("4.1-A Upright mass reduction dropped for the hydrogen car (KIN)")
    public void scenario41A() {
        answer("Keep artefact and map it to Suspension", "[FCPR] The feature UprightMassReduction was removed");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "UprightMassReduction"));

        assertFalse(featureExists("UprightMassReduction"));
        assertTrue(existsIn(CADDocument.class, "KIN", "UprightMassReduction"), "lightweight upright kept");
        assertEquals(List.of("Suspension"), featuresOf("kin.cad", "UprightMassReduction"));
        assertNotInAnyConfiguration("UprightMassReduction");
    }

    /**
     * 4.1-B Tank integration into an existing car (DES). des.TankSystemIntegration (and its condition
     * {@code => prj.Conversion}) is removed. The CV1-chassis adapter frame is still needed for CV2 maintenance:
     * it is kept and mapped to des.HydrogenStorageSystem.
     */
    @Test
    @DisplayName("4.1-B Tank integration into an existing car (DES)")
    public void scenario41B() {
        answer("Keep artefact and map it to HydrogenStorageSystem", "[FCPR] The feature TankSystemIntegration was removed");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "TankSystemIntegration"));

        assertTrue(existsIn(CADDocument.class, "DES", "CV1ChassisAdapterFrame"));
        assertTrue(featuresOf("des.cad", "CV1ChassisAdapterFrame").contains("HydrogenStorageSystem"));
        assertNotInAnyConfiguration("TankSystemIntegration");
    }

    /**
     * 4.2-A Backfire protection removed (THERMO / DES). thermo.BackfireProtection is removed and its
     * realisation (with the backfire pressure-relief analysis) deleted. The overlap CPR asks DES about the
     * relief valve and flame arrestor in the intake system, which are deleted as well.
     */
    @Test
    @DisplayName("4.2-A Backfire protection removed (THERMO / DES)")
    public void scenario42A() {
        answerAlways("Delete artefact", "[FCPR] The feature BackfireProtection was removed");
        answer("Delete counterpart", "[Overlap] BackfireReliefAnalysis was removed", "ReliefValveAndFlameArrestor (DES)");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "BackfireProtection"));

        assertFalse(existsIn(ThermalAnalysisModel.class, null, "BackfireProtection"));
        assertFalse(existsIn(ThermalAnalysisModel.class, null, "BackfireReliefAnalysis"));
        assertFalse(existsIn(CADDocument.class, "DES", "ReliefValveAndFlameArrestor"), "relief valve removed from the intake");
        assertNotInAnyConfiguration("BackfireProtection");
    }

    /**
     * 4.2-B Mechanically attached bracing removed (CRASH / DES). crash.MechanicallyAttachedBracing is removed and
     * the lug and sleeve joints are deleted. The critical bolts of these joints in the DES fastener list and
     * the BOM items are removed via the overlap CPRs.
     */
    @Test
    @DisplayName("4.2-B Mechanically attached bracing removed (CRASH / DES)")
    public void scenario42B() {
        answerAlways("Delete artefact", "[FCPR] The feature MechanicallyAttachedBracing was removed");
        answerAlways("Delete counterpart", "[Overlap]", "was removed", "(KA-RaceIng BOM)");
        answer("Delete counterpart", "[Overlap] MechanicallyAttachedBracing was removed", "CriticalBracingBolts (DES)");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "MechanicallyAttachedBracing"));

        assertFalse(existsIn(CADDocument.class, "CRASH", "LugJoints"));
        assertFalse(existsIn(CADDocument.class, "DES", "CriticalBracingBolts"), "critical bolts removed from the fasteners");
        assertFalse(existsIn(BOM.class, null, "DES-CriticalBracingBolts"));
        assertFalse(existsIn(BOM.class, null, "CRASH-LugJoints"));
        assertNotInAnyConfiguration("MechanicallyAttachedBracing");
    }

    /**
     * 4.3-A Engine conversion to hydrogen (THERMO / DES). thermo.EngineConversionToHydrogen is removed and the
     * conversion analysis deleted; DES removes the conversion parts (adapted cylinder head, injector
     * adapters) from the engine CAD, their BOM items are removed; the CV1-chassis adapter is kept.
     * des.CombustionEngine keeps its realisation.
     */
    @Test
    @DisplayName("4.3-A Engine conversion to hydrogen (THERMO / DES)")
    public void scenario43A() {
        answerAlways("Delete artefact", "[FCPR] The feature EngineConversionToHydrogen was removed");
        answer("Delete counterpart", "[Overlap] EngineConversionAnalysis was removed", "HydrogenConversionParts (DES)");
        answerAlways("Delete counterpart", "[Overlap]", "was removed", "(KA-RaceIng BOM)");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "EngineConversionToHydrogen"));

        assertFalse(existsIn(CADDocument.class, "DES", "HydrogenConversionParts"));
        assertFalse(existsIn(BOM.class, null, "DES-AdaptedCylinderHead"));
        assertTrue(existsIn(CADDocument.class, "DES", "CV1ChassisAdapterFrame"), "adapter frame kept");
        assertEquals(List.of("CombustionEngine"), artefactsOf("CombustionEngine", "des.cad"));
        assertNotInAnyConfiguration("EngineConversionToHydrogen");
    }

    /**
     * 4.4-A Conversion development type removed (PRJ / DES). prj.DevelopmentType (with NewDevelopment and
     * Conversion) is removed and its sections in the requirements specification are deleted. DES then
     * removes the CV1-chassis adapter; since des.TankSystemIntegration has no other realisation, the reverse
     * Feature CPR removes the feature. CV2 contains neither feature any more.
     */
    @Test
    @DisplayName("4.4-A Conversion development type removed (PRJ / DES)")
    public void scenario44A() {
        answerAlways("Delete artefact", "[FCPR] The feature DevelopmentType was removed");
        answer("Remove feature", "[FCPR] The feature TankSystemIntegration has no realisation");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "DevelopmentType"));
        change(List.of(CADDocument.class), view -> EcoreUtil.remove(get(root(view, CADDocument.class, "DES"), "CV1ChassisAdapterFrame")));

        assertFalse(featureExists("Conversion"));
        assertFalse(featureExists("TankSystemIntegration"));
        assertFalse(existsIn(RequirementsSpecification.class, null, "DevelopmentType"));
        Configuration cv2 = readConfiguration("CV2");
        assertNull(configFeature(cv2, "Conversion"));
        assertNull(configFeature(cv2, "TankSystemIntegration"));
    }

    /**
     * 4.4-B Retiring the FS2019 rule set (PRJ / CRASH). prj.FS2019 is removed with its rule baseline; CRASH
     * deletes the 2019 harness brackets and bracing joints and removes HarnessBracketDesign,
     * LapBeltSeatOpening and MechanicallyAttachedBracing. The features are removed from the
     * configuration of CV1 (an FS2019 car).
     */
    @Test
    @DisplayName("4.4-B Retiring the FS2019 rule set (PRJ / CRASH)")
    public void scenario44B() {
        answerAlways("Delete artefact", "[FCPR] The feature FS2019 was removed");
        answerAlways("Delete artefact", "[FCPR] The feature", "was removed", "in the CRASH model");
        answerAlways("Delete counterpart", "[Overlap]", "was removed", "(KA-RaceIng BOM)");
        answerRemainingDialogsWithDefaults();

        changeFeatureModel(model -> removeFeature(model, "FS2019"));
        changeFeatureModel(model -> {
            removeFeature(model, "HarnessBracketDesign");
            removeFeature(model, "LapBeltSeatOpening");
            removeFeature(model, "MechanicallyAttachedBracing");
        });

        assertFalse(existsIn(RequirementsSpecification.class, null, "FS2019"));
        assertFalse(existsIn(CADDocument.class, "CRASH", "HarnessBracketDesign"));
        assertFalse(existsIn(CADDocument.class, "CRASH", "MechanicallyAttachedBracing"));
        Configuration cv1 = readConfiguration("CV1");
        assertNull(configFeature(cv1, "FS2019"));
        assertNull(configFeature(cv1, "HarnessBracketDesign"));
        assertNotNull(configFeature(readConfiguration("EV3"), "FS2025"));
    }
}
