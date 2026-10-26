package tools.vitruv.methodologisttemplate.vsum.karaceing;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import UVLPackage.uvlPackage;
import configurationPackage.ConfigurationPackagePackage;
import tools.vitruv.change.propagation.ChangePropagationMode;
import tools.vitruv.change.propagation.ChangePropagationSpecification;
import tools.vitruv.change.testutils.TestUserInteraction;
import tools.vitruv.framework.vsum.VirtualModelBuilder;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.model.bom.BomPackage;
import tools.vitruv.methodologisttemplate.model.cad.CadPackage;
import tools.vitruv.methodologisttemplate.model.electrical.ElectricalPackage;
import tools.vitruv.methodologisttemplate.model.requirements.RequirementsPackage;
import tools.vitruv.methodologisttemplate.model.simulink.SimuLinkPackage;
import tools.vitruv.methodologisttemplate.model.testbench.TestbenchPackage;
import tools.vitruv.methodologisttemplate.model.thermal.ThermalPackage;

/**
 * Opens the initial state of the KA-RaceIng product line as a VSUM.
 *
 * <p>The initial state is a persisted VSUM in {@code src/test/resources/initialstate}, in the layout
 * Vitruvius uses for its storage folder (compare {@code vsumexample/}):
 * <ul>
 * <li>the model files ({@code karaceing.uvl}, {@code karaceing.config}, {@code des.cad}, ...),</li>
 * <li>{@code vsum/models.models} (the models of the VSUM),</li>
 * <li>{@code vsum/uuid.uuid} (the UUID of every model element),</li>
 * <li>{@code vsum/correspondences.correspondence} (the correspondence model: feature to artefact,
 * overlapping artefacts of different domains, requirement traces, UVL model to configuration model,
 * feature to configuration feature).</li>
 * </ul>
 * The folder is copied to the storage folder of the test, and the VSUM is created on it with
 * {@link VirtualModelBuilder}, which loads the models, UUIDs and correspondences. Vitruvius stores
 * absolute file URIs in {@code models.models} and {@code uuid.uuid}; the files contain the placeholder
 * {@value #PLACEHOLDER}, which is replaced by the storage folder of the test.
 */
public final class InitialStateLoader {

    /** Model files of the initial state. */
    public static final List<String> MODEL_FILES = List.of(
            "karaceing.uvl", "karaceing.config", "prj.requirements", "des.cad", "crash.cad", "kin.cad", "aero.cad",
            "thermo.thermal", "elec.electrical", "sim.simulink", "val.testbench", "karaceing.bom", "des.brakesystem");

    /** Order of the configurations in karaceing.config (the configuration metamodel has no name attribute). */
    public static final List<String> CARS = List.of("EV3", "CV2", "CV1");

    static final String PLACEHOLDER = "file:/KARACEING_INITIAL_STATE";
    private static final String RESOURCE_FOLDER = "/initialstate";

    private InitialStateLoader() {
    }

    /** Registers the XMI resource factory and all generated packages (by accessing their eINSTANCE). */
    public static void registerPackages() {
        Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
        List.of(uvlPackage.eINSTANCE, ConfigurationPackagePackage.eINSTANCE, CadPackage.eINSTANCE, RequirementsPackage.eINSTANCE,
                ThermalPackage.eINSTANCE, TestbenchPackage.eINSTANCE, ElectricalPackage.eINSTANCE, SimuLinkPackage.eINSTANCE,
                BomPackage.eINSTANCE, brakesystem.BrakesystemPackage.eINSTANCE,
                tools.vitruv.change.correspondence.CorrespondencePackage.eINSTANCE,
                tools.vitruv.dsls.reactions.runtime.correspondence.CorrespondencePackage.eINSTANCE);
    }

    /**
     * Copies the persisted initial state into the given (empty) folder and opens the VSUM on it with the
     * given change propagation specifications and user interaction.
     */
    public static InternalVirtualModel load(Path folder, Iterable<ChangePropagationSpecification> specifications,
            TestUserInteraction userInteraction) {
        return load(folder, specifications, userInteraction, message -> {
        });
    }

    /**
     * As above; notifications (the role-addressed messages of the overlap rules) need no scripted answer: they are
     * passed to the given sink.
     */
    public static InternalVirtualModel load(Path folder, Iterable<ChangePropagationSpecification> specifications,
            TestUserInteraction userInteraction, java.util.function.Consumer<String> notifications) {
        registerPackages();
        Path storageFolder = copyInitialState(folder);
        InternalVirtualModel vsum = new VirtualModelBuilder()
                .withStorageFolder(storageFolder)
                .withUserInteractorForResultProvider(new NotificationRecordingResultProvider(new TestUserInteraction.ResultProvider(userInteraction), notifications))
                .withChangePropagationSpecifications(specifications)
                .buildAndInitialize();
        vsum.setChangePropagationMode(ChangePropagationMode.TRANSITIVE_CYCLIC);
        System.out.println("Initial state loaded: " + vsum.getViewSourceModels().size() + " models from " + storageFolder);
        return vsum;
    }

