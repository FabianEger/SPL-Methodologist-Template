package tools.vitruv.methodologisttemplate.consistency;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import UVLPackage.Feature;
import UVLPackage.FeatureTree;

public class FeatureTreeUtil {
    

    public static boolean isFeatureAddedtoThisDomain(Feature feature, EObject parent) {
        return !isRootFeature(parent) && EcoreUtil.isAncestor(getDomainRootFeature(feature),feature);
    }



    public static Feature getDomainRootFeature(Feature feature) {
       
        if (isRootFeature(feature)) {
            return feature;
        } else {
            return checkIFParentIsRootFeature((Feature) feature.eContainer().eContainer(), feature);
        }
       
    } 

    


    private static Feature checkIFParentIsRootFeature(Feature feature, Feature child) {
        if (isRootFeature(feature)) {
            return child;
        } else {
            return checkIFParentIsRootFeature((Feature) feature.eContainer().eContainer(), feature);
        }
    }


    public static boolean isRootFeature(EObject feature) {
        if (feature instanceof FeatureTree) {
            return false;
        }
        return feature.eContainer().eContainer() instanceof FeatureTree;
    }

}
