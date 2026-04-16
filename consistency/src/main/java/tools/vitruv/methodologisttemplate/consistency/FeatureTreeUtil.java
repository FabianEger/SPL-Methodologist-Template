package tools.vitruv.methodologisttemplate.consistency;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

import UVLPackage.Feature;
import UVLPackage.FeatureTree;
import UVLPackage.Group;
import UVLPackage.UVLModel;
import edu.kit.ipd.sdq.commons.util.org.eclipse.emf.common.util.URIUtil;
import tools.vitruv.dsls.reactions.runtime.helper.PersistenceHelper;

public class FeatureTreeUtil {
    

    public static boolean isFeatureAddedtoThisDomain(Feature feature, EObject parent, String domainRootName) {

        if(isRootFeature(parent) || parent instanceof FeatureTree){
            System.out.println("Parent is root feature, so feature is added directly under root and not under domain root");
            return false;
        }

        Feature domainRootFeature = getDomainRootFeature(feature, domainRootName);
        
        if (domainRootFeature == null) {
            System.out.println("Domain root feature not found for feature " + feature.getName() + " and domain root name " + domainRootName);
            return false;
        }
        System.out.println("Domain root feature: " + domainRootFeature.getName());

        return EcoreUtil.isAncestor(domainRootFeature,feature);
    }



    public static Feature getDomainRootFeature(Feature feature, String domainRootName) {
       
        if (isRootFeature(feature.eContainer().eContainer()) && feature.getName().equals(domainRootName)) {
            return feature;
        } else if (isRootFeature(feature.eContainer().eContainer()) && !feature.getName().equals(domainRootName)) {
            return null;
        } else {
            return checkIFParentIsRootFeature((Feature) feature.eContainer().eContainer(), feature, domainRootName);
        }
    } 

    


    private static Feature checkIFParentIsRootFeature(Feature feature, Feature child, String domainRootName) {
        System.out.println("Checking feature " + feature.getName() + " for being added under domain root " + domainRootName);
        if (isRootFeature(feature.eContainer().eContainer()) && feature.getName().equals(domainRootName)) {
            return feature;
        } else if (isRootFeature(feature) && !feature.getName().equals(domainRootName)) {
            return null;
        } else {
            return checkIFParentIsRootFeature((Feature) feature.eContainer().eContainer(), feature, domainRootName);
        }
    }


    public static boolean isRootFeature(EObject feature) {
         if (feature == null) {
            return false;
        }
        if (feature instanceof FeatureTree) {
            return false;
        }
        return feature.eContainer().eContainer() instanceof FeatureTree;
    }

    public static boolean isRootFeatureWithoutDomainRoot(Feature rootfeature, String domainRootName) {
        Predicate<? super Group> isDomainRoot = e -> e.getFeature().get(0).getName().equals(domainRootName);
        return rootfeature.getChildren().stream().noneMatch(isDomainRoot);
    }


    public static Feature getRootFeatureOfDomain(Feature rootFeature, String domainRootName) {
        Predicate<? super Group> isDomainRoot = e -> e.getFeature().get(0).getName().equals(domainRootName);
        return rootFeature.getChildren().stream().filter(isDomainRoot).findFirst().get().getFeature().get(0);
    }


    public static boolean checkExistenceOfUVLModel(EObject alreadyPersistedEObject) {
        URI uvlModelUri = PersistenceHelper.getURIFromSourceProjectFolder(alreadyPersistedEObject, "/brake.uvl");
        System.out.println("UVLModel exists: " + URIUtil.existsResourceAtUri(uvlModelUri));
        return URIUtil.existsResourceAtUri(uvlModelUri);
    }
    
    public static boolean checkExistenceOfDomainRoot(EObject alreadyPersistedEObject, String domainRootName) {
        URI uvlModelUri = PersistenceHelper.getURIFromSourceProjectFolder(alreadyPersistedEObject, "/brake.uvl");
        if (!URIUtil.existsResourceAtUri(uvlModelUri)) {
            return false;
        }else { 
            Resource resource = alreadyPersistedEObject.eResource().getResourceSet().getResource(uvlModelUri, true);
            return resource.getContents().stream().filter(e -> e instanceof Feature).filter(f -> { 
            Feature feature = (Feature) f;
            return feature.getName().equals(domainRootName);}
        ).findFirst().orElse(null) != null;
        }
    }

    public static Feature getDomainRootFeatureFromResource(EObject alreadyPersistedEObject, String domainRootName) {
        URI uvlModelUri = PersistenceHelper.getURIFromSourceProjectFolder(alreadyPersistedEObject, "/brake.uvl");
        Resource resource = alreadyPersistedEObject.eResource().getResourceSet().getResource(uvlModelUri, true);
        return (Feature) resource.getContents().stream().filter(e -> e instanceof Feature).filter(f -> { 
            Feature feature = (Feature) f;
            return feature.getName().equals(domainRootName);}
        ).findFirst().orElse(null);
    }

}
