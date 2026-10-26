# KA-RaceIng scenarios of the TechReport in the VSUM

This document maps the KA-RaceIng scenarios of the *Inconsistency Scenarios* TechReport (KA-RaceIng-1 to
KA-RaceIng-10, here R1–R10) onto the KA-RaceIng VSUM. For each scenario it describes the models and instances
involved, the semantic overlaps it needs and what Vitruvius does in the *proposed consistency preservation*.
Each scenario is executed by `ReportScenarioTest` (`vsum/src/test/java/.../karaceing`).

Related documents:
- `INITIAL_STATE.md` explains the initial state (section 6a lists the report artefacts);
- `docs/OVERLAP_CATALOGUE.md` lists all 217 overlaps with their kind (generated);
- `KARACEING.md` covers build, reactions and tests;
- the explorer `docs/initial_state_explorer.html` shows every scenario as a diff of the initial state,
  including the notifications per change (family "R").

---

## 1. Mapping the report artefacts onto the VSUM

The decision (2026-10-26) was to map the report onto the existing models instead of adding new ones. The
feature model is unchanged; the metamodels got three small additions in the new metamodels only.

| Artefact in the report | Model in the VSUM |
|---|---|
| CAD Model | `des.cad` (DES), `kin.cad` (KIN, wheels / tyres), `aero.cad` (AERO) |
| Brake Specification Model | `des.brakesystem` (`brakesystem.ecore` of the template) |
| SysML Requirements / Requirements Specification / Requirements Model | `prj.requirements` |
| SysML Model (architecture), Sensor Overview | `elec.electrical` (devices, pins with signals) |
| Simulink Model, Simulation Model | `sim.simulink` (`BrakeSystemModel`, `TyreModel`) |
| Bill of Materials, Procurement Data, Supplier / Material Data Sheets | `karaceing.bom` (item parameters `mass`, `material`, `supplier`, `specification`, `partNumber`) |
| Thermodynamic Analysis Model | `thermo.thermal` (`ThermalComponent`, `AnalysisCase`) |
| Validation Test Configuration / Test-Bench | `val.testbench` (`BrakeValidation` suite) |
| Wiki Model, Performance Analysis Reports, Inventory Model, CAD tool | not in the VSUM (documented, not executed) |

Metamodel additions (only in the new metamodels):
- `Requirement.allowedValues` – the values a requirement permits (R4);
- `Device.mountingPosition` – the CAD element a device is mounted on (R2);
- `ThermalComponent` – a thermal element with material, supplier, conductivity, heat capacity and
  temperature limit (the supplier data sheet of a part, R3).

The provided metamodels (UVL, configuration, CAD, brake system, BOM, Simulink) are unchanged.

## 2. Overlap kinds and rules

The scenarios need eleven kinds of semantic overlap. Every overlap correspondence of the initial state has one
(`docs/OVERLAP_CATALOGUE.md`). The rules that propagate them are implemented in
`consistency/.../karaceing/KARaceIngOverlapRules.java` and run in the generated `Overlap<Src>To<Tgt>`
reactions on every attribute, reference and containment change.

| Kind | Overlap | Report scenarios | Rules |
|---|---|---|---|
| K1 | CAD geometry / mass → SIM parameters | R1, R7 | AR2 |
| K2 | CAD ↔ THERMO thermal model | R1, R3 | AR2, AR6 |
| K3 | CAD ↔ ELEC mounting / packaging | R2, R9 | AR4 |
| K4 | CAD part ↔ BOM item | R1, R2, R4 | AR2 |
| K5 | brake specification ↔ CAD / BOM / SIM / ELEC | R1, R2, R6, R7 | AR1, AR3, AR12 |
| K6 | BOM supplier / material data ↔ THERMO | R3 | AR6 |
| K7 | artefact ↔ validation (test cases, limits) | R2, R3, R4, R5 | AR7, AR8, AR10 |
| K8 | ELEC signals ↔ SIM control model | R6, R9 | AR11, AR13 |
| K9 | CAD ↔ CAD across roles | R1 (FT-08, FT-09) | AR2 |
| K10 | KIN tyres ↔ SIM tyre parameter sets | R8 | AR13 |
| K11 | ELEC component ↔ BOM item | R2, R6 | AR5 |
| (trace) | requirement → every artefact of its features | R1, R2, R4, R5, R7, R9 | AR8–AR10, new-requirement notification |