    /** Delegates to the scripted test answers, except for notifications, which are recorded. */
    private record NotificationRecordingResultProvider(tools.vitruv.change.interaction.InteractionResultProvider delegate,
            java.util.function.Consumer<String> sink) implements tools.vitruv.change.interaction.InteractionResultProvider {

        @Override
        public boolean getConfirmationInteractionResult(tools.vitruv.change.interaction.UserInteractionOptions.WindowModality windowModality,
                String title, String message, String positiveDecisionText, String negativeDecisionText, String cancelDecisionText) {
            return delegate.getConfirmationInteractionResult(windowModality, title, message, positiveDecisionText, negativeDecisionText, cancelDecisionText);
        }

        @Override
        public void getNotificationInteractionResult(tools.vitruv.change.interaction.UserInteractionOptions.WindowModality windowModality, String title,
                String message, String positiveDecisionText, tools.vitruv.change.interaction.UserInteractionOptions.NotificationType notificationType) {
            sink.accept(message);
        }

        @Override
        public String getTextInputInteractionResult(tools.vitruv.change.interaction.UserInteractionOptions.WindowModality windowModality, String title,
                String message, String positiveDecisionText, String cancelDecisionText,
                tools.vitruv.change.interaction.UserInteractionOptions.InputValidator inputValidator) {
            return delegate.getTextInputInteractionResult(windowModality, title, message, positiveDecisionText, cancelDecisionText, inputValidator);
        }

        @Override
        public int getMultipleChoiceSingleSelectionInteractionResult(tools.vitruv.change.interaction.UserInteractionOptions.WindowModality windowModality,
                String title, String message, String positiveDecisionText, String cancelDecisionText, Iterable<String> choices) {
            return delegate.getMultipleChoiceSingleSelectionInteractionResult(windowModality, title, message, positiveDecisionText, cancelDecisionText, choices);
        }

        @Override
        public Iterable<Integer> getMultipleChoiceMultipleSelectionInteractionResult(
                tools.vitruv.change.interaction.UserInteractionOptions.WindowModality windowModality, String title, String message,
                String positiveDecisionText, String cancelDecisionText, Iterable<String> choices) {
            return delegate.getMultipleChoiceMultipleSelectionInteractionResult(windowModality, title, message, positiveDecisionText, cancelDecisionText,
                    choices);
        }
    }

    /** Copies src/test/resources/initialstate into the folder and replaces the path placeholder; returns the folder. */
    static Path copyInitialState(Path folder) {
        try {
            Path target = folder.toRealPath();
            URL url = InitialStateLoader.class.getResource(RESOURCE_FOLDER);
            if (url == null) {
                throw new IllegalStateException("initial state not found on the class path: " + RESOURCE_FOLDER);
            }
            Path source = Paths.get(url.toURI());
            try (Stream<Path> files = Files.walk(source)) {
                for (Path file : (Iterable<Path>) files::iterator) {
                    Path copy = target.resolve(source.relativize(file).toString());
                    if (Files.isDirectory(file)) {
                        Files.createDirectories(copy);
                    } else {
                        Files.copy(file, copy);
                    }
                }
            }
            String folderUri = URI.createFileURI(target.toString()).toString();
            for (String metadata : List.of("vsum/models.models", "vsum/uuid.uuid")) {
                Path file = target.resolve(metadata);
                String content = Files.readString(file, StandardCharsets.UTF_8);
                Files.writeString(file, content.replace(PLACEHOLDER, folderUri), StandardCharsets.UTF_8);
            }
            return target;
        } catch (IOException | URISyntaxException e) {
            throw new IllegalStateException("Could not copy the initial state", e);
        }
    }

    /** First element (pre-order) below the given one whose name, id or instanceName equals the name. */
    public static EObject findByName(EObject container, String name) {
        for (var it = container.eAllContents(); it.hasNext();) {
            EObject candidate = it.next();
            if (name.equals(nameOf(candidate))) {
                return candidate;
            }
        }
        return null;
    }

    static String nameOf(EObject object) {
        for (String attribute : List.of("name", "id", "instanceName")) {
            EStructuralFeature feature = object.eClass().getEStructuralFeature(attribute);
            if (feature != null && object.eGet(feature) instanceof String value) {
                return value;
            }
        }
        return null;
    }
}
