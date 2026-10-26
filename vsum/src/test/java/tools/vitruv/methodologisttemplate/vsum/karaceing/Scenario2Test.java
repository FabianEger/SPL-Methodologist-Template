package tools.vitruv.methodologisttemplate.vsum.karaceing;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Scenario 2: a new constraint is added to the variability model of one domain (concrete instances
 * 2.1-A to 2.1-H). Expected: the configurations that are no longer valid products are detected.
 *
 * <p>Not executable in the VSUM yet: cross-tree constraints cannot be expressed with the provided UVL metamodel (no feature references in Reference / TermReference). They will be added as VitruvOCL constraints, which will check the
 * configurations against them. The tests document the instances (data and expected outcome in the
 * Javadoc); the expected results were checked against the three configurations with the Python
 * twin of the check in karaceing-generator.
 */
@Disabled("Cross-tree constraints cannot be expressed with the provided UVL metamodel (no feature references in Reference / TermReference); they will be added as VitruvOCL constraints")
public class Scenario2Test {

    /**
     * 2.1-A Lower mass target for hydrogen cars (PRJ): {@code prj.Hydrogen => prj.MaxVehicleMass == 220} is
     * replaced by {@code == 200}. CV2 (220 kg) is no longer a valid product.
     */
    @Test
    @DisplayName("2.1-A Lower mass target for hydrogen cars (PRJ)")
    public void scenario21A() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-B Lower mass target for non-hydrogen cars (PRJ): {@code !prj.Hydrogen => prj.MaxVehicleMass == 175}
     * becomes {@code == 170}. EV3 and CV1 (both 175 kg) are invalid, CV1 unintentionally.
     */
    @Test
    @DisplayName("2.1-B Lower mass target for non-hydrogen cars (PRJ)")
    public void scenario21B() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-C Minimum temperature resistance 100 degC (THERMO): {@code prj.FS2025 => thermo.MinTemperatureResistance == 120}
     * becomes {@code == 100}. EV3 and CV2 (value 120) are no longer valid; CV1 (FS2019) is not affected.
     */
    @Test
    @DisplayName("2.1-C Minimum temperature resistance 100 degC (THERMO)")
    public void scenario21C() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-D Tank system in the side pod becomes mandatory (DES): {@code prj.Hydrogen => des.SidePodMounting}.
     * CV2 (tank behind the driver) is no longer valid.
     */
    @Test
    @DisplayName("2.1-D Tank system in the side pod becomes mandatory (DES)")
    public void scenario21D() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-E Water injection becomes mandatory (THERMO): {@code thermo.HydrogenCombustion => thermo.WaterInjection}.
     * CV2 without water injection is no longer valid.
     */
    @Test
    @DisplayName("2.1-E Water injection becomes mandatory (THERMO)")
    public void scenario21E() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-F VDC becomes mandatory for electric cars (SIM): {@code prj.Electric => sim.VehicleDynamicsControl}.
     * EV3 derived without VDC is no longer valid.
     */
    @Test
    @DisplayName("2.1-F VDC becomes mandatory for electric cars (SIM)")
    public void scenario21F() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-G Aero balance target for hydrogen cars (AERO): {@code aero.AeroBalance & prj.Hydrogen => aero.AeroBalance == 65}.
     * No existing product becomes invalid (CV2 has no aero-balance target), but a CV2-based product with
     * the target and another value would be: the check distinguishes both cases.
     */
    @Test
    @DisplayName("2.1-G Aero balance target for hydrogen cars (AERO)")
    public void scenario21G() {
        // to be implemented with VitruvOCL constraints
    }

    /**
     * 2.1-H Older rule sets break the FS2019 / FS2025 assumption (CRASH). The rule sets FS2023 and FS2024
     * of the historic cars EV1 / EV2 are added, together with
     * {@code prj.FS2023 | prj.FS2024 => crash.MechanicallyAttachedBracing}. The EV1 configuration that was
     * derived without the bracing is not valid.
     */
    @Test
    @DisplayName("2.1-H Older rule sets break the FS2019 / FS2025 assumption (CRASH)")
    public void scenario21H() {
        // to be implemented with VitruvOCL constraints
    }

}