| Rule | What Vitruvius does |
|---|---|
| AR1 | Brake specification `ventilated` → BOM `specification` solid / vented; if the CAD disc has no (or has) holes, the Design Engineer is notified (not automatically resolvable). |
| AR2 | A CAD geometry, material or structure change → mass, material and variant of the BOM item; mass, inertia (½ m r²), heat capacity (Σ m·c) and `ventilated` of the SIM block; mass difference to KIN; notification only to AERO; heat capacity to THERMO; consistency check against the brake specification. Mass = volume × density; a `Cut` boolean operation subtracts its tools. |
| AR3 | Caliper `specificationType` (monobloc / two-piece) → BOM; CAD mismatch → Design Engineer or Project Manager. A caliper with two `*Half` parts is two-piece. |
| AR4 | A renamed CAD element → `Device.mountingPosition`; a moved or reshaped mounting element → Electrical Engineer. |
| AR5 | ELEC `partNumber` → BOM item `partNumber`. |
| AR6 | BOM `supplier` / `material` → thermal component (material table: conductivity, heat capacity), analyses of it become outdated (`passed = false`); BOM `material` → CAD material. |
| AR7 | Thermal `temperatureLimit` → `expectedResult` of the test steps of the overlapping test case. |
| AR8 / AR9 | Requirement `allowedValues` → compliance check of the materials of all traced CAD parts and BOM items (owners notified), pass criterion of the test cases verifying the requirement updated; a material change of a traced part is checked against the requirement (violation or confirmation). |
| AR10 | Removing an artefact traced to requirements → the requirements no other artefact of the same model realises any more are reported as violated to the owner and to the Validation Engineer (with the affected compliance tests). |
| AR11 | ELEC pins and signals → the parameters `signal:<pin>` of the SIM block. |
| AR12 | Wear in the brake specification (pad / disc thickness, wear warning) → SIM parameters; the Project Manager is told about the maintenance requirements and the minimum thickness. |
| AR13 | A new counterpart is initialised: BOM item (type, id `<ROLE>-<part>`, mass, material), SIM block of a device (signals), SIM parameter set of a new tyre configuration (mean of the existing sets). |

Notifications are Vitruvius notification dialogs. Each one starts with `[Notify → <role>]` and contains, as far
as the VSUM knows it: what changed (old → new), where (path of the element and model), what Vitruvius updated
(propagation path, changed parameters), what the role has to do, the contact role and the change origin (the
requirements traced to the changed artefact, most recent first). Values written by a rule cause no echo, and a
role gets one notification per state of the changed artefact.

Roles of the models: DES `des.cad` Design Engineer, `kin.cad` Kinematics Engineer, `aero.cad` Aerodynamics
Engineer, `crash.cad` Crash Structure Engineer, `sim.simulink` Simulation Engineer, `thermo.thermal`
Thermodynamics Engineer, `elec.electrical` Electrical Engineer, `val.testbench` Validation Engineer,
`karaceing.bom` Procurement Manager, `des.brakesystem` and `prj.requirements` Project Manager.

---

## 3. The scenarios

Each scenario lists the report steps as executed (S = stakeholder, V = Vitruvius) and the explainability
requirements of the report (TR / FT) the notifications address.

### R1 Brake disc becomes vented (KA-RaceIng-1) – `reportScenario1VentedBrakeDisc`

Initial state: solid discs `FrontBrakeDisc` (cylinder r = 110 mm, 4 mm, steel, 1.1936 kg); SIM
`FrontBrakeDiscModel` with mass, inertia, heat capacity derived from it and `ventilated = false`; BOM
`DES-FrontBrakeDisc` with specification `solid`; brake specification `FrontBrakeDisk.ventilated = false`.

1. S Project Manager adds requirement `ANF-R01` "Brake discs must be vented" and maps it to `HydraulicBrakes`.
   V: the requirement traces to all artefacts of `HydraulicBrakes` (CAD parts, BOM items, brake specification);
   Design Engineer, Procurement Manager and Project Manager are notified (TR-04 origin).
2. S Project Manager records the decision in the brake specification (`ventilated = true`).
   V (AR1): BOM specification `solid → vented`, Procurement Manager notified that vented discs must be ordered
   and must match the CAD model, contact Design Engineer (TR-02, TR-03, TR-05); the Design Engineer is told
   that the CAD disc has no ventilation holes (not automatically resolvable, step 3 of the report).
