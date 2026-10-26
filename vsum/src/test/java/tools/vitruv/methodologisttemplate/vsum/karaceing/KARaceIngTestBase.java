package tools.vitruv.methodologisttemplate.vsum.karaceing;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInfo;

import UVLPackage.Feature;
import UVLPackage.Group;
import UVLPackage.UVLModel;
import UVLPackage.uvlFactory;
import configurationPackage.Configuration;
import configurationPackage.ConfigurationModel;
import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.change.composite.propagation.ChangePropagationListener;
import tools.vitruv.change.correspondence.view.EditableCorrespondenceModelView;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondenceFactory;
import tools.vitruv.dsls.reactions.runtime.correspondence.ReactionsCorrespondence;
import tools.vitruv.framework.views.CommittableView;
import tools.vitruv.framework.views.View;
import tools.vitruv.framework.views.ViewTypeFactory;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;

/**
 * Helpers of the KA-RaceIng scenario tests. Every test class opens the initial state of the product
 * line in its own {@code @BeforeEach} method with {@link #openInitialState(Path)}: the persisted VSUM in
 * {@code src/test/resources/initialstate} is copied into the temporary folder of the test and opened with
 * all KA-RaceIng consistency preservation rules (see {@link InitialStateLoader}). The helpers change
 * models through views, script the answers to the dialogs of the reactions, and inspect the VSUM
 * (models and correspondences).
 *
 * <p>Dialog answers are matched by message text. Every test first registers the answers that matter
 * for its scenario and then calls {@link #answerRemainingDialogsWithDefaults()}, which answers all other
 * dialogs conservatively (ignore overlaps, keep artefacts and features, no new features; a new artefact is
 * mapped to the features of the element it overlaps with or to the feature of its containing element).
 *
 * <p>After every test the rule "every artefact corresponds to a feature" is checked on the resulting state.
 */
public abstract class KARaceIngTestBase {

    protected static final String NEW = "New";
    protected static final String EXISTING = "Existing";

    /**
     * If this system property is set (e.g. {@code -Dkaraceing.snapshotDir=target/snapshots}), the VSUM state
     * each test leaves behind is copied to {@code <dir>/<TestClass>.<method>} (used to visualise how the
     * scenarios change the initial state; see karaceing-generator/diagrams). Off by default.
     */
    public static final String SNAPSHOT_PROPERTY = "karaceing.snapshotDir";

    protected TestUserInteraction userInteraction;
    protected InternalVirtualModel vsum;
    private Path storageFolder;
    private final List<String> propagationLog = new ArrayList<>();
    private java.util.Map<String, String> correspondencesBefore;
    /** All notifications shown so far (messages "[Notify → role] ..."), and those of the running propagation. */
    protected final List<String> notifications = new ArrayList<>();
    private final List<String> notificationsOfPropagation = new ArrayList<>();

    /**
     * Opens the initial state: copies the persisted VSUM (models, vsum/models.models, vsum/uuid.uuid,
     * vsum/correspondences.correspondence) into the given folder and creates the VSUM on it with
     * {@link KARaceIngChangePropagationSpecifications#all()}.
     */
    protected void openInitialState(Path storageFolder) {
        this.storageFolder = storageFolder;
        userInteraction = new TestUserInteraction();
        vsum = InitialStateLoader.load(storageFolder, KARaceIngChangePropagationSpecifications.all(), userInteraction, message -> {
            notifications.add(message);
            notificationsOfPropagation.add(message);
        });
        if (snapshotsEnabled()) {
            vsum.addChangePropagationListener(new ChangePropagationListener() {
                @Override
                public void startedChangePropagation(VitruviusChange<tools.vitruv.change.atomic.uuid.Uuid> change) {
                    // the VSUM was saved after the previous propagation: the persisted correspondences are the current ones
                    correspondencesBefore = PropagationLog.correspondences(vsum);
                    notificationsOfPropagation.clear();
                }

                @Override
                public void finishedChangePropagation(Iterable<PropagatedChange> changes) {
                    // the VSUM is saved before this callback: the persisted correspondences include this propagation
                    propagationLog.add(PropagationLog.step(changes, correspondencesBefore, PropagationLog.correspondences(vsum),
                            new ArrayList<>(notificationsOfPropagation)));
                }
            });
        }
    }

    private static boolean snapshotsEnabled() {
        String dir = System.getProperty(SNAPSHOT_PROPERTY);
        return dir != null && !dir.isBlank();
    }

