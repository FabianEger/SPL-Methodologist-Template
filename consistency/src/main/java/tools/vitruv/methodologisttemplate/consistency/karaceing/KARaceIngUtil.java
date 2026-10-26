package tools.vitruv.methodologisttemplate.consistency.karaceing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;

import UVLPackage.Feature;
import UVLPackage.FeatureTree;
import UVLPackage.Group;
import tools.vitruv.methodologisttemplate.consistency.EMFUtil;
import tools.vitruv.methodologisttemplate.consistency.FeatureTreeUtil;

/**
 * Helper functions used by the KA-RaceIng reactions (feature model navigation, domain labels and
 * the generic creation of counterparts for overlapping artefacts).
 */
public final class KARaceIngUtil {

    /** Correspondence tag between semantically overlapping artefacts of two domains. */
    public static final String OVERLAP = "overlap";
    /** Correspondence tag between a requirement of the PRJ specification and an artefact. */
    public static final String REQUIREMENT = "requirement";

    private KARaceIngUtil() {
    }

    // ------------------------------------------------------------------ naming

    /** Displayable name (name, id or instanceName attribute, else the class name). */
    public static String nameOf(EObject object) {
        return object == null ? "<none>" : EMFUtil.getNameOf(object);
    }

    /**
     * Label of the domain model an element belongs to: the name of the root element of its
     * resource (DES, CRASH, KIN, AERO, ELEC, SIM, THERMO, VAL, PRJ, the BOM or BRAKE). For an
     * element that was already removed from its model, "removed element" is returned.
     */
    public static String domainOf(EObject object) {
        if (object == null || object.eResource() == null || object.eResource().getContents().isEmpty()) {
            return "removed element";
        }
        return nameOf(object.eResource().getContents().get(0));
    }

    /** true if the element is still contained in a model resource (not deleted). */
    /**
     * The main realisation among several elements that realise the same feature: the outermost element (not
     * contained in another candidate); if several are outermost, the only one that can contain elements.
     * Returns null if this is still ambiguous.
     */
    public static EObject outermost(List<? extends EObject> candidates) {
        List<EObject> outer = new ArrayList<>();
        for (EObject c : candidates) {
            if (candidates.stream().noneMatch(o -> o != c && EcoreUtil.isAncestor(o, c))) {
                outer.add(c);
            }
        }
        if (outer.size() == 1) {
            return outer.get(0);
        }
        List<EObject> containers = outer.stream().filter(o -> !o.eClass().getEAllContainments().isEmpty()).toList();
        return containers.size() == 1 ? containers.get(0) : null;
    }

    public static boolean isAlive(EObject object) {
        return object != null && object.eResource() != null;
    }

    /** The elements that are still contained in a model (the result of "retrieve many" is an Iterable). */
    public static <T extends EObject> List<T> alive(Iterable<T> objects) {
        List<T> result = new ArrayList<>();
        if (objects != null) {
            for (T object : objects) {
                if (isAlive(object)) {
                    result.add(object);
                }
            }
        }
        return result;
    }

    // ------------------------------------------------------------------ feature model

    /** The feature and all features below it, parents before children. */
    public static List<Feature> featureAndDescendants(Feature feature) {
        List<Feature> result = new ArrayList<>();
        result.add(feature);
        feature.eAllContents().forEachRemaining(o -> {
            if (o instanceof Feature f) {
                result.add(f);
            }
        });
        return result;
    }

    /** The parent feature of a feature, or null for the root feature or a detached feature. */
    public static Feature parentFeature(Feature feature) {
        if (feature == null || feature.eContainer() == null) {
            return null;
        }
        EObject parent = feature.eContainer().eContainer();
        return parent instanceof Feature f ? f : null;
    }

    /**
     * The domain root feature (the child of the root feature) above or equal to the given feature,
     * or null if the feature is the root feature itself or detached.
     */
    public static Feature domainRootOf(Feature feature) {
        Feature current = feature;
        while (current != null) {
            Feature parent = parentFeature(current);
            if (parent != null && FeatureTreeUtil.isRootFeature(parent)) {
                return current;
            }
            current = parent;
        }
        return null;
    }

    /** All features of the domain (stakeholder role) the given feature belongs to, root first. */
    public static List<Feature> featuresOfDomain(Feature anyFeatureOfDomain) {
        Feature domainRoot = domainRootOf(anyFeatureOfDomain);
        return domainRoot == null ? List.of(anyFeatureOfDomain) : featureAndDescendants(domainRoot);
    }

    /** All features of the feature model the given feature belongs to (root first). */
    public static List<Feature> allFeatures(Feature anyFeature) {
        Feature root = anyFeature;
        while (parentFeature(root) != null) {
            root = parentFeature(root);
        }
        return featureAndDescendants(root);
    }

    /** All features of the feature model: those of the domain of the given feature first, then the others. */
    public static List<Feature> domainFirst(Feature anyFeatureOfDomain) {
        List<Feature> result = new ArrayList<>(featuresOfDomain(anyFeatureOfDomain));
        for (Feature f : allFeatures(anyFeatureOfDomain)) {
            if (!result.contains(f)) {
                result.add(f);
            }
        }
        return result;
    }

    public static List<String> featureNames(List<Feature> features) {
        return features.stream().map(Feature::getName).collect(Collectors.toList());
    }