3. S Design Engineer adds six holes (cylinders) and a `Cut` boolean operation to the disc.
   V (AR2): SIM `mass 1.1936 → 1.1557`, `inertia 0.0072214 → 0.0069922`, `heatCapacity 549.06 → 531.64`,
   `ventilated false → true`, Simulation Engineer notified with the list of changed parameters and the
   origin `ANF-R01` (TR-01, FT-01, FT-03, FT-07, FT-10); BOM mass updated; Kinematics Engineer gets the
   mass difference (FT-09); Aerodynamics Engineer is only notified (FT-08); Thermodynamics Engineer
   (heat capacity), Electrical Engineer (wheel speed sensor mounted at the disc) and Validation Engineer
   (`BrakeDiscSimulationCorrelation`) are informed.

Not covered: ordering (inventory model), drawings before / after (FT-04), contact persons by name (the VSUM
knows roles, not persons).

### R2 Design change in the brake caliper (KA-RaceIng-2) – `reportScenario2TwoPieceCaliper`

Initial state: `FrontBrakeCaliper` with a monobloc `FrontCaliperBody`, piston, mounting bracket, pads; BOM
`DES-FrontBrakeCaliper` specification `monobloc`; ELEC `FrontCaliperTemperatureSensor` with
`mountingPosition = FrontCaliperMountingBracket`, part number `TS-100`.

1. S Project Manager adds requirement `ANF-R02` (two-piece) → V: Design Engineer informed about the new
   requirement traced to the caliper (step 2, "Vitruvius cannot infer the semantic information").
2. S Design Engineer replaces the body by `FrontCaliperInnerHalf` / `OuterHalf` and moves the bracket.
   V: BOM items `DES-FrontCaliperInnerHalf` / `OuterHalf` created (mass, material, AR13), item of the old body
   deleted, caliper specification `monobloc → two-piece` (AR2/AR3, step 4); Electrical Engineer told that the
   mounting interface moved (`x 0.0 → 0.012`, AR4, steps 5–6); Validation Engineer informed
   (`BrakeAssemblyBuild`, step 8); Project Manager told that the brake specification still says monobloc.
3. S Electrical Engineer changes the sensor part number → V (AR5): BOM `partNumber TS-100 → TS-110` (step 7).
4. S Project Manager updates the brake specification → consistent, no further notification.

### R3 Brake caliper piston supplier change (KA-RaceIng-3) – `reportScenario3PistonSupplierChange`

Initial state: titanium pistons of `PistonSupplier A` (BOM), thermal model `FrontCaliperPistonThermalModel`
(titanium, 7 W/(m K), 520 J/(kg K), limit 350 °C), analysis `CaliperPistonTemperatureAnalysis` passed, test
`CaliperPistonTemperatureTest` expecting ≤ 350 °C.

1. S Project Manager approves steel pistons of `PistonSupplier B` in the BOM.
   V (AR6): thermal model `supplier`, `material Titanium → Steel`, conductivity 45, heat capacity 460; the
   analysis is set to not passed (outdated); Thermodynamics Engineer notified (step 2); CAD piston material
   `Titanium → Steel` (Design Engineer notified) and, derived from it, the BOM mass; Validation Engineer told to
   integrate the new pistons in the test bench (step 5).
2. S Thermodynamics Engineer reruns the analysis and lowers the limit to 300 °C.
   V (AR7): expected result of the test step updated, Validation Engineer notified (step 4).

### R4 Brake pedal material requirement change (KA-RaceIng-4) – `reportScenario4BrakePedalMaterial`

Initial state: ANF-217 allows Steel, Aluminium and Titanium (machined); the CAD pedal part and the BOM item are
titanium; `BrakePedalMaterialCompliance` verifies ANF-217.

1. S Requirements Engineer removes Titanium from ANF-217.
   V (AR8/AR9, step 2–4): Design Engineer (CAD part not compliant), Procurement Manager (ordered titanium pedal
   not compliant, order a compliant one) and Validation Engineer (pass criterion updated to
   `material in {Steel, Aluminium} (ANF-217)`) notified.
2. S Design Engineer changes the pedal to aluminium.
   V: BOM material and mass updated (Procurement Manager notified, step 5); compliance of CAD part and BOM item
   with ANF-217 confirmed to the Project Manager (step 6).

### R5 Removal of the scatter shield (KA-RaceIng-5) – `reportScenario5ScatterShieldRemoval`

Initial state: `ScatterShield` with steel and aluminium variants in DES; ANF-259 … ANF-265 (FS rules
T 7.3.2–T 7.3.4) trace to them; `ScatterShieldCompliance` verifies them.