    /** Elements that do not represent anything on their own (value objects); all other model elements are artefacts. */
    protected static final java.util.Set<String> VALUE_OBJECTS = java.util.Set.of("Material", "LineSegment", "Circle", "Vector2",
            "Constraint", "Parameter", "TestStep", "CarValue", "ItemType", "ParameterDefinition", "StringParameterValue",
            "DoubleParameterValue", "IntegerParameterValue", "BoolParameterValue", "Pin", "BusSignalMapping", "IdentifierReference",
            "LibraryLinkReference");

    /** Artefacts of the solution-space models that do not correspond to a feature (tag ""); the rule says: none. */
    protected List<String> artefactsWithoutFeature() {
        List<String> without = new ArrayList<>();
        for (String file : InitialStateLoader.MODEL_FILES) {
            if (file.equals("karaceing.uvl") || file.equals("karaceing.config")) {
                continue;
            }
            EObject root = inVsum(file, null);
            List<EObject> all = new ArrayList<>(List.of(root));
            root.eAllContents().forEachRemaining(all::add);
            for (EObject o : all) {
                if (!VALUE_OBJECTS.contains(o.eClass().getName())
                        && correspondences().getCorrespondingEObjects(o, "").stream().noneMatch(Feature.class::isInstance)) {
                    without.add(file + ": " + KARaceIngUtil.nameOf(o) + " : " + o.eClass().getName());
                }
            }
        }
        return without;
    }

