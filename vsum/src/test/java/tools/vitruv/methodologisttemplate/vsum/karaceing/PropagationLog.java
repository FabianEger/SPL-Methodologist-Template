package tools.vitruv.methodologisttemplate.vsum.karaceing;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.emf.ecore.resource.Resource;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

import tools.vitruv.change.composite.description.PropagatedChange;
import tools.vitruv.change.composite.description.VitruviusChange;
import tools.vitruv.framework.vsum.internal.InternalVirtualModel;
import tools.vitruv.methodologisttemplate.consistency.EMFUtil;

/**
 * Serialises one change propagation of the VSUM as JSON (only used when
 * {@link KARaceIngTestBase#SNAPSHOT_PROPERTY} is set): for every propagated change the atomic changes of the
 * original change (made in the view, or by the previous propagation step) and of the consequential changes
 * (made by the reactions), and the dialogs answered on the way. Changes and dialogs are EMF objects and are
 * written generically through their structural features. Correspondences are not part of the atomic
 * changes; the correspondences added and removed by a propagation are found by comparing the persisted
 * correspondence model before and after it (Vitruvius saves it at the end of every propagation).
 */
final class PropagationLog {

    private PropagationLog() {
    }

    static String step(Iterable<PropagatedChange> changes, Map<String, String> before, Map<String, String> after) {
        return step(changes, before, after, List.of());
    }

    /** As above, with the notifications (role-addressed messages of the overlap rules) shown during the propagation. */
    static String step(Iterable<PropagatedChange> changes, Map<String, String> before, Map<String, String> after, List<String> notifications) {
        List<String> added = new ArrayList<>();
        List<String> removed = new ArrayList<>();
        if (before != null && after != null) {
            after.forEach((k, v) -> { if (!before.containsKey(k)) added.add(v); });
            before.forEach((k, v) -> { if (!after.containsKey(k)) removed.add(v); });
        }
        return "{\"hops\":" + hops(changes) + ",\"corrAdded\":[" + String.join(",", added) + "],\"corrRemoved\":[" + String.join(",", removed) + "],\"notifications\":["
                + notifications.stream().map(PropagationLog::str).collect(java.util.stream.Collectors.joining(",")) + "]}";
    }

    /**
     * The persisted correspondences of the VSUM, resolved against its models in memory:
     * identity key (both objects and tag) -> {"tag":..., "left":..., "right":...}.
     */
    static Map<String, String> correspondences(InternalVirtualModel vsum) {
        Map<String, String> out = new LinkedHashMap<>();
        try {
            Path file = vsum.getFolder().resolve("vsum").resolve("correspondences.correspondence");
            if (!file.toFile().exists()) {
                return out;
            }
            Map<String, Resource> resources = new LinkedHashMap<>();
            for (Resource r : vsum.getViewSourceModels()) {
                resources.put(r.getURI().lastSegment(), r);
            }
            NodeList list = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.toFile()).getDocumentElement().getElementsByTagName("correspondences");
            for (int i = 0; i < list.getLength(); i++) {
                Element c = (Element) list.item(i);
                String tag = c.getAttribute("tag");
                Object left = resolve(((Element) c.getElementsByTagName("leftEObjects").item(0)).getAttribute("href"), resources);
                Object right = resolve(((Element) c.getElementsByTagName("rightEObjects").item(0)).getAttribute("href"), resources);
                String key = identity(left) + "|" + identity(right) + "|" + tag;
                out.put(key, "{\"tag\":" + str(tag) + ",\"left\":" + value(left) + ",\"right\":" + value(right) + "}");
            }
        } catch (Exception e) {
            out.put("error", "{\"error\":" + str(String.valueOf(e)) + "}");
        }
        return out;
    }

    /** Model elements are compared by object identity, unresolved references and literals by their text. */
    private static String identity(Object o) {
        return o instanceof EObject ? "@" + System.identityHashCode(o) : "#" + o;
    }

    private static Object resolve(String href, Map<String, Resource> resources) {
        if (href.startsWith("http")) {
            return href.substring(href.indexOf('#') + 3);          // uvlPackage.Literals.UVL_MODEL
        }
        String file = href.substring(href.lastIndexOf('/', href.indexOf('#')) + 1, href.indexOf('#'));
        Resource r = resources.get(file);
        EObject o = r == null ? null : r.getEObject(href.substring(href.indexOf('#') + 1));
        return o != null ? o : href;
    }

    private static String hops(Iterable<PropagatedChange> changes) {
        List<String> parts = new ArrayList<>();
        try {
            for (PropagatedChange change : changes) {
                parts.add("{\"original\":" + changes(change.getOriginalChange()) + ",\"consequential\":" + changes(change.getConsequentialChanges())
                        + ",\"interactions\":" + interactions(change) + "}");
            }
        } catch (RuntimeException e) {
            parts.add("{\"error\":" + str(String.valueOf(e)) + "}");
        }
        return "[" + String.join(",", parts) + "]";
    }

    private static String changes(VitruviusChange<EObject> change) {
        List<String> out = new ArrayList<>();
        if (change != null) {
            for (Object c : change.getEChanges()) {
                out.add(object((EObject) c));
            }
        }
        return "[" + String.join(",", out) + "]";
    }

    private static String interactions(PropagatedChange change) {
        List<String> out = new ArrayList<>();
        for (VitruviusChange<EObject> c : List.of(change.getOriginalChange(), change.getConsequentialChanges())) {
            if (c == null) {
                continue;
            }
            for (Object i : c.getUserInteractions()) {
                out.add(object((EObject) i));
            }
        }
        return "[" + String.join(",", out) + "]";
    }

    /** {"type": EClass name, feature: value, ...} for all set features of an atomic change or interaction. */
    private static String object(EObject o) {
        List<String> fields = new ArrayList<>();
        fields.add("\"type\":" + str(o.eClass().getName()));
        for (EStructuralFeature f : o.eClass().getEAllStructuralFeatures()) {
            Object v = o.eGet(f);
            if (v == null || (v instanceof Collection<?> col && col.isEmpty())) {
                continue;
            }
            fields.add(str(f.getName()) + ":" + value(v));
        }
        return "{" + String.join(",", fields) + "}";
    }

    private static String value(Object v) {
        if (v instanceof Collection<?> col) {
            List<String> out = new ArrayList<>();
            col.forEach(x -> out.add(value(x)));
            return "[" + String.join(",", out) + "]";
        }
        if (v instanceof EStructuralFeature f) {
            return str(f.getName());
        }
        if (v instanceof EClass c) {
            return str(c.getName());
        }
        if (v instanceof EObject e) {
            String file = e.eResource() == null ? null : e.eResource().getURI().lastSegment();
            return "{\"name\":" + str(EMFUtil.getNameOf(e)) + ",\"cls\":" + str(e.eClass().getName()) + (file == null ? "" : ",\"file\":" + str(file)) + "}";
        }
        if (v instanceof Number || v instanceof Boolean) {
            return str(String.valueOf(v));
        }
        return str(String.valueOf(v));
    }

    private static String str(String s) {
        if (s == null) {
            return "null";
        }
        StringBuilder b = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }

}
