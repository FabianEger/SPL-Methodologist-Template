package tools.vitruv.methodologisttemplate.consistency;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class EMFUtil {
    
    public static List<EClass> findContainedClasses(EClass containerClass) {
        List<EClass> allClasses = collectAllClasses(containerClass.getEPackage());
        return findContainedClasses(containerClass, allClasses);
    }

    /**
     * Core logic: iterates all containment references of the given class.
     * - If the referenced type is concrete → add it directly.
     * - If the referenced type is abstract/interface → resolve all
     *   concrete classes in the metamodel that extend/implement it.
     */
    private static List<EClass> findContainedClasses(EClass containerClass,
                                                      List<EClass> allClasses) {
        List<EClass> result = new ArrayList<>();

        for (EReference ref : containerClass.getEAllReferences()) {
            if (!ref.isContainment()) continue;

            EClass referencedType = ref.getEReferenceType();

            if (referencedType.isAbstract() || referencedType.isInterface()) {
                // Resolve all concrete classes that implement/extend this type
                result.addAll(findConcreteSubclasses(referencedType, allClasses));
            } else {
                result.add(referencedType);
            }
        }

        return result;
    }

    /**
     * Finds all non-abstract, non-interface EClasses in the metamodel
     * that are subtypes of the given abstract class or interface.
     * Uses isSuperTypeOf() to correctly handle multi-level hierarchies.
     */
    private static List<EClass> findConcreteSubclasses(EClass abstractType,
                                                        List<EClass> allClasses) {
        List<EClass> concretes = new ArrayList<>();

        for (EClass candidate : allClasses) {
            if (!candidate.isAbstract()
                    && !candidate.isInterface()
                    && abstractType.isSuperTypeOf(candidate)
                    && !candidate.equals(abstractType)) {
                concretes.add(candidate);
            }
        }

        return concretes;
    }

    /** Recursively collects all EClasses from the generated package hierarchy. */
    private static List<EClass> collectAllClasses(EPackage pkg) {
        List<EClass> classes = new ArrayList<>();

        for (EClassifier classifier : pkg.getEClassifiers()) {
            if (classifier instanceof EClass eClass) {
                classes.add(eClass);
            }
        }

        for (EPackage subPackage : pkg.getESubpackages()) {
            classes.addAll(collectAllClasses(subPackage));
        }

        return classes;
    }

    public static  List<Object> getAllContainEObjects(EObject eObject) {
        List<Object> result = new ArrayList<>();
        TreeIterator<Object> allContents = EcoreUtil.getAllContents(eObject, false);

        for (TreeIterator<Object> it = allContents; it.hasNext();) {
            Object content = it.next();
            if (content instanceof EObject) {
                result.add(content);
            }
            System.out.println("Content no EObject: " + content.toString());
        }

        System.out.println("All contents printed");

        return result;
    }


    /**
     * Filters an Iterable of EObjects, returning only those that are instances
     * of any EClass defined in the given EPackage (metamodel).
     * Also traverses sub-packages recursively.
     *
     * @param instances the Iterable of EObjects to filter
     * @param ePackage  the EPackage representing the metamodel to check against
     * @return a list of EObjects that conform to any EClass in the metamodel
     */
    public static List<EObject> filterByMetamodel(Iterable<EObject> instances, EPackage ePackage) {
        if (instances == null || ePackage == null) {
            return List.of();
        }
        


        List<EClass> allEClasses = collectAllClasses(ePackage);

        return StreamSupport.stream(instances.spliterator(), false)
                .filter(obj -> allEClasses.stream().anyMatch(ec -> ec.isInstance(obj)))
                .collect(Collectors.toList());
    }


    /**
    * Returns all EObjects contained within the given root (excluding the root itself).
    */
    public static List<EObject> getAllContainedEObjects(EObject root) {
        List<EObject> result = new ArrayList<>();
        root.eAllContents().forEachRemaining(result::add);
        return result;
    }

    public static List<String> getAllNamesOfEObjects(List<EObject> eObjects) {
        return eObjects.stream()
                .map(obj -> (String) obj.eGet(obj.eClass().getEStructuralFeature("name")))
                .collect(Collectors.toList());
    }

    public static List<String> getAllNamesOfEClass(List<EClass> eClasses) {
        return eClasses.stream()
                .map(EClass::getName)
                .collect(Collectors.toList());
    }

    public static List<String> getAllNamesOfInstances(List<? extends EObject> eObjects) {
        return eObjects.stream()
                .map(obj -> (String) obj.eGet(obj.eClass().getEStructuralFeature("name")))
                .collect(Collectors.toList());
    }

}