    @AfterEach
    void disposeVsum(TestInfo testInfo) {
        if (vsum != null) {
            // the rule must hold after every evolution step: every artefact corresponds to a feature
            List<String> without = artefactsWithoutFeature();
            vsum.dispose();
            org.junit.jupiter.api.Assertions.assertTrue(without.isEmpty(), "artefacts without feature after the test: " + without);
            if (snapshotsEnabled()) {
                Path target = Path.of(System.getProperty(SNAPSHOT_PROPERTY), getClass().getSimpleName() + "." + testInfo.getTestMethod().get().getName());
                copyFolder(storageFolder, target);
                try {
                    Files.writeString(target.resolve("propagation.json"), "[" + String.join(",\n", propagationLog) + "]");
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
    }

    private static void copyFolder(Path source, Path target) {
        try (var paths = Files.walk(source)) {
            for (Path p : (Iterable<Path>) paths::iterator) {
                Path t = target.resolve(source.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(t);
                } else {
                    Files.copy(p, t, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------------------ views

    /** Creates a view on the roots of the given types. */
    protected View view(Collection<Class<?>> rootTypes) {
        var selector = vsum.createSelector(ViewTypeFactory.createIdentityMappingViewType("default"));
        selector.getSelectableElements().stream()
                .filter(element -> rootTypes.stream().anyMatch(type -> type.isInstance(element)))
                .forEach(element -> selector.setSelected(element, true));
        return selector.createView();
    }

    /** Changes models of the given root types in one view; the changes are recorded and propagated on commit. */
    protected void change(Collection<Class<?>> rootTypes, Consumer<CommittableView> modification) {
        CommittableView view = view(rootTypes).withChangeRecordingTrait();
        modification.accept(view);
        view.commitChanges();
        try {
            view.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** Reads from a fresh view on the roots of the given types. */
    protected <T> T read(Collection<Class<?>> rootTypes, Function<View, T> reader) {
        View view = view(rootTypes);
        try {
            return reader.apply(view);
        } finally {
            try {
                view.close();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /** The root of the given type whose name (or id / instanceName) is the given one, e.g. the CAD document "DES". */
    protected static <T extends EObject> T root(View view, Class<T> type, String name) {
        return view.getRootObjects(type).stream()
                .filter(root -> name == null || name.equals(InitialStateLoader.nameOf(root)))
                .findFirst().orElseThrow(() -> new AssertionError("No root " + type.getSimpleName() + " named " + name));
    }

    protected static <T extends EObject> T root(View view, Class<T> type) {
        return root(view, type, null);
    }

    /** The element with the given name below the given element (or null). */
    @SuppressWarnings("unchecked")
    protected static <T extends EObject> T find(EObject container, String name) {
        return (T) InitialStateLoader.findByName(container, name);
    }

    /** The element with the given name below the given element; fails if it does not exist. */
    protected static <T extends EObject> T get(EObject container, String name) {
        T found = find(container, name);
        assertNotNull(found, "expected element " + name + " below " + InitialStateLoader.nameOf(container));
        return found;
    }

    protected static List<String> childNames(EObject element) {
        return element.eContents().stream().map(InitialStateLoader::nameOf).collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ feature model changes

    protected void changeFeatureModel(Consumer<UVLModel> modification) {
        change(List.of(UVLModel.class), view -> modification.accept(root(view, UVLModel.class)));
    }

    /** Adds the features in a new group of the given kind (Mandatory, Optional, Alternative, Or) below the parent. */
    protected static List<Feature> addFeatures(UVLModel model, String parentName, String groupKind, String... names) {
        Feature parent = KARaceIngUtil.findFeature(model, parentName);
        assertNotNull(parent, "parent feature " + parentName);
        Group group = switch (groupKind) {
            case "Mandatory" -> uvlFactory.eINSTANCE.createMandatory();
            case "Optional" -> uvlFactory.eINSTANCE.createOptional();
            case "Alternative" -> uvlFactory.eINSTANCE.createAlternative();
            case "Or" -> uvlFactory.eINSTANCE.createOr();
            default -> throw new IllegalArgumentException(groupKind);
        };
        List<Feature> features = new ArrayList<>();
        for (String name : names) {
            Feature feature = uvlFactory.eINSTANCE.createFeature();
            feature.setName(name);
            group.getFeature().add(feature);
            feature.setGroup(group);
            features.add(feature);
        }
        parent.getChildren().add(group);
        return features;
    }

    /** Adds the feature to an existing group of the parent (the n-th group). */
    protected static Feature addFeatureToGroup(UVLModel model, String parentName, int groupIndex, String name) {
        Feature parent = KARaceIngUtil.findFeature(model, parentName);
        Group group = parent.getChildren().get(groupIndex);
        Feature feature = uvlFactory.eINSTANCE.createFeature();
        feature.setName(name);
        group.getFeature().add(feature);
        feature.setGroup(group);
        return feature;
    }

    /**
     * Removes a feature (and the features below it) from its group. The group itself stays (possibly
     * empty), so that the reactions can still determine the parent feature of the removed feature.
     */
    protected static void removeFeature(UVLModel model, String name) {
        Feature feature = KARaceIngUtil.findFeature(model, name);
        assertNotNull(feature, "feature to remove " + name);
        Group group = (Group) feature.eContainer();
        group.getFeature().remove(feature);
    }

    // ------------------------------------------------------------------ configurations

    /** The configuration of the car (EV3, CV2, CV1 are the configurations 0, 1, 2 of the configuration model). */
    protected Configuration configuration(View view, String car) {
        int index = InitialStateLoader.CARS.indexOf(car);
        if (index < 0) {
            throw new AssertionError("unknown car " + car);
        }
        return root(view, ConfigurationModel.class).getConfiguration().get(index);
    }

    protected Configuration readConfiguration(String name) {
        return read(List.of(ConfigurationModel.class), view -> EcoreUtil.copy(configuration(view, name)));
    }

    /**
     * The new feature has been added to the configuration of the car (by feature2config) and is not selected.
     * Whether the product is still valid is not checked here (VitruvOCL).
     */
    protected void assertAddedUnselected(String car, String feature) {
        configurationPackage.Feature configFeature = configFeature(readConfiguration(car), feature);
        org.junit.jupiter.api.Assertions.assertNotNull(configFeature, feature + " added to the configuration of " + car);
        org.junit.jupiter.api.Assertions.assertFalse(configFeature.isIsSelected(), feature + " not selected in " + car);
    }

    protected static configurationPackage.Feature configFeature(Configuration configuration, String name) {
        return configuration.getFeature().stream().filter(f -> name.equals(f.getName())).findFirst().orElse(null);
    }

    // ------------------------------------------------------------------ correspondences

    protected EditableCorrespondenceModelView<ReactionsCorrespondence> correspondences() {
        return vsum.getCorrespondenceModel()
                .getEditableView(ReactionsCorrespondence.class, CorrespondenceFactory.eINSTANCE::createReactionsCorrespondence);
    }

    /** The element in the VSUM (not a view copy) with the given name in the given model file. */
    protected EObject inVsum(String file, String name) {
        for (var resource : vsum.getViewSourceModels()) {
            if (resource.getURI().lastSegment().equals(file) && !resource.getContents().isEmpty()) {
                EObject root = resource.getContents().get(0);
                return name == null || name.equals(InitialStateLoader.nameOf(root)) ? root : InitialStateLoader.findByName(root, name);
            }
        }
        return null;
    }

    /** Names of the elements in the given model that correspond to the element (with the given tag, null = any). */
    protected List<String> correspondingNames(String file, String name, String tag, String targetFile) {
        EObject element = inVsum(file, name);
        assertNotNull(element, "element " + name + " in " + file);
        return correspondences().getCorrespondingEObjects(element, tag).stream()
                .filter(o -> targetFile == null || (o.eResource() != null && o.eResource().getURI().lastSegment().equals(targetFile)))
                .map(InitialStateLoader::nameOf)
                .collect(Collectors.toList());
    }

    /** Names of the features corresponding to the element of the given model. */
    protected List<String> featuresOf(String file, String name) {
        return correspondingNames(file, name, null, "karaceing.uvl");
    }

    /** Names of the artefacts in the given model that realise the feature. */
    protected List<String> artefactsOf(String featureName, String targetFile) {
        return correspondingNames("karaceing.uvl", featureName, "", targetFile);
    }

    // ------------------------------------------------------------------ dialogs

    /** Answers the next dialog whose message contains all given fragments with the given choice. */
    protected void answer(String choice, String... messageFragments) {
        userInteraction.onMultipleChoiceSingleSelection(d -> containsAll(d.getMessage(), messageFragments)).respondWith(choice);
    }

    /** Answers every dialog whose message contains all given fragments with the given choice. */
    protected void answerAlways(String choice, String... messageFragments) {
        userInteraction.onMultipleChoiceSingleSelection(d -> containsAll(d.getMessage(), messageFragments)).always().respondWith(choice);
    }

    /** Pattern dialogs of the Feature CPRs for a new feature: map to a new element of the given class. */
    protected void answerNewElementForFeature(String feature, String elementClass) {
        answer(NEW, "map the new feature " + feature + " to an existing element");
        answer(elementClass, "to be correspondent to the feature " + feature + "?");
    }

    /** Pattern dialogs of the Feature CPRs for a new feature: map to the existing element with the given name. */
    protected void answerExistingElementForFeature(String feature, String elementName) {
        answer(EXISTING, "map the new feature " + feature + " to an existing element");
        answer(elementName, "do you want to map the new feature " + feature + "?");
    }

    /**
     * Conservative answers for all dialogs not scripted by the test: overlaps of new elements are ignored,
     * counterparts of removed elements are kept, new artefacts get no feature, artefacts of removed features
     * are kept without feature and features without realisation are kept.
     */
    /** Answers all matching dialogs with the first choice that starts with the given prefix. */
    protected void answerAlwaysStartingWith(String choicePrefix, String... messageFragments) {
        userInteraction.onMultipleChoiceSingleSelection(d -> containsAll(d.getMessage(), messageFragments)).always()
                .respondWithChoiceMatching(choice -> choice.startsWith(choicePrefix));
    }

    protected void answerRemainingDialogsWithDefaults() {
        answerAlways("Ignore", "[Overlap]", "was added to");
        answerAlways("Keep counterpart", "[Overlap]", "was removed");
        // every artefact corresponds to a feature: a new artefact gets the features of the element it overlaps
        // with, otherwise the feature of its containing element
        answerAlwaysStartingWith("Features of the overlapping element", "[FCPR] The new", "It overlaps with");
        answerAlways("Feature of the containing element", "[FCPR] The new");
        answerAlwaysStartingWith("Keep artefact and map it to", "[FCPR] The feature", "was removed");
        answerAlways("Keep feature", "[FCPR] The feature", "has no realisation");
    }

    // ------------------------------------------------------------------ notifications

    /** The notifications addressed to the role (without the "[Notify → role] " prefix). */
    protected List<String> notificationsTo(String role) {
        String prefix = "[Notify \u2192 " + role + "] ";
        return notifications.stream().filter(n -> n.startsWith(prefix)).map(n -> n.substring(prefix.length())).collect(Collectors.toList());
    }

    /** Asserts that the role got a notification containing all fragments; returns it. */
    protected String assertNotified(String role, String... fragments) {
        return notificationsTo(role).stream().filter(n -> containsAll(n, fragments)).findFirst()
                .orElseThrow(() -> new AssertionError("no notification to " + role + " containing " + List.of(fragments) + "; got " + notificationsTo(role)));
    }

    /** Asserts that the role got no notification containing all fragments. */
    protected void assertNotNotified(String role, String... fragments) {
        notificationsTo(role).stream().filter(n -> containsAll(n, fragments)).findFirst().ifPresent(n -> {
            throw new AssertionError("unexpected notification to " + role + ": " + n);
        });
    }

    private static boolean containsAll(String message, String... fragments) {
        for (String fragment : fragments) {
            if (message == null || !message.contains(fragment)) {
                return false;
            }
        }
        return true;
    }
}