1. S Design Engineer removes the scatter shield assembly.
   V (AR10, steps 2–3): Design Engineer notified that the removal violates ANF-259, ANF-262 … ANF-265 and
   FUN-026 (and ANF-260 / ANF-261 of the variants) – restore the shield or start a requirement change;
   Validation Engineer notified that `ScatterShieldCompliance` is affected. The feature stays (default answer
   "Keep feature"); resolving the inconsistency is the Design Engineer's decision (step 4).

### R6 Integration of ABS and BOTS (KA-RaceIng-6) – `reportScenario6IntegratedAbsBots`

Initial state: ELEC `ABSControlUnit` (wheel speeds, valve command) and `BOTSSwitch` (realises
`des.BrakeOverTravelSwitch`) with SIM blocks `ABSControl` and `BOTSSwitchLogic` whose `signal:` parameters
mirror the pins.

1. S Project Manager adds requirement `ANF-R06` (integration), mapped to `BrakeOverTravelSwitch`.
2. S Electrical Engineer adds `IntegratedBrakeControlUnit` with the signals of both and maps it to
   `BrakeOverTravelSwitch`; then removes the two separate devices.
   V (steps 3–4): SIM block `IntegratedBrakeControlUnit` created in `BrakeSystemModel` with all signal
   parameters (AR13/AR11), the blocks `ABSControl` and `BOTSSwitchLogic` deleted; Simulation Engineer told to
   adapt the control logic (step 5). The BOTS requirements trace to the integrated unit, so removing the old
   switch violates nothing.

Not covered: the BOM update of step 1 (the old BOM items are kept by the default answer).

### R7 Missing wear tracking (KA-RaceIng-7) – `reportScenario7WearTracking`

1. S Maintenance (in the brake specification): pads `8 → 5 mm` with wear warning, disc `4 → 3 mm`.
   V (AR12): SIM `padThickness`, `wearWarning`, `discThickness` updated, Simulation Engineer told to rerun the
   performance analyses (steps 2–3); Project Manager told that the disc reached its minimum thickness and which
   requirements (maintenance, operation) trace to the worn parts (steps 4–6).

Not covered: the Wiki model / maintenance records.

### R8 Intermediate tyre (KA-RaceIng-8) – `reportScenario8IntermediateTyre`

1. S A new assembly `IntermediateTyres` is added below `Wheels` (KIN) and becomes a new optional feature (FCPR
   dialog, step 1); all configurations get it (unselected).
   V (AR13, step 2): SIM parameter set `IntermediateTyres` created in `TyreModel`, initialised with the mean of
   the slick and rain sets (grip 1.4, rolling resistance 0.0175, …); Simulation Engineer asked to review it
   (step 3); Project Manager told that the performance analyses for slightly wet conditions must be
   reevaluated (steps 4–5).

### R9 Brake over-travel monitoring (KA-RaceIng-9) – `reportScenario9BotsSensor`

1. S Project Manager adds requirement `ANF-R09` (over-travel detection and warning), mapped to
   `BrakeOverTravelSwitch` → owners of the BOTS artefacts informed.
2. S Electrical Engineer adds `BOTSTravelSensor` (pins `PedalTravel`, `Warning`).
   V (step 3): SIM block with the signal parameters created, Simulation Engineer asked to add the warning logic
   (step 4). A later signal change is propagated to the block (AR11).

Not covered: Wiki model / project documentation (steps 5–6).

### R10 Update of the CAD tooling (KA-RaceIng-10) – disabled

Out of scope: the metamodels describe the content of the models, not the tool or exchange format they are
edited with; there is no change in the VSUM that could be propagated.

---

## 4. Limits and assumptions

- Values (dimensions, materials, suppliers, temperatures, tyre data) are plausible Formula Student values, not
  team data.
- The VSUM knows roles, not persons, dates or versions: explanations name the responsible role and model
  file; "version" is the current state of the model.
- Some semantics follow conventions of the CAD model (`*Half` parts → two-piece; `Cut` → vented) and a material
  table in `KARaceIngOverlapRules` (the same table is used by `initstate.py`).
- Generic notifications also follow overlaps found by walking up the containment (a change inside an assembly
  reaches the counterparts of the assembly).
- Vitruvius propagates a change sequence per CPS; a commit that both adds and removes artefacts may be checked
  before the additions are mapped (R6 therefore uses two commits).
