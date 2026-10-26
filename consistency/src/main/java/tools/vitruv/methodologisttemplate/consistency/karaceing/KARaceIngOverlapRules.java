package tools.vitruv.methodologisttemplate.consistency.karaceing;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * Attribute- and structure-level consistency rules between semantically overlapping artefacts of the KA-RaceIng
 * product line (rules AR1-AR13). They implement the "proposed consistency preservation" of the KA-RaceIng scenarios
 * of the Inconsistency Scenarios TechReport (R1-R10, see REPORT_SCENARIOS.md) on the models of the VSUM:
 * <ul>
 * <li>AR1 brake specification: ventilated disc -> BOM specification; CAD mismatch -> Design Engineer</li>
 * <li>AR2 CAD geometry / material / structure -> mass, material, variant in the BOM; mass, inertia, heat capacity,
 * venting in the SIM block; notifications to KIN (mass difference), AERO, THERMO, VAL, brake specification</li>
 * <li>AR3 caliper specification type (monobloc / two-piece) -> BOM; CAD mismatch -> Design Engineer / Project Manager</li>
 * <li>AR4 mounting interfaces: renamed CAD element -> Device.mountingPosition; moved -> Electrical Engineer</li>
 * <li>AR5 ELEC part number -> BOM item</li>
 * <li>AR6 BOM supplier / material -> thermal component (material table), analyses outdated; CAD material</li>
 * <li>AR7 thermal temperature limit -> expected result of the validation test</li>
 * <li>AR8 / AR9 requirement allowed values -> compliance of CAD and BOM materials, pass criteria of the tests</li>
 * <li>AR10 removing an artefact traced to requirements -> violated requirements, affected tests</li>
 * <li>AR11 ELEC pins / signals -> signal parameters of the SIM block</li>
 * <li>AR12 wear (pad / disc thickness, wear warning) -> SIM parameters, maintenance requirements</li>
 * <li>AR13 new counterparts are initialised (BOM item parameters, SIM signals, tyre parameter sets from the existing
 * sets)</li>
 * </ul>
 * All other changes of an artefact with overlaps notify the owner of the counterpart ("not automatically
 * resolvable"). The rules work reflectively on the metamodels (class and feature names), so the consistency module
 * does not depend on the generated code of the domain metamodels.
 *
 * <p>Every notification starts with {@code [Notify → <role>]} and explains the change (origin, traced requirements,
 * contact), what was updated and what has to be done, following the explainability requirements of the report
 * (notification, change origin tracing, propagation path transparency, responsibility mapping).
 */
public final class KARaceIngOverlapRules {

    public static final String TITLE = "KA-RaceIng consistency";

    /** density kg/m3, specific heat capacity J/(kg K), thermal conductivity W/(m K); same table as initstate.py. */
    static final Map<String, double[]> MATERIALS = Map.of(
            "Steel", new double[] { 7850, 460, 45 },
            "Aluminium", new double[] { 2700, 900, 205 },
            "CFRP", new double[] { 1600, 1000, 5 },
            "Titanium", new double[] { 4500, 520, 7 },
            "StainlessSteel", new double[] { 8000, 500, 16 });

    private static double[] materialData(String name) {
        return name == null ? null : MATERIALS.get(name);
    }

    private static final Set<String> GEOMETRY = Set.of("children", "length", "width", "height", "radius", "radius1", "radius2",
            "material", "operation", "base", "tools", "density");
    private static final Set<String> POSITION = Set.of("x", "y", "z", "rotationAxisX", "rotationAxisY", "rotationAxisZ", "rotationAngle");
    private static final Set<String> IGNORED = Set.of("label", "visible", "description", "construction", "driving");
    private static final Set<String> WEAR = Set.of("thicknessInMM", "brakeDiskThicknessInMM", "wearWarning");

    /** Values written by the rules (object -> feature -> value): the change events they cause are not propagated back. */
    private static final Map<EObject, Map<String, String>> DERIVED = new WeakHashMap<>();
    /** Old values of the derived writes (to explain differences, e.g. the mass before the change). */
    private static final Map<EObject, Map<String, String>> DERIVED_OLD = new WeakHashMap<>();
    /** Last state for which a notification about (counterpart, anchor, rule) was sent. */
    private static final Map<EObject, Map<String, String>> NOTIFIED = new WeakHashMap<>();
    /** Former container (model file, path) of removed elements; requirements already reported per removed subtree. */
    private static final Map<EObject, String[]> REMOVED_FROM = new WeakHashMap<>();
    private static final Map<EObject, Set<String>> REPORTED = new WeakHashMap<>();
    private static final Map<EObject, Boolean> ADDED = new WeakHashMap<>();
    private static final Map<EObject, List<EObject>> TRACES = new WeakHashMap<>();
    /** Features only the specific rules care about: no generic notification. */
    private static final Set<String> NON_GENERIC = Set.of("partNumber", "numberOfItems", "OEM_number", "passed", "result", "mass");
    /** Requirements that were added or changed recently (sequence number): listed first when explaining the origin of a change. */
    private static final Map<EObject, Long> RECENT = new WeakHashMap<>();
    private static long sequence;

    private KARaceIngOverlapRules() {
    }

    // ================================================================== entry points (called by the reactions)

    /**
     * A feature of {@code changed} changed (attribute, reference, or "children" for added / removed contents);
     * {@code anchor} is {@code changed} or the nearest container that has overlaps (or requirement traces) with
     * artefacts of the target model of the calling reactions; {@code counterparts} are those artefacts, {@code
     * overlaps} all overlapping elements of the anchor, {@code traces} the elements traced to the anchor.
     *
     * @return the notifications to show
     */
    public static synchronized List<String> propagate(EObject changed, EObject anchor, String feature, String oldValue, String newValue,
            Iterable<EObject> counterparts, Iterable<EObject> overlaps, Iterable<EObject> traces) {
        List<String> out = new ArrayList<>();
        if (changed == null || anchor == null || feature == null || IGNORED.contains(feature)) {
            return out;
        }
        // a value written by a rule is propagated further (derived values may depend on it), but it causes no
        // generic notifications: those would only echo the change back to the model it came from
        Ctx c = new Ctx(changed, anchor, effectiveFeature(changed, feature), oldValue, newValue, requirementsOf(traces),
                KARaceIngUtil.alive(overlaps), isDerived(changed, feature, newValue));
        List<EObject> targets = KARaceIngUtil.alive(counterparts).stream().filter(t -> t != anchor).collect(Collectors.toList());
        if (targets.isEmpty()) {
            return out;
        }
        String src = kind(anchor);
        if ("prj".equals(src)) {
            out.addAll(fromRequirement(c, targets));
            return out;
        }
        if ("prj".equals(kind(targets.get(0)))) {
            out.addAll(toRequirements(c, targets));
            return out;
        }
        // attribute values set while an element is created are part of its addition (notified once as a structure change)
        if (!Set.of("children", "pins", "allowedValues").contains(feature) && isInitialValue(changed, feature, oldValue) && isNew(changed)) {
            return out;
        }
        for (EObject cp : targets) {
            List<String> notes = switch (src) {
            case "cad" -> fromCad(c, cp);
            case "bom" -> fromBom(c, cp);
            case "brake" -> fromBrake(c, cp);
            case "elec" -> fromElec(c, cp);
            case "thermo" -> fromThermo(c, cp);
            default -> generic(c, cp);
            };
            if (notes.stream().anyMatch(n -> !n.isEmpty())) {
                sent(cp, anchor);      // no further (generic) notification about this state of the anchor
            }
            out.addAll(notes);
        }
        return out.stream().filter(n -> !n.isEmpty()).distinct().collect(Collectors.toList());
    }

    /** Remembers the container an element was added to or removed from (for explanations after a deletion). */
    public static synchronized void rememberContainer(EObject element, EObject container, String how) {
        if (element != null && container != null) {
            String file = fileOf(container);
            REMOVED_FROM.put(element, new String[] { file == null ? "" : file, path(container), role(container) });
            if ("added".equals(how)) {
                ADDED.put(element, Boolean.TRUE);
            }
        }
    }

    /** The old value is the value of a new element before it was set (null, 0, false or the default of the feature). */
    private static boolean isInitialValue(EObject changed, String feature, String value) {
        if (value == null || Set.of("null", "0", "0.0", "false", "", "<none>").contains(value)) {
            return true;
        }
        EStructuralFeature f = changed.eClass().getEStructuralFeature(feature);
        return f != null && f.getDefaultValue() != null && value.equals(String.valueOf(f.getDefaultValue()));
    }

    /** Remembers the artefacts a requirement traces to (before an artefact is removed, see removedTracedArtefact). */
    public static synchronized void recordTraces(EObject requirement, Iterable<EObject> traced) {
        TRACES.put(requirement, new ArrayList<>(KARaceIngUtil.alive(traced)));
    }

    /** true if the element (or an element containing it) was added recently. */
    private static boolean isNew(EObject element) {
        for (EObject o = element; o != null; o = o.eContainer()) {
            if (ADDED.containsKey(o)) {
                return true;
            }
        }
        return false;
    }

    /**
     * AR10: an artefact traced to requirements was deleted. The requirements of kind Requirement / Function traced
     * to it are violated; the owner of the artefact and the validation engineer (tests verifying the requirements)
     * are notified. Requirements are reported once per removed subtree.
     */
    public static synchronized List<String> removedTracedArtefact(EObject removed, Iterable<EObject> traces) {
        List<String> out = new ArrayList<>();
        if (removed == null) {
            return out;
        }
        EObject subtreeRoot = EcoreUtil.getRootContainer(removed);
        String[] from = REMOVED_FROM.getOrDefault(subtreeRoot, new String[] { "", "", ownerOfPackage(removed) });
        // a requirement is violated if no artefact of the same model traces to it any more (other artefacts still realise it)
        List<EObject> reqs = requirementsOf(traces).stream()
                .filter(r -> !"Goal".equals(String.valueOf(get(r, "kind"))))
                .filter(r -> TRACES.getOrDefault(r, List.of()).stream()
                        .noneMatch(a -> KARaceIngUtil.isAlive(a) && a != removed && (from[0].isEmpty() ? kind(a).equals(packageKind(removed)) : from[0].equals(fileOf(a)))))
                .collect(Collectors.toList());
        if (reqs.isEmpty()) {
            return out;
        }
        Set<String> reported = REPORTED.computeIfAbsent(subtreeRoot, k -> new HashSet<>());
        List<EObject> fresh = reqs.stream().filter(r -> reported.add(nameOf(r))).collect(Collectors.toList());
        if (fresh.isEmpty()) {
            return out;
        }
        String where = from[1].isEmpty() ? relativePath(removed) : from[1] + "/" + relativePath(removed);
        String owner = from[2];
        String list = describeRequirements(fresh, 8);
        out.add(note(owner, "Removing " + where + " (" + from[0] + ") violates " + fresh.size() + " requirement(s) traced to it: " + list
                + ". The element is mandatory for these requirements: restore it or initiate a requirement change with the Project Manager."
                + " Contact for the requirements: Project Manager (prj.requirements)."));
        List<String> tests = testsVerifying(fresh.get(0), fresh.stream().map(KARaceIngOverlapRules::nameOf).collect(Collectors.toSet()));
        out.add(note("Validation Engineer", "Removing " + where + " (" + from[0] + ", " + owner + ") violates " + list + "."
                + (tests.isEmpty() ? " No test case verifies these requirements yet."
                        : " Compliance checks affected: " + String.join(", ", tests) + "; reevaluate them once the inconsistency is resolved.")));
        return out;
    }

    /** A counterpart is deleted by the overlap rules (on request of the user): its removal is not notified again. */
    public static synchronized void counterpartRemoved(EObject counterpart) {
        if (counterpart != null && counterpart.eContainer() != null) {
            markDerived(counterpart.eContainer(), "children", null);
        }
    }

    /** AR13: a counterpart was created for {@code changed} next to / below {@code overlapping}: initialise it. */
    public static synchronized List<String> counterpartCreated(EObject changed, EObject counterpart, EObject overlapping) {
        List<String> out = new ArrayList<>();
        if (changed == null || counterpart == null) {
            return out;
        }
        markDerived(counterpart, "name", nameOf(counterpart));
        if (counterpart.eContainer() != null) {
            markDerived(counterpart.eContainer(), "children", nameOf(counterpart));
        }
        if (overlapping != null && changed.eContainer() != null) {
            sent(overlapping, changed.eContainer());    // the owner gets the notification about the new counterpart instead
        }
        String cls = counterpart.eClass().getName();
        if ("Item".equals(cls)) {
            out.addAll(initialiseBomItem(changed, counterpart, overlapping));
        } else if ("Block".equals(cls) && "Device".equals(changed.eClass().getName())) {
            List<String> changes = syncSignals(changed, counterpart);
            setSimParameter(counterpart, "SampleTime", "0.001", "double", true);
            out.add(note("Simulation Engineer", "New block " + path(counterpart) + " for the device " + path(changed) + " (" + fileOf(changed)
                    + "): " + (changes.isEmpty() ? "no signals" : "signal interfaces " + String.join(", ", changes))
                    + ". Implement its control logic, connect the signals and rerun the affected simulations. Contact: Electrical Engineer (elec.electrical)."));
        } else if ("Block".equals(cls)) {
            List<String> init = initialiseFromSiblings(counterpart);
            if (!init.isEmpty()) {
                out.add(note("Simulation Engineer", "New parameter set " + path(counterpart) + " for " + path(changed) + " (" + fileOf(changed)
                        + ", " + role(changed) + "). Initialised from the existing sets " + siblingNames(counterpart) + ": " + String.join(", ", init)
                        + ". Review the values with the supplier data and rerun the affected simulations."));
                out.add(note("Project Manager", "The configuration " + nameOf(changed) + " was added (" + fileOf(changed) + ") and the simulation got the"
                        + " parameter set " + nameOf(counterpart) + ". Performance analyses and planning assumptions for the conditions of "
                        + nameOf(changed) + " have to be reevaluated once the simulations are rerun."));
            } else {
                out.add(note(role(counterpart), "New element " + path(counterpart) + " was created as counterpart of " + path(changed) + " ("
                        + fileOf(changed) + ", " + role(changed) + "). Complete it."));
            }
        } else {
            out.add(note(role(counterpart), "New element " + path(counterpart) + " was created as counterpart of " + path(changed) + " ("
                    + fileOf(changed) + ", " + role(changed) + "). Complete it."));
        }
        return out;
    }

    /**
     * A requirement was mapped to a feature: it traces to all artefacts of the feature. The owners of these
     * artefacts are informed (one notification per role); Vitruvius cannot infer what the requirement means for them.
     */
    public static synchronized List<String> newRequirementTraces(EObject requirement, String featureName, Iterable<EObject> artefacts) {
        RECENT.put(requirement, ++sequence);
        Map<String, List<String>> byRole = new LinkedHashMap<>();
        for (EObject a : KARaceIngUtil.alive(artefacts)) {
            if (!isRequirement(a) && isSolutionArtefact(a)) {
                byRole.computeIfAbsent(role(a), k -> new ArrayList<>()).add(nameOf(a) + " (" + fileOf(a) + ")");
            }
        }
        List<String> out = new ArrayList<>();
        byRole.forEach((r, arts) -> out.add(note(r, "New requirement " + nameOf(requirement) + " '" + str(get(requirement, "text")) + "' (mapped to the feature "
                + featureName + ") traces to your artefacts " + abbreviate(arts, 10) + ". Vitruvius cannot infer its semantic consequences:"
                + " check whether the artefacts satisfy it. Contact: Project Manager (prj.requirements).")));
        return out;
    }

    // ================================================================== rules by source model

    private static List<String> fromCad(Ctx c, EObject cp) {
        boolean geometry = GEOMETRY.contains(c.feature);
        boolean position = POSITION.contains(c.feature);
        switch (kind(cp)) {
        case "bom":
            if (geometry) {
                return cadToBom(c, cp);
            }
            break;
        case "sim":
            if (geometry) {
                List<String> notes = cadToSim(c, cp);
                if (!notes.isEmpty()) {
                    return notes;
                }
            }
            break;
        case "cad":
            if (geometry) {
                return cadToCad(c, cp);
            }
            break;
        case "brake":
            if (geometry) {
                return cadToBrake(c, cp);
            }
            break;
        case "elec":
            List<String> notes = cadToElec(c, cp, geometry || position);
            if (notes != null) {
                return notes;
            }
            break;
        case "thermo":
            if (geometry) {
                return cadToThermo(c, cp);
            }
            break;
        default:
            break;
        }
        return generic(c, cp);
    }

    private static List<String> fromBom(Ctx c, EObject cp) {
        if (!Set.of("material", "supplier", "specification", "partNumber", "numberOfItems").contains(c.feature)) {
            return List.of();              // mass is derived from CAD; other item data is procurement-internal
        }
        switch (kind(cp)) {
        case "thermo":
            return bomToThermo(c, cp);
        case "cad":
            return bomToCad(c, cp);
        case "val":
            return c.derived ? List.of() : once(cp, c.anchor, "bom2val", note("Validation Engineer", "The BOM item " + nameOf(c.anchor) + " changed (" + c.change()
                    + "). Integrate the new parts in " + path(cp) + " as soon as they arrive and check them against the current models."
                    + c.origin()));
        default:
            return generic(c, cp);
        }
    }

    private static List<String> fromBrake(Ctx c, EObject cp) {
        switch (kind(cp)) {
        case "bom":
            return brakeToBom(c, cp);
        case "cad":
            return brakeToCad(c, cp);
        case "sim":
            if (WEAR.contains(c.feature)) {
                return brakeToSim(c, cp);
            }
            return List.of();             // the simulation follows the CAD geometry (R1), not the specification
        default:
            return generic(c, cp);
        }
    }

    private static List<String> fromElec(Ctx c, EObject cp) {
        if ("bom".equals(kind(cp)) && "partNumber".equals(c.feature)) {
            EObject device = c.anchor;
            List<String> changes = new ArrayList<>();
            setBomParameter(cp, "partNumber", str(get(device, "partNumber")), null, changes);
            if (changes.isEmpty()) {
                return List.of();
            }
            return List.of(note("Procurement Manager", "The electrical design changed the part number of " + path(device) + " (" + c.change()
                    + "). BOM item " + nameOf(cp) + " updated: " + String.join(", ", changes) + ". Order the new part." + c.origin()));
        }
        if ("sim".equals(kind(cp)) && "Device".equals(c.anchor.eClass().getName())
                && (c.changed.eClass().getName().equals("Pin") || Set.of("children", "pins").contains(c.feature))) {
            List<String> changes = syncSignals(c.anchor, cp);
            if (changes.isEmpty()) {
                return List.of();
            }
            return List.of(note("Simulation Engineer", "The signal interfaces of " + path(c.anchor) + " (elec.electrical) changed. Updated the signal"
                    + " parameters of " + path(cp) + ": " + String.join(", ", changes) + ". Review and adapt the control logic of " + nameOf(cp)
                    + " and rerun the affected simulations." + c.origin()));
        }
        return generic(c, cp);
    }

    private static List<String> fromThermo(Ctx c, EObject cp) {
        if ("val".equals(kind(cp)) && "temperatureLimit".equals(c.feature)) {
            List<String> changes = new ArrayList<>();
            String expected = nameOf(c.anchor) + " temperature <= " + c.newValue + " C";
            for (EObject step : contents(cp, "steps")) {
                String old = str(get(step, "expectedResult"));
                if (old.contains(nameOf(c.anchor)) || old.contains("temperature <=")) {
                    if (setDerived(step, "expectedResult", expected)) {
                        changes.add(nameOf(step) + ": '" + old + "' -> '" + expected + "'");
                    }
                }
            }
            if (changes.isEmpty()) {
                return List.of();
            }
            return List.of(note("Validation Engineer", "The thermodynamic analysis changed the temperature limit of " + path(c.anchor) + " ("
                    + c.change() + "). Updated the expected results of " + path(cp) + ": " + String.join("; ", changes)
                    + ". Validate the parts against the new limit." + c.origin()));
        }
        return generic(c, cp);
    }

    /** A requirement changed; {@code targets} are the artefacts of one model it traces to. */
    private static List<String> fromRequirement(Ctx c, List<EObject> targets) {
        EObject req = c.anchor;
        RECENT.put(req, ++sequence);
        if ("allowedValues".equals(c.feature)) {
            List<String> allowed = strings(get(req, "allowedValues"));
            Map<String, List<String>> violations = new LinkedHashMap<>();
            List<String> criteria = new ArrayList<>();
            for (EObject t : targets) {
                switch (kind(t)) {
                case "cad":
                    for (EObject solid : solids(t)) {
                        String m = materialName(solid);
                        if (m != null && !allowed.contains(m)) {
                            violations.computeIfAbsent(role(solid), k -> new ArrayList<>()).add(nameOf(solid) + " (" + m + ", " + fileOf(solid) + ")");
                        }
                    }
                    break;
                case "bom":
                    String m = bomString(t, "material");
                    if (m != null && !m.equals("-") && !allowed.contains(m)) {
                        violations.computeIfAbsent("Procurement Manager", k -> new ArrayList<>()).add("BOM item " + nameOf(t) + " (" + m + ")");
                    }
                    break;
                case "val":
                    if ("TestCase".equals(t.eClass().getName()) && strings(get(t, "verifiedRequirements")).contains(nameOf(req))) {
                        String criterion = "material in {" + String.join(", ", allowed) + "} (" + nameOf(req) + ")";
                        String old = str(get(t, "passCriterion"));
                        if (setDerived(t, "passCriterion", criterion)) {
                            criteria.add(nameOf(t) + ": '" + old + "' -> '" + criterion + "'");
                        }
                    }
                    break;
                default:
                    break;
                }
            }
            List<String> out = new ArrayList<>();
            String head = "Requirement " + nameOf(req) + " '" + str(get(req, "text")) + "' now allows {" + String.join(", ", allowed) + "} (" + c.change() + ").";
            violations.forEach((r, list) -> out.addAll(once(req, req, "allowed:" + r, note(r, head + " No longer compliant: " + String.join(", ", list)
                    + (r.equals("Procurement Manager") ? ". Update the procurement specification and order a compliant part."
                            : ". Update the design so that it complies.")
                    + " Contact: Project Manager (prj.requirements)."))));
            if (!criteria.isEmpty()) {
                out.add(note("Validation Engineer", head + " Updated the compliance checks: " + String.join("; ", criteria) + "."));
            }
            return out;
        }
        if ("text".equals(c.feature) && c.oldValue != null && !"null".equals(c.oldValue)) {
            Map<String, List<String>> byRole = new LinkedHashMap<>();
            for (EObject t : targets) {
                byRole.computeIfAbsent(role(t), k -> new ArrayList<>()).add(nameOf(t));
            }
            List<String> out = new ArrayList<>();
            byRole.forEach((r, arts) -> out.add(note(r, "Requirement " + nameOf(req) + " changed: '" + c.oldValue + "' -> '" + c.newValue
                    + "'. It traces to your artefacts " + abbreviate(arts, 6) + ". Vitruvius cannot infer the semantic consequences: review them."
                    + " Contact: Project Manager (prj.requirements).")));
            return out;
        }
        return List.of();
    }

    /** An artefact traced to requirements changed: compliance with requirements that restrict values (AR8/AR9, AR12). */
    private static List<String> toRequirements(Ctx c, List<EObject> requirements) {
        List<String> out = new ArrayList<>();
        String src = kind(c.anchor);
        if (("cad".equals(src) && Set.of("material", "children").contains(c.feature)) || ("bom".equals(src) && "material".equals(c.feature))) {
            List<String> materials = "cad".equals(src) ? solids(c.anchor).stream().map(KARaceIngOverlapRules::materialName)
                    .filter(Objects::nonNull).distinct().collect(Collectors.toList()) : listOf(bomString(c.anchor, "material"));
            for (EObject req : requirements) {
                List<String> allowed = strings(get(req, "allowedValues"));
                if (allowed.isEmpty() || materials.isEmpty()) {
                    continue;
                }
                List<String> bad = materials.stream().filter(m -> !allowed.contains(m) && !"-".equals(m)).collect(Collectors.toList());
                String what = ("cad".equals(src) ? path(c.anchor) : "BOM item " + nameOf(c.anchor)) + " (" + fileOf(c.anchor) + ")";
                String text = bad.isEmpty()
                        ? what + " now uses " + String.join(", ", materials) + ": complies with " + nameOf(req) + " (allowed {" + String.join(", ", allowed) + "})."
                        : what + " uses " + String.join(", ", bad) + ", which violates " + nameOf(req) + " '" + str(get(req, "text")) + "' (allowed {"
                                + String.join(", ", allowed) + "}).";
                if (bad.isEmpty() && !"material".equals(c.feature)) {
                    continue;
                }
                out.addAll(once(req, c.anchor, "compliance", note(role(c.anchor), text + c.origin())));
                out.addAll(once(req, c.anchor, "compliancePM", note("Project Manager", text)));
            }
        }
        if ("brake".equals(src) && WEAR.contains(c.feature)) {
            String limit = "";
            Object thickness = get(c.anchor, "brakeDiskThicknessInMM");
            Object minimum = get(c.anchor, "minimumThicknessInMM");
            if (thickness instanceof Integer t && minimum instanceof Integer m && t <= m) {
                limit = " The disc has reached its minimum thickness (" + t + " mm <= " + m + " mm): replace it before the next race.";
            }
            if (Boolean.TRUE.equals(get(c.anchor, "wearWarning"))) {
                limit += " The wear warning of " + nameOf(c.anchor) + " is set.";
            }
            out.addAll(once(c.anchor, c.anchor, "wear", note("Project Manager", "Wear of " + nameOf(c.anchor) + " was recorded in the brake specification ("
                    + c.change() + ")." + limit + " Maintenance planning and operational constraints are affected; requirements traced to "
                    + nameOf(c.anchor) + ": " + describeRequirements(requirementsOf(requirements), 4)
                    + ". Update the maintenance and operational requirements if necessary.")));
        }
        return out;
    }

    // ------------------------------------------------------------------ CAD rules (AR2, AR4)

    private static List<String> cadToBom(Ctx c, EObject item) {
        List<String> changes = new ArrayList<>();
        double m = mass(c.anchor);
        if (m > 0) {
            setBomParameter(item, "mass", null, round(m, 3), changes);
        }
        String mat = singleMaterial(c.anchor);
        if (mat != null) {
            setBomParameter(item, "material", mat, null, changes);
        }
        String spec = bomString(item, "specification");
        if ("solid".equals(spec) || "vented".equals(spec)) {
            setBomParameter(item, "specification", hasCut(c.anchor) ? "vented" : "solid", null, changes);
        } else if ("monobloc".equals(spec) || "two-piece".equals(spec)) {
            String v = housingVariant(c.anchor);
            if (v != null) {
                setBomParameter(item, "specification", v, null, changes);
            }
        }
        if (changes.isEmpty()) {
            return List.of();
        }
        return List.of(note("Procurement Manager", "BOM item " + nameOf(item) + " was updated from the CAD model " + path(c.anchor) + " (" + fileOf(c.anchor)
                + "): " + String.join(", ", changes) + ". Parts ordered for the previous version no longer match the CAD model; order them according"
                + " to the current version. Contact for geometry and manufacturing: " + role(c.anchor) + " (" + fileOf(c.anchor) + ")." + c.origin()));
    }

    private static List<String> cadToSim(Ctx c, EObject block) {
        Map<String, String> values = new LinkedHashMap<>();
        double m = mass(c.anchor);
        if (m <= 0) {
            return List.of();
        }
        values.put("mass", fmt(m));
        values.put("inertia", fmt(0.5 * m * Math.pow(outerRadius(c.anchor), 2)));
        values.put("heatCapacity", fmt(heatCapacity(c.anchor)));
        values.put("ventilated", String.valueOf(hasCut(c.anchor)));
        List<String> changes = new ArrayList<>();
        List<String> unchanged = new ArrayList<>();
        values.forEach((p, v) -> {
            EObject param = simParameter(block, p);
            if (param != null) {
                String old = str(get(param, "value"));
                if (setDerived(param, "value", v)) {
                    changes.add(p + " " + old + " -> " + v);
                } else {
                    unchanged.add(p);
                }
            }
        });
        if (changes.isEmpty() && unchanged.isEmpty()) {
            return List.of();          // the block has no derived parameters: generic notification
        }
        if (changes.isEmpty()) {
            return List.of("");        // nothing to do (filtered out below)
        }
        return List.of(note("Simulation Engineer", "The CAD model of " + path(c.anchor) + " (" + fileOf(c.anchor) + ") changed (" + c.change()
                + "); a new version must be used for the simulations. Propagated to " + path(block) + ": " + String.join(", ", changes)
                + (unchanged.isEmpty() ? "" : " (unchanged: " + String.join(", ", unchanged) + ")") + ". Review the propagated values and rerun the"
                + " simulations. Contact: " + role(c.anchor) + " (" + fileOf(c.anchor) + ")." + c.origin()));
    }

    private static List<String> cadToCad(Ctx c, EObject other) {
        String r = role(other);
        double now = mass(c.anchor);
        Double before = massBefore(c);
        String massText = now > 0 ? " Mass " + (before != null && Math.abs(before - now) > 1e-9 ? fmt(before) + " -> " + fmt(now) + " kg (difference "
                + fmt(now - (before == null ? 0 : before)) + " kg)" : fmt(now) + " kg") + "." : "";
        String text;
        if ("Kinematics Engineer".equals(r)) {
            text = path(c.anchor) + " (" + fileOf(c.anchor) + ") changed (" + c.change() + ")." + massText + " It overlaps with " + path(other)
                    + ": the unsprung / rotating mass changes accordingly; check the kinematics parameters.";
        } else if ("Aerodynamics Engineer".equals(r)) {
            text = path(c.anchor) + " (" + fileOf(c.anchor) + ") changed (" + c.change() + "). It overlaps with " + path(other)
                    + ". No automatic transfer into the aerodynamics models takes place; assess whether a new CFD computation is required.";
        } else {
            text = path(c.anchor) + " (" + fileOf(c.anchor) + ") changed (" + c.change() + ")." + massText + " It overlaps with " + path(other)
                    + "; check whether your model has to be adapted.";
        }
        return once(other, c.anchor, "cad2cad", note(r, text + " Contact: " + role(c.anchor) + "." + c.origin()));
    }

    private static List<String> cadToBrake(Ctx c, EObject comp) {
        String cls = comp.eClass().getName();
        if ("BrakeDisk".equals(cls)) {
            boolean cad = hasCut(c.anchor);
            boolean spec = Boolean.TRUE.equals(get(comp, "ventilated"));
            if (cad != spec) {
                return once(comp, c.anchor, "vent", note("Project Manager", "The CAD model " + path(c.anchor) + (cad ? " has" : " has no")
                        + " ventilation holes, but the brake specification of " + nameOf(comp) + " says ventilated = " + spec
                        + ". Update the specification or clarify with the " + role(c.anchor) + "." + c.origin()));
            }
            return List.of();
        }
        if ("BrakeCaliper".equals(cls)) {
            String variant = housingVariant(c.anchor);
            String spec = str(get(comp, "specificationType"));
            if (variant != null && !variant.equals(spec)) {
                return once(comp, c.anchor, "housing", note("Project Manager", "The CAD model " + path(c.anchor) + " is a " + variant
                        + " caliper, but the brake specification of " + nameOf(comp) + " says '" + spec + "'. Update the brake specification." + c.origin()));
            }
            return List.of();
        }
        return generic(c, comp);
    }

    /** AR4; null if the device is not mounted on the changed element (then the generic rule applies). */
    private static List<String> cadToElec(Ctx c, EObject device, boolean geometryOrPosition) {
        String mount = str(get(device, "mountingPosition"));
        if (mount.isEmpty()) {
            return null;
        }
        if ("name".equals(c.feature) && mount.equals(c.oldValue)) {
            setDerived(device, "mountingPosition", c.newValue);
            return List.of(note("Electrical Engineer", "The mounting interface of " + path(device) + " was renamed in the CAD model (" + c.oldValue + " -> "
                    + c.newValue + "); mountingPosition updated." + c.origin()));
        }
        if (geometryOrPosition && (mount.equals(nameOf(c.changed)) || mount.equals(nameOf(c.anchor)))) {
            return once(device, c.anchor, "mount", note("Electrical Engineer", "The mounting interface " + mount + " of " + path(device) + " changed in the CAD"
                    + " model (" + path(c.changed) + ": " + c.change() + "). Review the mounting of the device, the geometric constraints and the harness"
                    + " routing, and adapt the electrical design if required (a changed part number is propagated to the BOM). Contact: " + role(c.anchor)
                    + " (" + fileOf(c.anchor) + ")." + c.origin()));
        }
        return null;
    }

    private static List<String> cadToThermo(Ctx c, EObject t) {
        String cls = t.eClass().getName();
        if ("ThermalComponent".equals(cls) && "material".equals(c.feature)) {
            String mat = singleMaterial(c.anchor);
            if (mat != null) {
                List<String> changes = setThermalMaterial(t, mat, null);
                if (!changes.isEmpty()) {
                    List<String> outdated = outdateAnalyses(t, "material of " + nameOf(c.anchor) + " changed to " + mat);
                    return List.of(note("Thermodynamics Engineer", "The material of " + path(c.anchor) + " changed in the CAD model (" + c.change() + "). Updated "
                            + path(t) + ": " + String.join(", ", changes) + "." + outdatedText(outdated) + c.origin()));
                }
                return List.of();
            }
        }
        double m = mass(c.anchor);
        return once(t, c.anchor, "cad2thermo", note("Thermodynamics Engineer", "The geometry of " + path(c.anchor) + " (" + fileOf(c.anchor) + ") changed ("
                + c.change() + ")" + (m > 0 ? ": mass " + fmt(m) + " kg, heat capacity " + fmt(heatCapacity(c.anchor)) + " J/K" : "") + ". It overlaps with "
                + path(t) + ": heat storage and dissipation change; rerun the thermal analyses of " + nameOf(t) + ". Contact: " + role(c.anchor) + "."
                + c.origin()));
    }

    // ------------------------------------------------------------------ BOM rules (AR6)

    private static List<String> bomToThermo(Ctx c, EObject t) {
        if (!"ThermalComponent".equals(t.eClass().getName())) {
            return generic(c, t);
        }
        List<String> changes = new ArrayList<>();
        if ("supplier".equals(c.feature)) {
            String old = str(get(t, "supplier"));
            if (setDerived(t, "supplier", c.newValue)) {
                changes.add("supplier " + old + " -> " + c.newValue);
            }
        } else if ("material".equals(c.feature)) {
            changes.addAll(setThermalMaterial(t, c.newValue, null));
        } else {
            return generic(c, t);
        }
        if (changes.isEmpty()) {
            return List.of();
        }
        List<String> outdated = outdateAnalyses(t, "supplier data of " + nameOf(c.anchor) + " changed (" + c.change() + ")");
        return List.of(note("Thermodynamics Engineer", "The BOM item " + nameOf(c.anchor) + " changed (" + c.change() + "; supplier data sheet). Propagated to "
                + path(t) + ": " + String.join(", ", changes) + "." + outdatedText(outdated) + " Review the propagated material properties, rerun the"
                + " thermal analyses and check the temperature limit. Contact: Procurement Manager (karaceing.bom)." + c.origin()));
    }

    private static List<String> bomToCad(Ctx c, EObject part) {
        if ("material".equals(c.feature) && materialData(c.newValue) != null) {
            List<String> changed = new ArrayList<>();
            for (EObject solid : solids(part)) {
                String old = materialName(solid);
                if (!c.newValue.equals(old) && setMaterial(solid, c.newValue)) {
                    changed.add(nameOf(solid) + " " + old + " -> " + c.newValue);
                }
            }
            if (changed.isEmpty()) {
                return List.of();
            }
            return List.of(note(role(part), "The BOM item " + nameOf(c.anchor) + " specifies the material " + c.newValue + " (" + c.change() + "). Updated "
                    + path(part) + ": " + String.join(", ", changed) + ". Check strength and mass of the part. Contact: Procurement Manager (karaceing.bom)."
                    + c.origin()));
        }
        if (c.derived) {
            return List.of();
        }
        return once(part, c.anchor, "bom2cad", note(role(part), "The BOM item " + nameOf(c.anchor) + " changed (" + c.change() + "). Check that the supplied"
                + " part still matches " + path(part) + ". Contact: Procurement Manager (karaceing.bom)." + c.origin()));
    }

    // ------------------------------------------------------------------ brake specification rules (AR1, AR3, AR12)

    private static List<String> brakeToBom(Ctx c, EObject item) {
        List<String> changes = new ArrayList<>();
        if ("ventilated".equals(c.feature)) {
            setBomParameter(item, "specification", Boolean.TRUE.equals(get(c.anchor, "ventilated")) ? "vented" : "solid", null, changes);
        } else if ("specificationType".equals(c.feature) && "BrakeCaliper".equals(c.anchor.eClass().getName())) {
            setBomParameter(item, "specification", str(get(c.anchor, "specificationType")), null, changes);
        } else {
            return WEAR.contains(c.feature) ? List.of() : generic(c, item);
        }
        if (changes.isEmpty()) {
            return List.of();
        }
        String cad = c.overlaps.stream().filter(o -> "cad".equals(kind(o))).map(KARaceIngOverlapRules::path).findFirst().orElse("the CAD model");
        return List.of(note("Procurement Manager", "The brake specification of " + nameOf(c.anchor) + " changed (" + c.change() + "). BOM item " + nameOf(item)
                + " updated: " + String.join(", ", changes) + ". New parts must be ordered; they must match the Design Engineer's CAD model " + cad
                + ". Contact for geometry: Design Engineer (des.cad)." + c.origin()));
    }

    private static List<String> brakeToCad(Ctx c, EObject part) {
        if ("ventilated".equals(c.feature)) {
            boolean spec = Boolean.TRUE.equals(get(c.anchor, "ventilated"));
            if (spec != hasCut(part)) {
                return List.of(note(role(part), "The brake specification requires " + nameOf(c.anchor) + " to be " + (spec ? "ventilated" : "solid")
                        + ", but the CAD model " + path(part) + (spec ? " has no ventilation holes" : " has ventilation holes")
                        + ". This inconsistency cannot be resolved automatically: " + (spec ? "add the ventilation holes" : "remove the ventilation holes")
                        + " in the CAD model. Contact: Project Manager (des.brakesystem)." + c.origin()));
            }
            return List.of();
        }
        if ("specificationType".equals(c.feature) && "BrakeCaliper".equals(c.anchor.eClass().getName())) {
            String spec = str(get(c.anchor, "specificationType"));
            String variant = housingVariant(part);
            if (variant != null && !variant.equals(spec)) {
                return List.of(note(role(part), "The brake specification requires a " + spec + " caliper " + nameOf(c.anchor) + ", but the CAD model "
                        + path(part) + " is " + variant + ". Update the caliper geometry. Contact: Project Manager (des.brakesystem)." + c.origin()));
            }
            return List.of();
        }
        if (WEAR.contains(c.feature)) {
            return List.of();             // the CAD model shows the nominal part
        }
        return generic(c, part);
    }

    private static List<String> brakeToSim(Ctx c, EObject block) {
        String param = switch (c.feature) {
        case "brakeDiskThicknessInMM" -> "discThickness";
        case "thicknessInMM" -> "padThickness";
        case "wearWarning" -> "wearWarning";
        default -> null;
        };
        if (param == null) {
            return List.of();
        }
        String value = str(get(c.changed, c.rawFeature));
        EObject p = simParameter(block, param);
        String old = p == null ? "-" : str(get(p, "value"));
        if (!setSimParameter(block, param, value, "wearWarning".equals(param) ? "boolean" : "double", false)) {
            return List.of();
        }
        return List.of(note("Simulation Engineer", "Wear of " + nameOf(c.changed) + " was recorded in the brake specification (" + c.change() + "). Propagated to "
                + path(block) + ": " + param + " " + old + " -> " + value + ". The simulation no longer assumes the nominal component: review the"
                + " degradation and rerun the performance analyses. Contact: Project Manager / maintenance (des.brakesystem)." + c.origin()));
    }

    // ------------------------------------------------------------------ generic rule

    private static List<String> generic(Ctx c, EObject cp) {
        if ("bom".equals(kind(cp)) && !Set.of("cad", "brake", "elec").contains(kind(c.anchor))) {
            return List.of();             // the Procurement Manager is informed about what is ordered (part data), not about analyses
        }
        if (c.derived || NON_GENERIC.contains(c.feature) || POSITION.contains(c.feature) && !"cad".equals(kind(cp)) && !"elec".equals(kind(cp))) {
            return List.of();
        }
        return once(cp, c.anchor, "generic", note(role(cp), path(c.changed) + " (" + fileOf(c.changed) + ") changed (" + c.change() + "). It "
                + (c.changed == c.anchor ? "" : "is part of " + nameOf(c.anchor) + ", which ") + "overlaps with " + path(cp) + " (" + fileOf(cp) + ", "
                + kindLabel(c.anchor, cp) + "). This may have to be adapted; Vitruvius cannot resolve it automatically. Contact: " + role(c.anchor) + " ("
                + fileOf(c.anchor) + ")." + c.origin()));
    }

    // ================================================================== geometry (same rules as initstate.py)

    public static double volume(EObject o) {
        switch (o.eClass().getName()) {
        case "Box":
            return d(o, "length") * d(o, "width") * d(o, "height");
        case "Cylinder":
            return Math.PI * Math.pow(d(o, "radius"), 2) * d(o, "height");
        case "Sphere":
            return 4.0 / 3 * Math.PI * Math.pow(d(o, "radius"), 3);
        case "Cone":
            double r1 = d(o, "radius1"), r2 = d(o, "radius2");
            return Math.PI * d(o, "height") * (r1 * r1 + r1 * r2 + r2 * r2) / 3;
        case "BooleanOperation":
            EObject base = (EObject) get(o, "base");
            double vb = base == null ? 0 : volume(base);
            List<Double> vt = contentsOfRef(o, "tools").stream().map(KARaceIngOverlapRules::volume).collect(Collectors.toList());
            String op = String.valueOf(get(o, "operation"));
            if ("Cut".equals(op)) {
                return Math.max(vb - vt.stream().mapToDouble(Double::doubleValue).sum(), 0);
            }
            if ("Common".equals(op)) {
                return vt.stream().mapToDouble(Double::doubleValue).reduce(vb, Math::min);
            }
            return vb + vt.stream().mapToDouble(Double::doubleValue).sum();
        default:
            return 0;
        }
    }

    private static double density(EObject o) {
        if ("BooleanOperation".equals(o.eClass().getName())) {
            EObject base = (EObject) get(o, "base");
            return base == null ? 0 : density(base);
        }
        EObject m = material(o);
        return m == null ? 0 : d(m, "density");
    }

    private static double specificHeat(EObject o) {
        if ("BooleanOperation".equals(o.eClass().getName())) {
            EObject base = (EObject) get(o, "base");
            return base == null ? 0 : specificHeat(base);
        }
        double[] data = materialData(materialName(o));
        return data == null ? 0 : data[1];
    }

    /** Mass in kg: solids volume x density; assemblies the sum of their parts (solids consumed by a boolean operation are skipped). */
    public static double mass(EObject o) {
        if (o == null) {
            return 0;
        }
        if ("Assembly".equals(o.eClass().getName())) {
            return visibleParts(o).stream().mapToDouble(KARaceIngOverlapRules::mass).sum();
        }
        return isSolid(o) ? volume(o) * density(o) : 0;
    }

    private static double heatCapacity(EObject o) {
        if ("Assembly".equals(o.eClass().getName())) {
            return visibleParts(o).stream().mapToDouble(KARaceIngOverlapRules::heatCapacity).sum();
        }
        return isSolid(o) ? volume(o) * density(o) * specificHeat(o) : 0;
    }

    private static double outerRadius(EObject o) {
        double r = 0;
        for (EObject s : solids(o)) {
            EObject base = "BooleanOperation".equals(s.eClass().getName()) ? (EObject) get(s, "base") : s;
            if (base != null && "Cylinder".equals(base.eClass().getName())) {
                r = Math.max(r, d(base, "radius"));
            }
        }
        return r;
    }

    /** Children of an assembly without the solids consumed by a boolean operation of the assembly. */
    private static List<EObject> visibleParts(EObject assembly) {
        List<EObject> children = contents(assembly, "children");
        Set<EObject> consumed = new HashSet<>();
        for (EObject ch : children) {
            if ("BooleanOperation".equals(ch.eClass().getName())) {
                Object base = get(ch, "base");
                if (base instanceof EObject b) {
                    consumed.add(b);
                }
                consumed.addAll(contentsOfRef(ch, "tools"));
            }
        }
        return children.stream().filter(ch -> !consumed.contains(ch)).collect(Collectors.toList());
    }

    /** The (visible) solids of a CAD element: the element itself or the solids below an assembly. */
    private static List<EObject> solids(EObject o) {
        if (o == null) {
            return List.of();
        }
        if ("Assembly".equals(o.eClass().getName())) {
            List<EObject> out = new ArrayList<>();
            for (EObject ch : visibleParts(o)) {
                out.addAll(solids(ch));
            }
            return out;
        }
        return isSolid(o) ? List.of(o) : List.of();
    }

    private static boolean isSolid(EObject o) {
        return o.eClass().getEStructuralFeature("material") != null && o.eClass().getEStructuralFeature("children") == null;
    }

    private static boolean hasCut(EObject o) {
        if (o == null) {
            return false;
        }
        if ("BooleanOperation".equals(o.eClass().getName()) && "Cut".equals(String.valueOf(get(o, "operation")))) {
            return true;
        }
        for (EObject ch : contents(o, "children")) {
            if (hasCut(ch)) {
                return true;
            }
        }
        return false;
    }

    /** Housing of a caliper assembly: two or more parts named *Half -> two-piece, one *Body -> monobloc (naming convention of the CAD model). */
    private static String housingVariant(EObject caliper) {
        List<EObject> children = contents(caliper, "children");
        long halves = children.stream().filter(ch -> nameOf(ch).contains("Half")).count();
        if (halves >= 2) {
            return "two-piece";
        }
        return children.stream().anyMatch(ch -> nameOf(ch).endsWith("Body")) ? "monobloc" : null;
    }

    private static EObject material(EObject solid) {
        Object m = get(solid, "material");
        return m instanceof EObject e ? e : null;
    }

    private static String materialName(EObject solid) {
        if ("BooleanOperation".equals(solid.eClass().getName())) {
            Object base = get(solid, "base");
            return base instanceof EObject b ? materialName(b) : null;
        }
        EObject m = material(solid);
        return m == null ? null : nameOf(m);
    }

    private static String singleMaterial(EObject o) {
        List<String> mats = solids(o).stream().map(KARaceIngOverlapRules::materialName).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        return mats.size() == 1 ? mats.get(0) : null;
    }

    /** Sets the material of a CAD solid (the Material of the CAD document with that name is created if missing). */
    private static boolean setMaterial(EObject solid, String name) {
        EObject doc = EcoreUtil.getRootContainer(solid);
        EStructuralFeature materials = doc.eClass().getEStructuralFeature("materials");
        if (materials == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        EList<EObject> list = (EList<EObject>) doc.eGet(materials);
        EObject mat = list.stream().filter(m -> name.equals(nameOf(m))).findFirst().orElse(null);
        if (mat == null) {
            mat = EcoreUtil.create(((EReference) materials).getEReferenceType());
            KARaceIngUtil.setName(mat, name);
            double[] data = materialData(name);
            if (data != null) {
                set(mat, "density", data[0]);
            }
            list.add(mat);
        }
        EStructuralFeature f = solid.eClass().getEStructuralFeature("material");
        if (solid.eGet(f) == mat) {
            return false;
        }
        solid.eSet(f, mat);
        markDerived(solid, "material", name);
        return true;
    }

    // ================================================================== target model helpers

    /** Sets a parameter of a BOM item (creating the value object if the item type defines the parameter). */
    private static void setBomParameter(EObject item, String name, String stringValue, Double doubleValue, List<String> changes) {
        EObject value = bomValue(item, name);
        if (value == null) {
            Object type = get(item, "type");
            if (!(type instanceof EObject t)) {
                return;
            }
            EObject definition = contents(t, "parameterDefinitions").stream().filter(pd -> name.equals(nameOf(pd))).findFirst().orElse(null);
            if (definition == null) {
                return;
            }
            EReference values = (EReference) item.eClass().getEStructuralFeature("parameterValues");
            EClass cls = (EClass) values.getEReferenceType().getEPackage().getEClassifier(doubleValue != null ? "DoubleParameterValue" : "StringParameterValue");
            value = EcoreUtil.create(cls);
            set(value, "definition", definition);
            if (doubleValue != null) {
                set(value, "unit", "kg");
            }
            contents(item, "parameterValues").add(value);
        }
        Object newValue = doubleValue != null ? doubleValue : stringValue;
        Object old = get(value, "value");
        if (setDerived(value, "value", newValue)) {
            changes.add(name + " " + (old == null ? "-" : fmtObj(old)) + " -> " + fmtObj(newValue) + (doubleValue != null ? " kg" : ""));
        }
    }

    private static EObject bomValue(EObject item, String name) {
        for (EObject v : contents(item, "parameterValues")) {
            Object def = get(v, "definition");
            if (def instanceof EObject d && name.equals(nameOf(d))) {
                return v;
            }
        }
        return null;
    }

    private static String bomString(EObject item, String name) {
        EObject v = bomValue(item, name);
        return v == null ? null : str(get(v, "value"));
    }

    private static EObject simParameter(EObject block, String name) {
        return contents(block, "parameters").stream().filter(p -> name.equals(nameOf(p))).findFirst().orElse(null);
    }

    /** Sets (or, if {@code create}, creates) a parameter of a Simulink block; true if something changed. */
    private static boolean setSimParameter(EObject block, String name, String value, String type, boolean create) {
        EObject p = simParameter(block, name);
        if (p == null) {
            if (!create) {
                return false;
            }
            EReference ref = (EReference) block.eClass().getEStructuralFeature("parameters");
            p = EcoreUtil.create(ref.getEReferenceType());
            set(p, "name", name);
            set(p, "type", type);
            set(p, "readOnly", false);
            contents(block, "parameters").add(p);
            markDerived(p, "name", name);
        }
        return setDerived(p, "value", value);
    }

    /** AR11: the parameters "signal:<pin>" of the block mirror the pins (and signals) of the device. */
    private static List<String> syncSignals(EObject device, EObject block) {
        Map<String, String> wanted = new LinkedHashMap<>();
        for (EObject pin : contents(device, "pins")) {
            wanted.put("signal:" + nameOf(pin), str(get(pin, "signal")));
        }
        List<String> changes = new ArrayList<>();
        for (EObject p : new ArrayList<>(contents(block, "parameters"))) {
            String n = nameOf(p);
            if (n.startsWith("signal:") && !wanted.containsKey(n)) {
                contents(block, "parameters").remove(p);
                changes.add("removed " + n);
            }
        }
        wanted.forEach((n, sig) -> {
            EObject existing = simParameter(block, n);
            String old = existing == null ? null : str(get(existing, "value"));
            if (setSimParameter(block, n, sig, "string", true)) {
                changes.add(existing == null ? "added " + n + " = " + sig : n + " " + old + " -> " + sig);
            }
        });
        return changes;
    }

    /** AR13: parameters of a new block are initialised with the mean of the numeric parameters of its sibling blocks. */
    private static List<String> initialiseFromSiblings(EObject block) {
        List<EObject> siblings = siblings(block);
        Map<String, List<Double>> values = new LinkedHashMap<>();
        for (EObject s : siblings) {
            for (EObject p : contents(s, "parameters")) {
                try {
                    values.computeIfAbsent(nameOf(p), k -> new ArrayList<>()).add(Double.parseDouble(str(get(p, "value"))));
                } catch (NumberFormatException e) {
                    // not numeric: not initialised
                }
            }
        }
        List<String> out = new ArrayList<>();
        values.forEach((n, vs) -> {
            String v = fmt(vs.stream().mapToDouble(Double::doubleValue).average().orElse(0));
            if (setSimParameter(block, n, v, "double", true)) {
                out.add(n + " = " + v);
            }
        });
        return out;
    }

    private static List<EObject> siblings(EObject block) {
        EObject container = block.eContainer();
        if (container == null || block.eContainingFeature() == null) {
            return List.of();
        }
        return contents(container, block.eContainingFeature().getName()).stream()
                .filter(s -> s != block && s.eClass() == block.eClass() && !contents(s, "parameters").isEmpty()).collect(Collectors.toList());
    }

    private static String siblingNames(EObject block) {
        return siblings(block).stream().map(KARaceIngOverlapRules::nameOf).collect(Collectors.joining(", "));
    }

    private static List<String> initialiseBomItem(EObject changed, EObject item, EObject overlapping) {
        if (overlapping != null && "Item".equals(overlapping.eClass().getName())) {
            Object type = get(overlapping, "type");
            if (type != null) {
                set(item, "type", type);
            }
            String id = nameOf(overlapping);
            String prefix = id.contains("-") ? id.substring(0, id.indexOf('-') + 1) : "";
            if (!prefix.isEmpty() && !nameOf(item).startsWith(prefix)) {
                set(item, "id", prefix + nameOf(changed));
                markDerived(item, "id", prefix + nameOf(changed));
            }
        }
        List<String> changes = new ArrayList<>();
        double m = mass(changed);
        if (m > 0) {
            setBomParameter(item, "mass", null, round(m, 3), changes);
        }
        String mat = singleMaterial(changed);
        if (mat != null) {
            setBomParameter(item, "material", mat, null, changes);
        }
        return List.of(note("Procurement Manager", "New BOM item " + nameOf(item) + " for the new part " + path(changed) + " (" + fileOf(changed) + ", "
                + role(changed) + ")" + (changes.isEmpty() ? "" : ": " + String.join(", ", changes)) + ". Supplier and part number are still to be"
                + " defined; order the part according to the CAD model."));
    }

    private static List<String> setThermalMaterial(EObject t, String material, List<String> changes) {
        List<String> out = changes == null ? new ArrayList<>() : changes;
        String old = str(get(t, "material"));
        if (setDerived(t, "material", material)) {
            out.add("material " + old + " -> " + material);
        }
        double[] data = materialData(material);
        if (data != null) {
            Object k = get(t, "thermalConductivity");
            Object cp = get(t, "specificHeatCapacity");
            if (setDerived(t, "thermalConductivity", data[2])) {
                out.add("thermal conductivity " + fmtObj(k) + " -> " + fmt(data[2]) + " W/(m K)");
            }
            if (setDerived(t, "specificHeatCapacity", data[1])) {
                out.add("specific heat capacity " + fmtObj(cp) + " -> " + fmt(data[1]) + " J/(kg K)");
            }
        }
        return out;
    }

    /** Thermal analyses that analyse the element are outdated: not passed any more, to be rerun. */
    private static List<String> outdateAnalyses(EObject element, String reason) {
        List<String> out = new ArrayList<>();
        EObject root = EcoreUtil.getRootContainer(element);
        for (var it = root.eAllContents(); it.hasNext();) {
            EObject o = it.next();
            if ("AnalysisCase".equals(o.eClass().getName()) && contentsOfRef(o, "analysedElements").contains(element)) {
                setDerived(o, "passed", false);
                setDerived(o, "result", "outdated (" + reason + "): rerun");
                out.add(nameOf(o));
            }
        }
        return out;
    }

    private static String outdatedText(List<String> outdated) {
        return outdated.isEmpty() ? "" : " Analyses based on the previous data are outdated (set to not passed): " + String.join(", ", outdated) + ".";
    }

    /** Test cases (validation model) that verify one of the requirements. */
    private static List<String> testsVerifying(EObject anyRequirement, Set<String> requirementNames) {
        List<String> out = new ArrayList<>();
        Resource r = anyRequirement.eResource();
        if (r == null || r.getResourceSet() == null) {
            return out;
        }
        for (Resource res : r.getResourceSet().getResources()) {
            if (res.getURI() != null && "val.testbench".equals(res.getURI().lastSegment())) {
                for (var it = res.getAllContents(); it.hasNext();) {
                    EObject o = it.next();
                    if ("TestCase".equals(o.eClass().getName())
                            && strings(get(o, "verifiedRequirements")).stream().anyMatch(requirementNames::contains)) {
                        out.add(nameOf(o));
                    }
                }
            }
        }
        return out;
    }

    // ================================================================== derived values and de-duplication

    private static boolean setDerived(EObject o, String feature, Object value) {
        EStructuralFeature f = o.eClass().getEStructuralFeature(feature);
        if (f == null) {
            return false;
        }
        Object old = o.eGet(f);
        if (Objects.equals(old, value) || (old != null && value != null && fmtObj(old).equals(fmtObj(value)) && !(value instanceof String))) {
            return false;
        }
        o.eSet(f, value);
        DERIVED_OLD.computeIfAbsent(o, k -> new HashMap<>()).put(feature, old == null ? null : String.valueOf(old));
        markDerived(o, feature, String.valueOf(value));
        return true;
    }

    private static void markDerived(EObject o, String feature, String value) {
        DERIVED.computeIfAbsent(o, k -> new HashMap<>()).put(feature, value);
    }

    private static boolean isDerived(EObject o, String feature, String value) {
        Map<String, String> m = DERIVED.get(o);
        return m != null && m.containsKey(feature) && Objects.equals(m.get(feature), value);
    }

    /** The mass of the anchor before the change: the mass parameter of an overlapping BOM item or SIM block (old value if already updated). */
    private static Double massBefore(Ctx c) {
        double now = mass(c.anchor);
        for (EObject o : c.overlaps) {
            EObject v = "Item".equals(o.eClass().getName()) ? bomValue(o, "mass") : simParameter(o, "mass");
            if (v == null) {
                continue;
            }
            String value = str(get(v, "value"));
            Map<String, String> old = DERIVED_OLD.get(v);
            try {
                if (old != null && old.get("value") != null && Math.abs(Double.parseDouble(value) - now) < 1e-3) {
                    return Double.parseDouble(old.get("value"));
                }
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    /** The note, unless the same note for (counterpart, anchor, rule) was sent for the current state of the anchor. */
    private static List<String> once(EObject counterpart, EObject anchor, String rule, String note) {
        // one notification per role and state of the anchor (even if the anchor overlaps with several of the role's elements)
        Map<String, String> m = NOTIFIED.computeIfAbsent(anchor, k -> new HashMap<>());
        String key = note.substring(0, Math.max(note.indexOf(']'), 0));
        String state = fingerprint(anchor);
        if (state.equals(m.get(key))) {
            return List.of();
        }
        m.put(key, state);
        return List.of(note);
    }

    /** Records that the owner of the counterpart was informed about the current state of the anchor. */
    private static void sent(EObject counterpart, EObject anchor) {
        String key = note(role(counterpart), "");
        NOTIFIED.computeIfAbsent(anchor, k -> new HashMap<>()).put(key.substring(0, key.indexOf(']')), fingerprint(anchor));
    }

    private static String fingerprint(EObject o) {
        StringBuilder sb = new StringBuilder();
        fingerprint(o, sb, 0);
        return sb.toString();
    }

    private static void fingerprint(EObject o, StringBuilder sb, int depth) {
        if (depth > 6 || sb.length() > 50000) {
            return;
        }
        sb.append(o.eClass().getName()).append('{');
        for (EStructuralFeature f : o.eClass().getEAllStructuralFeatures()) {
            Object v = o.eGet(f);
            if (f instanceof EReference r) {
                if (r.isContainment()) {
                    for (Object x : r.isMany() ? (Collection<?>) v : v == null ? List.of() : List.of(v)) {
                        fingerprint((EObject) x, sb, depth + 1);
                    }
                } else if (!r.isMany() && v instanceof EObject e) {
                    sb.append(f.getName()).append('=').append(nameOf(e)).append(';');
                }
            } else {
                sb.append(f.getName()).append('=').append(v).append(';');
            }
        }
        sb.append('}');
    }

    // ================================================================== naming, roles, messages

    /** Model kind of an element: cad, bom, brake, elec, sim, thermo, val, prj, uvl, config. */
    static String kind(EObject o) {
        String f = fileOf(o);
        if (f == null) {
            return packageKind(o);
        }
        if (f.endsWith(".cad")) {
            return "cad";
        }
        if (f.endsWith(".bom")) {
            return "bom";
        }
        if (f.endsWith(".brakesystem")) {
            return "brake";
        }
        if (f.endsWith(".electrical")) {
            return "elec";
        }
        if (f.endsWith(".simulink")) {
            return "sim";
        }
        if (f.endsWith(".thermal")) {
            return "thermo";
        }
        if (f.endsWith(".testbench")) {
            return "val";
        }
        if (f.endsWith(".requirements")) {
            return "prj";
        }
        return f.substring(f.lastIndexOf('.') + 1);
    }

    private static String packageKind(EObject o) {
        String p = o.eClass().getEPackage().getName().toLowerCase();
        return switch (p) {
        case "cad" -> "cad";
        case "bom" -> "bom";
        case "brakesystem" -> "brake";
        case "electrical" -> "elec";
        case "simulink" -> "sim";
        case "thermal" -> "thermo";
        case "testbench" -> "val";
        case "requirements" -> "prj";
        default -> p;
        };
    }

    static String fileOf(EObject o) {
        EObject root = o == null ? null : EcoreUtil.getRootContainer(o);
        Resource r = root == null ? null : root.eResource();
        return r == null || r.getURI() == null ? null : r.getURI().lastSegment();
    }

    /** The stakeholder role that owns the model of the element. */
    public static String role(EObject o) {
        String f = fileOf(o);
        if (f == null) {
            return ownerOfPackage(o);
        }
        return switch (f) {
        case "des.cad" -> "Design Engineer";
        case "crash.cad" -> "Crash Structure Engineer";
        case "kin.cad" -> "Kinematics Engineer";
        case "aero.cad" -> "Aerodynamics Engineer";
        case "thermo.thermal" -> "Thermodynamics Engineer";
        case "elec.electrical" -> "Electrical Engineer";
        case "sim.simulink" -> "Simulation Engineer";
        case "val.testbench" -> "Validation Engineer";
        case "karaceing.bom" -> "Procurement Manager";
        case "des.brakesystem" -> "Project Manager";
        case "prj.requirements" -> "Project Manager";
        default -> "Product Line Engineer";
        };
    }

    private static String ownerOfPackage(EObject o) {
        return switch (packageKind(o)) {
        case "cad" -> "Design Engineer";
        case "elec" -> "Electrical Engineer";
        case "sim" -> "Simulation Engineer";
        case "thermo" -> "Thermodynamics Engineer";
        case "val" -> "Validation Engineer";
        case "bom" -> "Procurement Manager";
        default -> "Project Manager";
        };
    }

    /** Overlap kind (see docs/OVERLAP_CATALOGUE.md) of the pair of models. */
    private static String kindLabel(EObject a, EObject b) {
        Set<String> p = new HashSet<>(List.of(kind(a), kind(b)));
        if (p.contains("brake")) {
            return "overlap K5";
        }
        if (p.contains("val")) {
            return "overlap K7";
        }
        if (p.equals(Set.of("cad"))) {
            return "overlap K9";
        }
        if (p.equals(Set.of("cad", "bom"))) {
            return "overlap K4";
        }
        if (p.equals(Set.of("elec", "bom"))) {
            return "overlap K11";
        }
        if (p.equals(Set.of("bom", "thermo"))) {
            return "overlap K6";
        }
        if (p.equals(Set.of("cad", "thermo"))) {
            return "overlap K2";
        }
        if (p.equals(Set.of("cad", "elec"))) {
            return "overlap K3";
        }
        if (p.equals(Set.of("cad", "sim"))) {
            return "overlap K1";
        }
        if (p.equals(Set.of("elec", "sim"))) {
            return "overlap K8";
        }
        return "overlap";
    }

    /** Path of names from the model root, e.g. DES/BrakeSystem/HydraulicBrakes/FrontBrakeDisc. */
    public static String path(EObject o) {
        List<String> names = new ArrayList<>();
        for (EObject x = o; x != null; x = x.eContainer()) {
            String n = nameOf(x);
            if (n != null && !n.equals(x.eClass().getName())) {
                names.add(0, n);
            }
        }
        return String.join("/", names);
    }

    private static String relativePath(EObject o) {
        return path(o);
    }

    static String nameOf(EObject o) {
        return KARaceIngUtil.nameOf(o);
    }

    private static String note(String role, String text) {
        return "[Notify → " + role + "] " + text;
    }

    private static String describeRequirements(List<EObject> reqs, int max) {
        List<String> parts = reqs.stream().map(r -> nameOf(r) + " '" + abbreviateText(str(get(r, "text")), 70) + "'").collect(Collectors.toList());
        return abbreviate(parts, max);
    }

    private static String abbreviate(List<String> items, int max) {
        if (items.size() <= max) {
            return String.join(", ", items);
        }
        return String.join(", ", items.subList(0, max)) + " and " + (items.size() - max) + " more";
    }

    private static String abbreviateText(String t, int max) {
        return t.length() <= max ? t : t.substring(0, max - 1) + "…";
    }

    static boolean isRequirement(EObject o) {
        return o != null && "Requirement".equals(o.eClass().getName());
    }

    static boolean isSolutionArtefact(EObject o) {
        String k = kind(o);
        return !Set.of("uvl", "config", "configuration").contains(k) && !"prj".equals(k);
    }

    private static List<EObject> requirementsOf(Iterable<EObject> traces) {
        List<EObject> out = new ArrayList<>();
        for (EObject t : KARaceIngUtil.alive(traces)) {
            if (isRequirement(t)) {
                out.add(t);
            }
        }
        return out;
    }

    /** The feature a rule looks at: the parameter name for BOM parameter values and Simulink parameters. */
    private static String effectiveFeature(EObject changed, String feature) {
        String cls = changed.eClass().getName();
        if (cls.endsWith("ParameterValue")) {
            Object def = get(changed, "definition");
            return def instanceof EObject d ? nameOf(d) : feature;
        }
        if ("Parameter".equals(cls)) {
            return nameOf(changed);
        }
        return feature;
    }

    // ------------------------------------------------------------------ reflection helpers

    static Object get(EObject o, String feature) {
        EStructuralFeature f = o == null ? null : o.eClass().getEStructuralFeature(feature);
        return f == null ? null : o.eGet(f);
    }

    private static void set(EObject o, String feature, Object value) {
        EStructuralFeature f = o.eClass().getEStructuralFeature(feature);
        if (f != null) {
            o.eSet(f, value);
        }
    }

    private static double d(EObject o, String feature) {
        Object v = get(o, feature);
        return v instanceof Number n ? n.doubleValue() : 0;
    }

    @SuppressWarnings("unchecked")
    private static EList<EObject> contents(EObject o, String reference) {
        EStructuralFeature f = o == null ? null : o.eClass().getEStructuralFeature(reference);
        if (f == null || !f.isMany() || !(f instanceof EReference)) {
            return new org.eclipse.emf.common.util.BasicEList<>();
        }
        return (EList<EObject>) o.eGet(f);
    }

    private static List<EObject> contentsOfRef(EObject o, String reference) {
        return new ArrayList<>(contents(o, reference));
    }

    private static List<String> strings(Object v) {
        if (v instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return v == null ? List.of() : List.of(String.valueOf(v));
    }

    private static List<String> listOf(String s) {
        return s == null ? List.of() : List.of(s);
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }

    private static double round(double v, int digits) {
        return BigDecimal.valueOf(v).setScale(digits, java.math.RoundingMode.HALF_UP).doubleValue();
    }

    static String fmt(double v) {
        if (v == 0) {
            return "0";
        }
        return new BigDecimal(v).round(new MathContext(5)).stripTrailingZeros().toPlainString();
    }

    private static String fmtObj(Object v) {
        return v instanceof Double dv ? fmt(dv) : String.valueOf(v);
    }

    /** The context of one change. */
    private static final class Ctx {
        final EObject changed;
        final EObject anchor;
        final String feature;
        final String rawFeature;
        final String oldValue;
        final String newValue;
        final List<EObject> requirements;
        final List<EObject> overlaps;
        final boolean derived;

        Ctx(EObject changed, EObject anchor, String feature, String oldValue, String newValue, List<EObject> requirements, List<EObject> overlaps,
                boolean derived) {
            this.derived = derived;
            this.changed = changed;
            this.anchor = anchor;
            this.feature = feature;
            this.rawFeature = changed.eClass().getName().endsWith("ParameterValue") || "Parameter".equals(changed.eClass().getName()) ? "value" : feature;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.requirements = requirements;
            this.overlaps = overlaps;
        }

        String change() {
            if ("children".equals(feature) || "pins".equals(feature)) {
                return newValue != null && !"null".equals(newValue) ? newValue + " added" : oldValue + " removed";
            }
            return feature + " " + (oldValue == null || "null".equals(oldValue) ? "-" : oldValue) + " -> " + (newValue == null ? "-" : newValue);
        }

        /** Change origin tracing: the requirements traced to the changed artefact (most recent first). */
        String origin() {
            if (requirements.isEmpty()) {
                return "";
            }
            List<EObject> recent = new ArrayList<>(requirements);
            recent.sort((a, b) -> Long.compare(RECENT.getOrDefault(b, 0L), RECENT.getOrDefault(a, 0L)));
            return " Origin: requirements traced to " + nameOf(anchor) + ": " + describeRequirements(recent, 3) + ".";
        }
    }

    /** Distinct notifications in order (helper for the tests). */
    public static List<String> distinct(List<String> notes) {
        return new ArrayList<>(new LinkedHashSet<>(notes));
    }
}