    /** Finds a feature by name in the feature model the given element belongs to. */
    public static Feature findFeature(EObject anyElementOfModel, String name) {
        EObject root = EcoreUtil.getRootContainer(anyElementOfModel);
        for (var it = root.eAllContents(); it.hasNext();) {
            if (it.next() instanceof Feature f && name.equals(f.getName())) {
                return f;
            }
        }
        return null;
    }

    /** true if the object is the feature tree (used to recognise the root group). */
    public static boolean isFeatureTree(EObject object) {
        return object instanceof FeatureTree;
    }

    /** The group a feature is directly contained in. */
    public static Group groupOf(Feature feature) {
        return feature.eContainer() instanceof Group g ? g : null;
    }

    // ------------------------------------------------------------------ overlapping artefacts

    /**
     * The element below which a counterpart for a new child of an overlapping artefact is created.
     * If the overlapping artefact can contain elements of its own kind (an assembly, a circuit, a
     * subsystem, a thermal system, a test suite, a section), it is used itself. Otherwise (e.g. a
     * part, a device, a BOM item, a brake component) its container is used, so the counterpart
     * becomes a sibling.
     */
    public static EObject counterpartContainer(EObject overlapping) {
        if (!creatableClasses(overlapping).isEmpty() || overlapping.eContainer() == null) {
            return overlapping;
        }
        return overlapping.eContainer();
    }

    /**
     * Concrete classes that can be created below the container as "peer" elements, i.e. classes
     * conforming to the type of the containment reference that holds the container itself (for a
     * model root: all classes of its containment references).
     */
    public static List<EClass> creatableClasses(EObject container) {
        List<EClass> all = EMFUtil.findContainedClasses(container.eClass());
        EReference holding = container.eContainmentFeature();
        if (holding == null) {
            return all.stream().distinct().collect(Collectors.toList());
        }
        EClass peerType = holding.getEReferenceType();
        return all.stream().filter(peerType::isSuperTypeOf).distinct().collect(Collectors.toList());
    }

    public static List<String> classNames(List<EClass> classes) {
        return EMFUtil.getAllNamesOfEClass(classes);
    }

    /**
     * Creates an instance of the given class below the container (in the first containment
     * reference that accepts it) and names it. BOM items get the item type "Part" (or the first
     * type) since an item without a type is not meaningful.
     */
    @SuppressWarnings("unchecked")
    public static EObject createChild(EObject container, EClass eClass, String name) {
        EObject created = EcoreUtil.create(eClass);
        setName(created, name);
        for (EReference reference : container.eClass().getEAllContainments()) {
            if (reference.getEReferenceType().isSuperTypeOf(eClass)) {
                if (reference.isMany()) {
                    ((EList<EObject>) container.eGet(reference)).add(created);
                } else {
                    container.eSet(reference, created);
                }
                initialiseBomItem(container, created);
                return created;
            }
        }
        throw new IllegalArgumentException(eClass.getName() + " cannot be contained in " + container.eClass().getName());
    }

    /** Sets the name (or id / instanceName) attribute if the class has one. */
    public static void setName(EObject object, String name) {
        for (String attribute : List.of("name", "id", "instanceName")) {
            EStructuralFeature feature = object.eClass().getEStructuralFeature(attribute);
            if (feature != null && feature.getEType().getInstanceClass() == String.class) {
                object.eSet(feature, name);
                return;
            }
        }
    }

    private static void initialiseBomItem(EObject container, EObject created) {
        EStructuralFeature type = created.eClass().getEStructuralFeature("type");
        EStructuralFeature itemTypes = container.eClass().getEStructuralFeature("itemTypes");
        if (type == null || itemTypes == null || !created.eClass().getName().equals("Item")) {
            return;
        }
        @SuppressWarnings("unchecked")
        List<EObject> types = (List<EObject>) container.eGet(itemTypes);
        types.stream().filter(t -> "Part".equals(nameOf(t))).findFirst()
                .or(() -> types.stream().findFirst())
                .ifPresent(t -> created.eSet(type, t));
        EStructuralFeature count = created.eClass().getEStructuralFeature("numberOfItems");
        if (count != null) {
            created.eSet(count, 1);
        }
    }

    /** The element itself and all named elements below it (candidates for "link existing"). */
    public static List<EObject> namedContents(EObject element) {
        List<EObject> result = new ArrayList<>();
        result.add(element);
        element.eAllContents().forEachRemaining(o -> {
            if (o.eClass().getEStructuralFeature("name") != null || o.eClass().getEStructuralFeature("id") != null) {
                result.add(o);
            }
        });
        return result;
    }

    /** true for a requirement of the requirements specification. */
    public static boolean isRequirement(EObject object) {
        return KARaceIngOverlapRules.isRequirement(object);
    }

    /** true for an artefact a requirement can trace to (alive, in a solution-space model, not a feature or requirement). */
    public static boolean isTraceableArtefact(EObject object) {
        return isAlive(object) && !(object instanceof Feature) && KARaceIngOverlapRules.isSolutionArtefact(object);
    }

    public static List<String> nameList(List<? extends EObject> objects) {
        return objects.stream().map(KARaceIngUtil::nameOf).collect(Collectors.toList());
    }
}
