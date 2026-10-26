package tools.vitruv.methodologisttemplate.vsum.karaceing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Scenario 5: a constraint is removed from the variability model of one domain (concrete instances
 * 5.1-A to 5.1-G). Expected: configurations that were invalid because of the constraint become valid.
 *
 * <p>Not executable in the VSUM yet: cross-tree constraints cannot be expressed with the provided UVL metamodel (no feature references in Reference / TermReference). They will be added as VitruvOCL constraints, which will check the
 * configurations against them. The tests document the instances (data and expected outcome in the
 * Javadoc); the expected results were checked against the three configurations with the Python
 * twin of the check in karaceing-generator.
 */
@Disabled("Cross-tree constraints cannot be expressed with the provided UVL metamodel (no feature references in Reference / TermReference); they will be added as VitruvOCL constraints")
public class Scenario5Test {

    /**
     * 5.1-A {@code prj.Hydrogen => prj.FS2025} (PRJ) is removed. A hydrogen product for the new rule set
     * FS2026 was invalid because of this constraint; the violation disappears.
     */
    @Test
    @DisplayName("5.1-A Hydrogen => FS2025 (PRJ)")
    public void scenario51A() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 5.1-B {@code aero.ActiveAirDevice => prj.FS2025} (AERO) is removed. A product with an active air device
     * under another rule set (here FS2019) is no longer invalid because of it.
     */
    @Test
    @DisplayName("5.1-B aero.ActiveAirDevice => prj.FS2025 (AERO)")
    public void scenario51B() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 5.1-C FS2025-only presence conditions in DES ({@code des.ForwardEdgeRadius <=> prj.FS2025},
     * {@code des.RubberLineFittings <=> des.ReinforcedRubberLine & prj.FS2025}) are removed. A CV1 variant with
     * the forward edge radius becomes valid.
     */
    @Test
    @DisplayName("5.1-C FS2025-only presence conditions in DES")
    public void scenario51C() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 5.1-D Conversion preconditions (DES, THERMO) are removed: {@code des.TankSystemIntegration => prj.Conversion}
     * and {@code thermo.EngineConversionToHydrogen => prj.Conversion}. A new-development hydrogen product with
     * these goals becomes valid.
     */
    @Test
    @DisplayName("5.1-D Conversion preconditions (DES, THERMO)")
    public void scenario51D() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 5.1-E Upright mass value constraints (KIN) are removed ({@code !prj.Petrol => kin.UprightMassReduction == 10},
     * {@code prj.Petrol => kin.UprightMassReduction == 5}). A CV3 product without upright mass reduction
     * (value 0) becomes valid.
     */
    @Test
    @DisplayName("5.1-E Upright mass value constraints (KIN)")
    public void scenario51E() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 5.1-F Hydrogen mass constraint (PRJ) {@code prj.Hydrogen => prj.MaxVehicleMass == 220} is removed (to be
     * replaced later); any hydrogen mass value is temporarily valid.
     */
    @Test
    @DisplayName("5.1-F Hydrogen mass constraint replaced (PRJ)")
    public void scenario51F() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 5.1-G FS2019 value constraints (PRJ) are removed ({@code prj.FS2019 => des.ScatterShieldMinThickness == 1},
     * {@code prj.FS2019 => elec.LVMaxACVoltage == 25}, {@code prj.FS2019 => thermo.MinTemperatureResistance == 125}).
     * CV1 is no longer checked against its values: a CV1 variant with other values becomes valid.
     */
    @Test
    @DisplayName("5.1-G FS2019 value constraints (PRJ)")
    public void scenario51G() {
        // to be implemented with VitruvOCL constraints
    }

}
