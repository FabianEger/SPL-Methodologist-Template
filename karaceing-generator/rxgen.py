"""Generate the KA-RaceIng reactions (and the Java list of change propagation specifications).

* fcpr/      Feature CPRs per stakeholder role: RootFeatureTo<ROLE>, FeatureTo<ROLE> and CombinedUVLTo<ROLE>.
             These are copies of the existing pattern (UVL2CAD/RootFeatureToCAD + FeatureToCAD +
             CombinedUVLToCAD); only the domain-root name, the metamodel, the file name and the
             class-specific create routines differ.
             FeatureRemovalTo<MM>: handling of removed features (per metamodel).
* reverse/   <MM>ToFeature: new / removed artefacts without a feature (solution space -> problem space).
* overlap/   Overlap<Src>To<Tgt>: propagation along 'overlap' correspondences between domains and
             cleanup of 'requirement' trace links.
* config/    FeatureEvolutionToConfig (uvl -> config): removed features are removed from the configurations.
"""
import os, re, sys, csv, itertools
from collections import OrderedDict
from pyecore.resources import ResourceSet, URI

TEMPLATE = sys.argv[1] if len(sys.argv) > 1 else os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ECORE = f'{TEMPLATE}/model/src/main/ecore'
RX = f'{TEMPLATE}/consistency/src/main/reactions/tools/vitruv/methodologisttemplate/consistency/karaceing'
INIT = f'{TEMPLATE}/vsum/src/test/resources/initialstate'
JAVA_LIST = f'{TEMPLATE}/vsum/src/main/java/tools/vitruv/methodologisttemplate/vsum/karaceing/KARaceIngChangePropagationSpecifications.java'
BASE = 'tools.vitruv.methodologisttemplate.model'

rs = ResourceSet()
ECO = {}
for f in ['requirements', 'cad', 'electrical', 'simuLink', 'thermal', 'testbench', 'bom', 'brakesystem', 'UVL', 'configurationsMM']:
    p = rs.get_resource(URI(f'{ECORE}/{f}.ecore')).contents[0]
    rs.metamodel_registry[p.nsURI] = p
    ECO[f] = p

# metamodel descriptors -----------------------------------------------------------------------
# containers: container class -> designated containment references used for new elements
MMS = OrderedDict([
    ('requirements', dict(eco='requirements', alias='req', ns='http://vitruv.tools/methodologisttemplate/requirements',
                          pkg=f'{BASE}.requirements', prefix='Requirements', root='RequirementsSpecification', short='Requirements',
                          containers=OrderedDict([('RequirementsSpecification', ['sections']), ('Section', ['sections', 'requirements'])]),
                          inserted=[('Section', 'RequirementsSpecification', 'sections'), ('Section', 'Section', 'sections'),
                                    ('Requirement', 'Section', 'requirements')],
                          artefact='Section', deleted=['Section', 'Requirement'])),
    ('cad', dict(eco='cad', alias='cad', ns='http://demonstrator.kit.edu/cad', pkg=f'{BASE}.cad', prefix='Cad', root='CADDocument', short='Cad',
                 containers=OrderedDict([('CADDocument', ['objects']), ('Assembly', ['children'])]),
                 inserted=[('DocumentObject', 'CADDocument', 'objects'), ('DocumentObject', 'Assembly', 'children')],
                 artefact='DocumentObject', deleted=['DocumentObject'])),
    ('electrical', dict(eco='electrical', alias='elec', ns='http://vitruv.tools/methodologisttemplate/electrical', pkg=f'{BASE}.electrical',
                        prefix='Electrical', root='ElectricalDesign', short='Electrical',
                        containers=OrderedDict([('ElectricalDesign', ['elements']), ('Circuit', ['elements']), ('Device', ['pins'])]),
                        inserted=[('ElectricalElement', 'ElectricalDesign', 'elements'), ('ElectricalElement', 'Circuit', 'elements')],
                        artefact='ElectricalElement', deleted=['ElectricalElement'])),
    ('simulink', dict(eco='simuLink', alias='sim', ns='http://www.example.org/simulink', pkg=f'{BASE}.simulink', prefix='SimuLink',
                      root='SimulinkModel', short='Simulink',
                      containers=OrderedDict([('SimulinkModel', ['contains']), ('SubSystem', ['subBlocks'])]),
                      inserted=[('Block', 'SimulinkModel', 'contains'), ('Block', 'SubSystem', 'subBlocks')],
                      artefact='Block', deleted=['Block'])),
    ('thermal', dict(eco='thermal', alias='thermo', ns='http://vitruv.tools/methodologisttemplate/thermal', pkg=f'{BASE}.thermal',
                     prefix='Thermal', root='ThermalAnalysisModel', short='Thermal',
                     containers=OrderedDict([('ThermalAnalysisModel', ['elements']), ('ThermalSystem', ['elements'])]),
                     inserted=[('ThermalElement', 'ThermalAnalysisModel', 'elements'), ('ThermalElement', 'ThermalSystem', 'elements')],
                     artefact='ThermalElement', deleted=['ThermalElement'])),
    ('testbench', dict(eco='testbench', alias='tb', ns='http://vitruv.tools/methodologisttemplate/testbench', pkg=f'{BASE}.testbench',
                       prefix='Testbench', root='TestBenchConfiguration', short='Testbench',
                       containers=OrderedDict([('TestBenchConfiguration', ['elements', 'instruments']), ('TestSuite', ['elements']),
                                               ('TestCase', ['steps'])]),
                       inserted=[('TestElement', 'TestBenchConfiguration', 'elements'), ('TestElement', 'TestSuite', 'elements')],
                       artefact='TestElement', deleted=['TestElement'])),
    ('bom', dict(eco='bom', alias='bom', ns='http://example.org/bom', pkg=f'{BASE}.bom', prefix='Bom', root='BOM', short='Bom',
                 containers=OrderedDict(), inserted=[('Item', 'BOM', 'items')], artefact='Item', deleted=['Item'])),
    ('brakesystem', dict(eco='brakesystem', alias='brake', ns='http://www.example.org/brakesystem', pkg='brakesystem', prefix='Brakesystem',
                         root='Brakesystem', short='Brakesystem', containers=OrderedDict(),
                         inserted=[('BrakeComponent', 'Brakesystem', 'BrakeComponents')], artefact='BrakeComponent', deleted=['BrakeComponent'])),
])
FILE_MM = {'requirements': 'requirements', 'cad': 'cad', 'electrical': 'electrical', 'simulink': 'simulink', 'thermal': 'thermal',
           'testbench': 'testbench', 'bom': 'bom', 'brakesystem': 'brakesystem'}

# stakeholder roles: code, domain root feature, metamodel, file, model label, root name attribute
ROLES = [
    ('PRJ', 'ProjectAndRequirements', 'requirements', 'prj.requirements', 'PRJ requirements specification'),
    ('DES', 'DesignEngineering', 'cad', 'des.cad', 'DES CAD model'),
    ('CRASH', 'CrashStructure', 'cad', 'crash.cad', 'CRASH CAD model'),
    ('KIN', 'Kinematics', 'cad', 'kin.cad', 'KIN CAD model'),
    ('AERO', 'Aerodynamics', 'cad', 'aero.cad', 'AERO CAD model'),
    ('THERMO', 'Thermodynamics', 'thermal', 'thermo.thermal', 'THERMO thermal analysis model'),
    ('ELEC', 'ElectricalEngineering', 'electrical', 'elec.electrical', 'ELEC electrical design model'),
    ('SIM', 'Simulation', 'simulink', 'sim.simulink', 'SIM Simulink model'),
    ('VAL', 'Validation', 'testbench', 'val.testbench', 'VAL test-bench configuration'),
]

UVL_IMPORT = 'import "http://vitruv.tools/methodologisttemplate/uvl" as uvl'
CONFIG_IMPORT = 'import "http://vitruv.tools/methodologisttemplate/config" as config'
SEGMENTS = []          # (segment name, registered in the default list?)


def cap(s):
    return s[0].upper() + s[1:]


def cls(mm, name):
    return ECO[MMS[mm]['eco']].getEClassifier(name)


def concrete_subclasses(mm, ecls):
    out = []
    for c in ECO[MMS[mm]['eco']].eClassifiers:
        if c.eClass.name == 'EClass' and not c.abstract and (c is ecls or ecls in c.eAllSuperTypes()):
            out.append(c)
    return out


def contained(mm, container, refs):
    """[(class name, reference name)] for the designated references, in metamodel order."""
    c = cls(mm, container)
    out = []
    for r in c.eAllReferences():
        if r.containment and r.name in refs:
            # same rule as EMFUtil.findContainedClasses, which builds the choices of the dialog: an abstract
            # (or interface) type stands for its concrete subclasses, a concrete type only for itself
            kinds = concrete_subclasses(mm, r.eType) if (r.eType.abstract or r.eType.interface) else [r.eType]
            for k in kinds:
                out.append((k.name, r.name))
    return out


def expand_notifications(text):
    """showNotifications(<expr>) -> a loop that shows every message of <expr> as a notification dialog."""
    out = []
    for line in text.split('\n'):
        m = re.match(r'^(\s*)showNotifications\((.*)\)\s*$', line)
        if not m:
            out.append(line); continue
        ind, expr = m.groups()
        out += [f'{ind}for (message : {expr}) {{',
                f'{ind}    userInteractor.notificationDialogBuilder.message(message).title(KARaceIngOverlapRules.TITLE).windowModality(WindowModality.MODELESS).startInteraction',
                f'{ind}}}']
    return '\n'.join(out)


def write(path, text):
    text = expand_notifications(text)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    open(path, 'w').write(text.lstrip('\n'))


# ============================================================================== FCPR per role
ROOT_TMPL = '''
import java.io.File;
import tools.vitruv.methodologisttemplate.consistency.FeatureTreeUtil;
import UVLPackage.Feature;

@@UVL@@
import "@@NS@@" as @@A@@

// Root Feature CPR of the stakeholder role @@ROLE@@ (copy of UVL2CAD/RootFeatureToCAD.reactions):
// when the domain root feature @@DR@@ is inserted below the root feature, the @@LABEL@@ is created.

reactions: rootFeatureTo@@ROLE@@
in reaction to changes in uvl
execute actions in @@A@@

reaction UVLDomainRootInserted{
    after element uvl::Feature inserted in uvl::Group[feature]
    with FeatureTreeUtil.isRootFeature(newValue.eContainer().eContainer()) && newValue.name == "@@DR@@"
    call create@@ROOT@@ForDomainRoot(newValue)
}


routine create@@ROOT@@ForDomainRoot(uvl::Feature feature) {
    match{
        require absence of @@A@@::@@ROOT@@ corresponding to feature
    }
    create{
        val model = new @@A@@::@@ROOT@@
    }
    update{
        System.out.println("Adding @@LABEL@@ for domain root feature " + feature.name)
        model.name = "@@ROLE@@"
        persistProjectRelative(feature, model, new File("").toString() + "@@FILE@@")
        addCorrespondenceBetween(feature, model)
    }
}
'''

FEATURE_HEAD = '''
import java.io.File;
import org.eclipse.emf.ecore.util.EcoreUtil;
import tools.vitruv.methodologisttemplate.consistency.FeatureTreeUtil;
import tools.vitruv.methodologisttemplate.consistency.EMFUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngOverlapRules;
import tools.vitruv.change.interaction.UserInteractionOptions.WindowModality
import java.util.Arrays
import UVLPackage.Feature;
import @@PKG@@.@@PREFIX@@Package;
@@CLASSIMPORTS@@

@@UVL@@
import "@@NS@@" as @@A@@

// Feature CPR of the stakeholder role @@ROLE@@ (copy of the pattern in UVL2CAD/FeatureToCAD.reactions):
// a feature added below the domain root @@DR@@ is mapped to an existing element of the @@LABEL@@
// or a new element is created below the element that corresponds to the parent feature.

reactions: featureTo@@ROLE@@
in reaction to changes in uvl
execute actions in @@A@@


// A Feature is added under the domain root
reaction UVLFeatureInsertedIntoDomainRoot{
    after element uvl::Feature inserted in uvl::Group[feature]
    with FeatureTreeUtil.isFeatureAddedtoThisDomain(newValue, newValue.eContainer().eContainer(),"@@DR@@")
    call decisionRoutineForAddedFeature(newValue)
}


// Decide at which level the feature was added by checking what corresponds to the parent feature
routine decisionRoutineForAddedFeature(uvl::Feature feature){
    match{
        val correspondingElements = retrieve many EObject corresponding to feature.eContainer().eContainer()
        val correspondingElementsToFeature = retrieve many EObject corresponding to feature
    }
    update{

        val correspondingElementsToFeatureOfMM = EMFUtil.filterByMetamodel(correspondingElementsToFeature, @@PREFIX@@Package.eINSTANCE)
        if(!correspondingElementsToFeatureOfMM.isEmpty()){
            System.out.println("Feature " + feature.name + " already has corresponding element(s) in the @@LABEL@@: " + correspondingElementsToFeatureOfMM)
            return
        }


        val mapToExistingComponent = userInteractor.singleSelectionDialogBuilder.message("Do you want to map the new feature " + feature.name + " to an existing element of the @@LABEL@@ or to a new one?").
                                    choices(Arrays.asList("Existing","New")).windowModality(WindowModality.MODAL).startInteraction


        val containedClasses = EMFUtil.filterByMetamodel(correspondingElements, @@PREFIX@@Package.eINSTANCE)
        System.out.println("Contained classes: " + containedClasses)

        if(containedClasses.isEmpty()){
            System.out.println("No corresponding EClass for parent feature " + feature.name + "found")
            return
        }

        var correspondingClass = containedClasses.get(0)
        if(containedClasses.size() > 1){
            // Every artefact corresponds to a feature, so the parent feature can be realised by several elements (its
            // main element and the details inside it): use the outermost one that can contain elements; ask if ambiguous.
            System.out.println("Multiple corresponding EClasses for parent feature " + feature.name + " found: " + containedClasses)
            correspondingClass = KARaceIngUtil.outermost(containedClasses)
            if(correspondingClass === null){
                val index = userInteractor.singleSelectionDialogBuilder.message("The parent feature of " + feature.name + " is realised by several elements of the @@LABEL@@. Below which one should the new feature be realised?").
                            choices(EMFUtil.getAllNamesOfEObjects(containedClasses)).windowModality(WindowModality.MODAL).startInteraction
                correspondingClass = containedClasses.get(index)
            }
        }

        System.out.println("Corresponding class for parent feature " + feature.name + ": " + correspondingClass)

@@BRANCHES@@
    }
}
'''

MAP_TMPL = '''

routine mapToExistingIn@@C@@(uvl::Feature feature, @@A@@::@@C@@ container) {
    update{

        val existingComponents = EMFUtil.getAllContainedEObjects(container)

        System.out.println("Existing components: " + existingComponents)

        if(existingComponents.isEmpty()){
            System.out.println("No existing elements to map to. Please create a new element.")
            createElementIn@@C@@(feature, container)
            return
        }

        val componentNames = EMFUtil.getAllNamesOfEObjects(existingComponents)

        val selectedComponentIndex = userInteractor.singleSelectionDialogBuilder.message("To which existing element of the @@LABEL@@ do you want to map the new feature " + feature.name + "?").
                                    choices(componentNames).windowModality(WindowModality.MODAL).startInteraction

        if(selectedComponentIndex < 0 || selectedComponentIndex >= existingComponents.size()){
            System.out.println("Invalid selection. Please try again.")
            return
        }

        val selectedComponent = existingComponents.get(selectedComponentIndex)
        addCorrespondenceBetween(feature, selectedComponent)
    }
}



routine createElementIn@@C@@(uvl::Feature feature, @@A@@::@@C@@ container) {
    match{
        require absence of @@A@@::@@ELEMBASE@@ corresponding to feature
    }
    update{
        val containedClasses = EMFUtil.findContainedClasses(container.eClass())
        System.out.println("Contained classes: " + containedClasses)


        val componentType = userInteractor.singleSelectionDialogBuilder.message(
            "What element of the @@LABEL@@ do you want to add below " + EMFUtil.getNameOf(container) + " to be correspondent to the feature " + feature.name + "?").choices(
            EMFUtil.getAllNamesOfEClass(containedClasses)).windowModality(WindowModality.MODAL).
            startInteraction

        System.out.println("Selected element type index: " + componentType)

        switch (EMFUtil.getAllNamesOfEClass(containedClasses).get(componentType)) {
@@CASES@@
            default:
                return // do nothing
        }
    }
}
'''

CREATE_TMPL = '''
routine create@@K@@In@@C@@(uvl::Feature feature, @@A@@::@@C@@ container) {
    create {
        val newElement = new @@A@@::@@K@@
    }
    update {
        System.out.println("Adding @@K@@ for feature " + feature.name + " to the @@LABEL@@")
        newElement.name = feature.name
        container.@@REF@@.add(newElement);
        addCorrespondenceBetween(feature, newElement);
    }
}
'''

COMBINED_TMPL = '''

@@UVL@@
import "@@NS@@" as @@A@@

// Vitruvius allows only one change propagation specification per pair of metamodels (uvl -> @@MM@@), so the
// Feature CPRs of all roles whose artefacts are @@MM@@ models (@@ROLES@@) and the feature removal are combined
// here, like CombinedUVLToCAD combines feature2cad and featureRoot2cad.

reactions: @@SEG@@
in reaction to changes in uvl
execute actions in @@A@@


@@IMPORTS@@
'''


def sub(t, **kw):
    for k, v in kw.items():
        t = t.replace(f'@@{k}@@', v)
    assert '@@' not in t, t[t.index('@@') - 50: t.index('@@') + 50]
    return t


def element_base(mm, container):
    """Common supertype used in 'require absence of' for elements created in this container."""
    c = cls(mm, container)
    refs = MMS[mm]['containers'][container]
    for r in c.eAllReferences():
        if r.containment and r.name == refs[0]:
            return r.eType.name


def gen_role(code, dr, mm, file, label):
    d = MMS[mm]
    common = dict(UVL=UVL_IMPORT, NS=d['ns'], A=d['alias'], ROLE=code, DR=dr, LABEL=label, ROOT=d['root'], FILE=file,
                  PKG=d['pkg'], PREFIX=d['prefix'])
    write(f'{RX}/fcpr/{code}/RootFeatureTo{code}.reactions', sub(ROOT_TMPL, **{k: common[k] for k in ['UVL', 'NS', 'A', 'ROLE', 'DR', 'LABEL', 'ROOT', 'FILE']}))
    containers = list(d['containers'].keys())
    imports = '\n'.join(f'import {d["pkg"]}.{c};' for c in containers)
    branches, body = [], ''
    first = True
    for c in containers:
        kw = 'if' if first else 'else if'
        first = False
        branches.append(f'''        {kw} (correspondingClass instanceof {c} && mapToExistingComponent == 1) {{
            System.out.println("Creating new element below the {c} for feature " + feature.name)
            createElementIn{c}(feature, correspondingClass as {c})
        }}
        else if (correspondingClass instanceof {c} && mapToExistingComponent == 0) {{
            mapToExistingIn{c}(feature, correspondingClass as {c})
        }}''')
        source = f'FeatureTreeUtil.getDomainRootFeature(feature,"{dr}")' if c == d['root'] else 'feature.eContainer().eContainer()'
        cases = ''.join(f'            case "{k}":\n                create{k}In{c}(feature, container)\n' for k, r in contained(mm, c, d['containers'][c]))
        body += sub(MAP_TMPL, C=c, A=d['alias'], SOURCE=source, LABEL=label, ELEMBASE=element_base(mm, c), CASES=cases.rstrip('\n'))
        for k, r in contained(mm, c, d['containers'][c]):
            body += sub(CREATE_TMPL, K=k, C=c, A=d['alias'], LABEL=label, REF=r)
    branches.append(f'''        else {{
            System.out.println("Corresponding element for parent feature of " + feature.name + " cannot contain new elements: " + correspondingClass)
            return
        }}''')
    head = sub(FEATURE_HEAD, CLASSIMPORTS=imports, BRANCHES='\n'.join(branches), **{k: common[k] for k in ['UVL', 'NS', 'A', 'ROLE', 'DR', 'LABEL', 'PKG', 'PREFIX']})
    write(f'{RX}/fcpr/{code}/FeatureTo{code}.reactions', head + body)
    SEGMENTS.append(('featureTo' + code, False))
    SEGMENTS.append(('rootFeatureTo' + code, False))


# ============================================================================== feature removal
REMOVAL_TMPL = '''
import tools.vitruv.methodologisttemplate.consistency.EMFUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngOverlapRules;
import tools.vitruv.change.interaction.UserInteractionOptions.WindowModality
import java.util.Arrays
import UVLPackage.Feature;
import @@PKG@@.@@PREFIX@@Package;

@@UVL@@
import "@@NS@@" as @@A@@

// Feature CPR for removed features (@@MM@@ models): the artefacts that realise a removed feature (and the
// features below it) are deleted or kept and mapped to the parent feature; an artefact that still realises
// another feature keeps that one. There is no artefact without a feature.
// A feature is only treated as removed if it is not contained anywhere any more (a move is no removal).

reactions: featureRemovalTo@@SHORT@@
in reaction to changes in uvl
execute actions in @@A@@

reaction UVLFeatureRemoved{
    after element uvl::Feature removed from uvl::Group[feature]
    with oldValue.eContainer() === null
    call handleRemovedFeatureSubtree(oldValue, affectedEObject)
}


routine handleRemovedFeatureSubtree(uvl::Feature feature, uvl::Group group) {
    update{
        for (removedFeature : KARaceIngUtil.featureAndDescendants(feature)) {
            handleArtefactsOfRemovedFeature(removedFeature, group)
        }
    }
}


routine handleArtefactsOfRemovedFeature(uvl::Feature feature, uvl::Group group) {
    match{
        val realisations = retrieve many EObject corresponding to feature
    }
    update{
        val artefacts = EMFUtil.filterByMetamodel(realisations, @@PREFIX@@Package.eINSTANCE)
        for (artefact : artefacts) {
            removeCorrespondenceBetween(feature, artefact)
            if (KARaceIngUtil.isAlive(artefact)) {
                decideOnRealisationOfRemovedFeature(feature, artefact, group)
            }
        }
    }
}


// Every artefact corresponds to a feature: an artefact that still realises another feature keeps it; otherwise it
// is deleted or mapped to the parent feature (there is no "keep artefact without feature").
routine decideOnRealisationOfRemovedFeature(uvl::Feature feature, EObject artefact, uvl::Group group) {
    match{
        val otherFeatures = retrieve many uvl::Feature corresponding to artefact
    }
    update{
        if (!KARaceIngUtil.alive(otherFeatures).isEmpty()) {
            System.out.println(KARaceIngUtil.nameOf(artefact) + " still corresponds to " + KARaceIngUtil.featureNames(KARaceIngUtil.alive(otherFeatures)))
            return
        }
        val parentFeature = if (group.eContainer() instanceof Feature) group.eContainer() as Feature else null
        if (parentFeature === null) {
            System.out.println("Deleting " + KARaceIngUtil.nameOf(artefact) + " realising the removed feature " + feature.name + " (no parent feature)")
            removeObject(artefact)
            return
        }
        val choice = userInteractor.singleSelectionDialogBuilder.message("[FCPR] The feature " + feature.name + " was removed. What should happen with its realisation " + KARaceIngUtil.nameOf(artefact) + " in the " + KARaceIngUtil.domainOf(artefact) + " model?").
                        choices(Arrays.asList("Delete artefact", "Keep artefact and map it to " + parentFeature.name)).windowModality(WindowModality.MODAL).startInteraction
        if (choice == 0) {
            System.out.println("Deleting " + KARaceIngUtil.nameOf(artefact) + " realising the removed feature " + feature.name)
            removeObject(artefact)
        } else {
            System.out.println("Mapping " + KARaceIngUtil.nameOf(artefact) + " to the parent feature " + parentFeature.name)
            addCorrespondenceBetween(parentFeature, artefact)
        }
    }
}
'''


def gen_removal(mm):
    d = MMS[mm]
    write(f'{RX}/fcpr/FeatureRemovalTo{d["short"]}.reactions',
          sub(REMOVAL_TMPL, UVL=UVL_IMPORT, NS=d['ns'], A=d['alias'], MM=mm, SHORT=d['short'], PKG=d['pkg'], PREFIX=d['prefix']))
    SEGMENTS.append(('featureRemovalTo' + d['short'], False))  # registered via featureCPRsTo<MM>


def gen_combined(mm):
    d = MMS[mm]
    roles = [r[0] for r in ROLES if r[2] == mm]
    seg = 'featureCPRsTo' + d['short']
    imports = ''.join(f'import featureTo{r} using qualified names\n\nimport rootFeatureTo{r} using qualified names\n\n' for r in roles)
    imports += f'import featureRemovalTo{d["short"]} using qualified names\n'
    write(f'{RX}/fcpr/FeatureCPRsTo{d["short"]}.reactions',
          sub(COMBINED_TMPL, UVL=UVL_IMPORT, NS=d['ns'], A=d['alias'], MM=mm, ROLES=', '.join(roles), SEG=seg, IMPORTS=imports))
    SEGMENTS.append((seg, True))


# ============================================================================== reverse (artefact -> feature)
REVERSE_HEAD = '''
import tools.vitruv.methodologisttemplate.consistency.EMFUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngOverlapRules;
import tools.vitruv.change.interaction.UserInteractionOptions.WindowModality
import java.util.Arrays
import UVLPackage.Feature;
import @@PKG@@.@@PREFIX@@Package;

@@UVL@@
import "@@NS@@" as @@A@@

// Feature CPRs from the solution space (@@MM@@ models) to the problem space. Rule: every artefact corresponds
// to a feature, there is no artefact without a feature.
// * a new artefact is mapped to the feature of its containing element, to the features of an element it
//   overlaps with, to another existing feature, or it is represented by a new feature below the feature of
//   its containing element;
// * a removed artefact loses its feature links; a feature without any remaining realisation in this
//   domain is removed or kept.

reactions: @@SEG@@
in reaction to changes in @@A@@
execute actions in uvl

@@REACTIONS@@

routine decisionRoutineForAddedArtefact(EObject artefact) {
    match{
        require absence of uvl::Feature corresponding to artefact
    }
    update{
        proposeFeatureForArtefact(artefact, artefact.eContainer())
    }
}


// Every artefact must correspond to a feature (there is no "keep without feature"). The proposal starts at the
// feature of the containing element; if the container has no feature yet (e.g. it is new as well), the next
// container up is used.
routine proposeFeatureForArtefact(EObject artefact, EObject holder) {
    match{
        require absence of uvl::Feature corresponding to artefact
        val holderFeatures = retrieve many uvl::Feature corresponding to holder
        val overlapping = retrieve many EObject corresponding to artefact tagged "overlap"
    }
    update{
        val features = KARaceIngUtil.alive(holderFeatures)
        if (features.isEmpty()) {
            if (holder.eContainer() !== null) {
                proposeFeatureForArtefact(artefact, holder.eContainer())
            } else {
                System.out.println("No feature found above " + KARaceIngUtil.nameOf(artefact))
            }
            return
        }
        val containing = features.get(0)
        val partners = KARaceIngUtil.alive(overlapping)
        val choices = new java.util.ArrayList<String>
        choices.add("Feature of the containing element")
        for (p : partners) {
            choices.add("Features of the overlapping element " + KARaceIngUtil.nameOf(p) + " (" + KARaceIngUtil.domainOf(p) + ")")
        }
        choices.add("Existing feature")
        choices.add("New feature")
        val overlapText = if (partners.isEmpty()) "" else " It overlaps with " + KARaceIngUtil.nameList(partners) + "."
        val choice = userInteractor.singleSelectionDialogBuilder.message("[FCPR] The new " + artefact.eClass().name + " " + KARaceIngUtil.nameOf(artefact) + " in the " + KARaceIngUtil.domainOf(artefact) + " model has no feature." + overlapText + " Every artefact must correspond to a feature: map it to " + containing.name + " (the feature of its containing element " + KARaceIngUtil.nameOf(holder) + "), to the features of an overlapping element, to another existing feature, or to a new feature below " + containing.name + "?").
                        choices(choices).windowModality(WindowModality.MODAL).startInteraction
        if (choice == 0) {
            System.out.println("Mapping " + KARaceIngUtil.nameOf(artefact) + " to the feature of its containing element " + containing.name)
            linkArtefactToFeature(containing, artefact)
        } else if (choice <= partners.size()) {
            mapArtefactToFeaturesOf(artefact, partners.get(choice - 1), containing)
        } else if (choice == partners.size() + 1) {
            mapArtefactToExistingFeature(artefact, containing)
        } else {
            val groupKind = userInteractor.singleSelectionDialogBuilder.message("[FCPR] In which kind of group below " + containing.name + " should the new feature " + KARaceIngUtil.nameOf(artefact) + " be added?").
                        choices(Arrays.asList("Optional", "Mandatory", "Alternative", "Or")).windowModality(WindowModality.MODAL).startInteraction
            switch (groupKind) {
                case 0: createOptionalFeatureForArtefact(artefact, containing)
                case 1: createMandatoryFeatureForArtefact(artefact, containing)
                case 2: createAlternativeFeatureForArtefact(artefact, containing)
                case 3: createOrFeatureForArtefact(artefact, containing)
                default: createOptionalFeatureForArtefact(artefact, containing)
            }
        }
    }
}


routine mapArtefactToFeaturesOf(EObject artefact, EObject overlappingElement, uvl::Feature fallback) {
    match{
        val partnerFeatures = retrieve many uvl::Feature corresponding to overlappingElement
    }
    update{
        val features = KARaceIngUtil.alive(partnerFeatures)
        if (features.isEmpty()) {
            System.out.println(KARaceIngUtil.nameOf(overlappingElement) + " has no feature; mapping " + KARaceIngUtil.nameOf(artefact) + " to " + fallback.name)
            linkArtefactToFeature(fallback, artefact)
            return
        }
        for (f : features) {
            System.out.println("Mapping " + KARaceIngUtil.nameOf(artefact) + " to " + f.name + " (feature of the overlapping " + KARaceIngUtil.nameOf(overlappingElement) + ")")
            linkArtefactToFeature(f, artefact)
        }
    }
}


routine mapArtefactToExistingFeature(EObject artefact, uvl::Feature parentFeature) {
    update{
        // any feature can be chosen (an artefact may realise a feature of another role, e.g. an electrical BOTS
        // device realises des.BrakeOverTravelSwitch); the features of the artefact's own role are offered first
        val candidates = KARaceIngUtil.domainFirst(parentFeature)
        val index = if (candidates.size() <= 1) candidates.size() - 1 else userInteractor.singleSelectionDialogBuilder.message("[FCPR] To which existing feature should " + KARaceIngUtil.nameOf(artefact) + " (" + KARaceIngUtil.domainOf(artefact) + ") be mapped?").
                        choices(KARaceIngUtil.featureNames(candidates)).windowModality(WindowModality.MODAL).startInteraction
        if (index < 0 || index >= candidates.size()) {
            System.out.println("No feature selected: mapping " + KARaceIngUtil.nameOf(artefact) + " to " + parentFeature.name)
            linkArtefactToFeature(parentFeature, artefact)
            return
        }
        linkArtefactToFeature(candidates.get(index), artefact)
    }
}


// Maps the artefact to the feature. Requirement traces: a requirement traces to every artefact of the features it
// is mapped to, so a new artefact gets the traces of the feature's requirements, and a new requirement traces to
// all artefacts of the feature (their owners are informed).
routine linkArtefactToFeature(uvl::Feature feature, EObject artefact) {
    match{
        val realisations = retrieve many EObject corresponding to feature
    }
    update{
        addCorrespondenceBetween(feature, artefact)
        val others = KARaceIngUtil.alive(realisations)
        for (other : others) {
            if (other !== artefact) {
                if (KARaceIngUtil.isRequirement(artefact) && KARaceIngUtil.isTraceableArtefact(other)) {
                    addCorrespondenceBetween(artefact, other, "requirement")
                } else if (KARaceIngUtil.isRequirement(other) && KARaceIngUtil.isTraceableArtefact(artefact)) {
                    addCorrespondenceBetween(other, artefact, "requirement")
                }
            }
        }
        if (KARaceIngUtil.isRequirement(artefact)) {
            for (message : KARaceIngOverlapRules.newRequirementTraces(artefact, feature.name, others)) {
                userInteractor.notificationDialogBuilder.message(message).title(KARaceIngOverlapRules.TITLE).windowModality(WindowModality.MODELESS).startInteraction
            }
        }
    }
}

@@CREATES@@

routine handleRemovedArtefact(EObject artefact) {
    match{
        val features = retrieve many uvl::Feature corresponding to artefact
    }
    update{
        for (feature : features) {
            removeCorrespondenceBetween(artefact, feature)
            if (KARaceIngUtil.isAlive(feature)) {
                checkFeatureStillRealised(feature)
            }
        }
    }
}


routine checkFeatureStillRealised(uvl::Feature feature) {
    match{
        val realisations = retrieve many EObject corresponding to feature
    }
    update{
        val remaining = KARaceIngUtil.alive(EMFUtil.filterByMetamodel(realisations, @@PREFIX@@Package.eINSTANCE))
        if (!remaining.isEmpty()) {
            System.out.println("Feature " + feature.name + " is still realised by " + KARaceIngUtil.nameList(remaining))
            return
        }
        val choice = userInteractor.singleSelectionDialogBuilder.message("[FCPR] The feature " + feature.name + " has no realisation in the @@MM@@ model any more. Remove the feature or keep it?").
                        choices(Arrays.asList("Remove feature", "Keep feature")).windowModality(WindowModality.MODAL).startInteraction
        if (choice == 0) {
            System.out.println("Removing feature " + feature.name)
            removeObject(feature)
        }
    }
}
'''

REVERSE_CREATE = '''
routine create@@G@@FeatureForArtefact(EObject artefact, uvl::Feature parentFeature) {
    create{
        val feature = new uvl::Feature
        val group = new uvl::@@G@@
    }
    update{
        System.out.println("Adding feature " + KARaceIngUtil.nameOf(artefact) + " below " + parentFeature.name)
        feature.name = KARaceIngUtil.nameOf(artefact)
        group.feature.add(feature)
        feature.group = group
        parentFeature.children.add(group)
        addCorrespondenceBetween(feature, artefact)
    }
}
'''


def gen_reverse(mm):
    d = MMS[mm]
    seg = f'{mm}ToFeatureModel'
    rx = ''
    for el, cont, ref in d['inserted']:      # every artefact needs a feature (Sections and Requirements alike)
        rx += f'''
reaction ArtefactInsertedIn{cont}{cap(ref)}{{
    after element {d["alias"]}::{el} inserted in {d["alias"]}::{cont}[{ref}]
    call decisionRoutineForAddedArtefact(newValue)
}}
'''
    for el in d['deleted']:
        rx += f'''
reaction {el}Deleted{{
    after element {d["alias"]}::{el} deleted
    call handleRemovedArtefact(affectedEObject)
}}
'''
    creates = ''.join(sub(REVERSE_CREATE, G=g, A=d['alias'], ART=d['artefact']) for g in ['Optional', 'Mandatory', 'Alternative', 'Or'])
    write(f'{RX}/reverse/{cap(mm)}ToFeatureModel.reactions',
          sub(REVERSE_HEAD, UVL=UVL_IMPORT, NS=d['ns'], A=d['alias'], MM=mm, SEG=seg, PKG=d['pkg'], PREFIX=d['prefix'], ART=d['artefact'],
              REACTIONS=rx, CREATES=creates))
    SEGMENTS.append((seg, True))


# ============================================================================== overlap propagation
OVERLAP_TMPL = '''
import tools.vitruv.methodologisttemplate.consistency.EMFUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngOverlapRules;
import tools.vitruv.change.interaction.UserInteractionOptions.WindowModality
import java.util.Arrays
import @@TPKG@@.@@TPREFIX@@Package;

@@IMPORTS@@

// Consistency between semantically overlapping artefacts (@@SRC@@ -> @@TGT@@), based on the correspondences
// tagged "overlap" in the VSUM:
// * a new element below an artefact that overlaps with @@TGT@@ artefacts gets a counterpart there (created or
//   linked), or the overlap is ignored;
// * when an artefact with overlapping @@TGT@@ artefacts is removed, the counterparts are deleted or kept;
// * trace links (tag "requirement") of removed elements are removed; removing an element traced to requirements
//   reports the violated requirements (rule AR10);
// * attribute and structure changes of an artefact (or of an element below it) are propagated to the overlapping
//   @@TGT@@ artefacts by the rules in KARaceIngOverlapRules (AR1-AR13): derived values are updated, everything that
//   cannot be resolved automatically is notified to the owner of the counterpart.

reactions: @@SEG@@
in reaction to changes in @@SA@@
execute actions in @@TA@@

@@REACTIONS@@

routine propagateNewElement(EObject changedElement) {
    match{
        val ownOverlaps = retrieve many EObject corresponding to changedElement tagged "overlap"
        val parentOverlaps = retrieve many EObject corresponding to changedElement.eContainer() tagged "overlap"
    }
    update{
        if (!ownOverlaps.isEmpty()) {
            return // counterpart already exists (e.g. the element itself was created as a counterpart)
        }
        val counterparts = KARaceIngUtil.alive(EMFUtil.filterByMetamodel(parentOverlaps, @@TPREFIX@@Package.eINSTANCE))
        for (overlapping : counterparts) {
            val choice = userInteractor.singleSelectionDialogBuilder.message("[Overlap] " + KARaceIngUtil.nameOf(changedElement) + " was added to " + KARaceIngUtil.nameOf(changedElement.eContainer()) + " (" + KARaceIngUtil.domainOf(changedElement) + "), which overlaps with " + KARaceIngUtil.nameOf(overlapping) + " (" + KARaceIngUtil.domainOf(overlapping) + "). Create a counterpart, link an existing element, or ignore?").
                            choices(Arrays.asList("Create counterpart", "Link existing element", "Ignore")).windowModality(WindowModality.MODAL).startInteraction
            if (choice == 0) {
                val container = KARaceIngUtil.counterpartContainer(overlapping)
                val classes = KARaceIngUtil.creatableClasses(container)
                if (!classes.isEmpty()) {
                    // a selection dialog needs at least two choices; with one possible kind it is used directly
                    val index = if (classes.size() == 1) 0 else userInteractor.singleSelectionDialogBuilder.message("[Overlap] Which kind of element should the counterpart of " + KARaceIngUtil.nameOf(changedElement) + " below " + KARaceIngUtil.nameOf(container) + " (" + KARaceIngUtil.domainOf(container) + ") be?").
                                    choices(KARaceIngUtil.classNames(classes)).windowModality(WindowModality.MODAL).startInteraction
                    if (index >= 0 && index < classes.size()) {
                        val counterpart = KARaceIngUtil.createChild(container, classes.get(index), KARaceIngUtil.nameOf(changedElement))
                        System.out.println("Created counterpart " + KARaceIngUtil.nameOf(counterpart) + " in " + KARaceIngUtil.domainOf(counterpart))
                        addCorrespondenceBetween(changedElement, counterpart, "overlap")
                        showNotifications(KARaceIngOverlapRules.counterpartCreated(changedElement, counterpart, overlapping))
                    }
                }
            } else if (choice == 1) {
                val candidates = KARaceIngUtil.namedContents(overlapping)
                // a selection dialog needs at least two choices; a single candidate is linked directly
                val index = if (candidates.size() <= 1) candidates.size() - 1 else userInteractor.singleSelectionDialogBuilder.message("[Overlap] Which element of " + KARaceIngUtil.nameOf(overlapping) + " (" + KARaceIngUtil.domainOf(overlapping) + ") corresponds to " + KARaceIngUtil.nameOf(changedElement) + "?").
                                choices(KARaceIngUtil.nameList(candidates)).windowModality(WindowModality.MODAL).startInteraction
                if (index >= 0 && index < candidates.size()) {
                    addCorrespondenceBetween(changedElement, candidates.get(index), "overlap")
                }
            }
        }
    }
}


routine propagateRemovedElement(EObject changedElement) {
    match{
        val overlaps = retrieve many EObject corresponding to changedElement tagged "overlap"
        val traces = retrieve many EObject corresponding to changedElement tagged "requirement"
    }
    update{
        val tracedHere = EMFUtil.filterByMetamodel(traces, @@TPREFIX@@Package.eINSTANCE)
        for (requirement : tracedHere) {
            recordTracesOfRequirement(requirement)
        }
        showNotifications(KARaceIngOverlapRules.removedTracedArtefact(changedElement, tracedHere))
        for (trace : tracedHere) {
            removeCorrespondenceBetween(changedElement, trace, "requirement")
        }
        for (overlapping : EMFUtil.filterByMetamodel(overlaps, @@TPREFIX@@Package.eINSTANCE)) {
            removeCorrespondenceBetween(changedElement, overlapping, "overlap")
            if (KARaceIngUtil.isAlive(overlapping)) {
                val choice = userInteractor.singleSelectionDialogBuilder.message("[Overlap] " + KARaceIngUtil.nameOf(changedElement) + " was removed. It overlaps with " + KARaceIngUtil.nameOf(overlapping) + " (" + KARaceIngUtil.domainOf(overlapping) + "). Delete the counterpart or keep it?").
                                choices(Arrays.asList("Delete counterpart", "Keep counterpart")).windowModality(WindowModality.MODAL).startInteraction
                if (choice == 0) {
                    System.out.println("Deleting counterpart " + KARaceIngUtil.nameOf(overlapping))
                    KARaceIngOverlapRules.counterpartRemoved(overlapping)
                    removeObject(overlapping)
                }
            }
        }
    }
}


// ---- attribute and structure level (KARaceIngOverlapRules) ----------------------------------------------

routine recordTracesOfRequirement(EObject requirement) {
    match{
        val traced = retrieve many EObject corresponding to requirement tagged "requirement"
    }
    update{
        KARaceIngOverlapRules.recordTraces(requirement, traced)
    }
}


routine propagateStructureChange(EObject container, EObject child, String how) {
    update{
        KARaceIngOverlapRules.rememberContainer(child, container, how)
        if (how == "added") {
            propagateValueChange(container, "children", null, KARaceIngUtil.nameOf(child))
        } else {
            propagateValueChange(container, "children", KARaceIngUtil.nameOf(child), null)
        }
    }
}


routine propagateValueChange(EObject changed, String feature, String oldValue, String newValue) {
    update{
        propagateValueChangeFrom(changed, changed, feature, oldValue, newValue)
    }
}


// The anchor is the changed element or the nearest container with overlaps (or requirement traces) in the
// @@TGT@@ model.
routine propagateValueChangeFrom(EObject changed, EObject anchor, String feature, String oldValue, String newValue) {
    match{
        val overlaps = retrieve many EObject corresponding to anchor tagged "overlap"
        val traces = retrieve many EObject corresponding to anchor tagged "requirement"
    }
    update{
        val counterparts = KARaceIngUtil.alive(EMFUtil.filterByMetamodel(overlaps, @@TPREFIX@@Package.eINSTANCE))
        counterparts.addAll(KARaceIngUtil.alive(EMFUtil.filterByMetamodel(traces, @@TPREFIX@@Package.eINSTANCE)))
        if (counterparts.isEmpty()) {
            if (anchor.eContainer() !== null) {
                propagateValueChangeFrom(changed, anchor.eContainer(), feature, oldValue, newValue)
            }
            return
        }
        showNotifications(KARaceIngOverlapRules.propagate(changed, anchor, feature, oldValue, newValue, counterparts, overlaps, traces))
    }
}
'''


def gen_overlap(src, tgt):
    s, t = MMS[src], MMS[tgt]
    seg = f'overlap{s["short"]}To{t["short"]}'
    imports = f'import "{s["ns"]}" as {s["alias"]}'
    ta = t['alias']
    if src == tgt:
        ta = s['alias']
    else:
        imports += f'\nimport "{t["ns"]}" as {t["alias"]}'
    rx = ''
    for el, cont, ref in s['inserted']:
        rx += f'''
reaction NewElementIn{cont}{cap(ref)}{{
    after element {s["alias"]}::{el} inserted in {s["alias"]}::{cont}[{ref}]
    call propagateNewElement(newValue)
}}
'''
    for el in s['deleted']:
        rx += f'''
reaction {el}Removed{{
    after element {s["alias"]}::{el} deleted
    call propagateRemovedElement(affectedEObject)
}}
'''
    a = s['alias']
    for el, cont, ref in s['inserted'] + STRUCTURE_EXTRA.get(src, []):
        rx += f'''
reaction StructureAddedIn{cont}{cap(ref)}{{
    after element {a}::{el} inserted in {a}::{cont}[{ref}]
    call propagateStructureChange(affectedEObject, newValue, "added")
}}

reaction StructureRemovedFrom{cont}{cap(ref)}{{
    after element {a}::{el} removed from {a}::{cont}[{ref}]
    call propagateStructureChange(affectedEObject, oldValue, "removed")
}}
'''
    for trig in VALUE_TRIGGERS.get(src, []):
        c, f, k = (trig + ('attr',))[:3]
        name = f'{c}{cap(f)}Changed'
        if k == 'attr':
            rx += f'''
reaction {name}{{
    after attribute replaced at {a}::{c}[{f}]
    call propagateValueChange(affectedEObject, "{f}", String.valueOf(oldValue), String.valueOf(newValue))
}}
'''
        elif k == 'ref':
            rx += f'''
reaction {name}{{
    after element replaced at {a}::{c}[{f}]
    call propagateValueChange(affectedEObject, "{f}", KARaceIngUtil.nameOf(oldValue), KARaceIngUtil.nameOf(newValue))
}}
'''
        else:
            rx += f'''
reaction {name}Inserted{{
    after attribute inserted in {a}::{c}[{f}]
    call propagateValueChange(affectedEObject, "{f}", null, String.valueOf(newValue))
}}

reaction {name}Removed{{
    after attribute removed from {a}::{c}[{f}]
    call propagateValueChange(affectedEObject, "{f}", String.valueOf(oldValue), null)
}}
'''
    write(f'{RX}/overlap/Overlap{s["short"]}To{t["short"]}.reactions',
          sub(OVERLAP_TMPL, TPKG=t['pkg'], TPREFIX=t['prefix'], IMPORTS=imports, SRC=src, TGT=tgt, SEG=seg, SA=s['alias'], TA=ta, REACTIONS=rx))
    SEGMENTS.append((seg, True))


# attribute / reference changes propagated by the overlap rules (KARaceIngOverlapRules): (class, feature[, kind])
# kind: attr (single-valued attribute, default), ref (single-valued non-containment reference), many (multi-valued attribute)
VALUE_TRIGGERS = {
    'cad': [('DocumentObject', 'name'), ('DocumentObject', 'x'), ('DocumentObject', 'y'), ('DocumentObject', 'z'), ('Box', 'length'),
            ('Box', 'width'), ('Box', 'height'), ('Cylinder', 'radius'), ('Cylinder', 'height'), ('Sphere', 'radius'), ('Cone', 'radius1'),
            ('Cone', 'radius2'), ('Cone', 'height'), ('BooleanOperation', 'operation'), ('Solid', 'material', 'ref')],
    'requirements': [('Requirement', 'text'), ('Requirement', 'allowedValues', 'many')],
    'electrical': [('ElectricalElement', 'name'), ('ElectricalElement', 'partNumber'), ('Device', 'mountingPosition'), ('Device', 'kind'),
                   ('Pin', 'signal')],
    'simulink': [('Parameter', 'value')],
    'thermal': [('ThermalComponent', 'material'), ('ThermalComponent', 'supplier'), ('ThermalComponent', 'temperatureLimit'),
                ('ThermalComponent', 'thermalConductivity'), ('ThermalComponent', 'specificHeatCapacity'), ('HeatSource', 'maxTemperature'),
                ('HeatSource', 'heatLoad'), ('AnalysisCase', 'passed')],
    'testbench': [('TestCase', 'passCriterion'), ('TestStep', 'expectedResult')],
    'bom': [('StringParameterValue', 'value'), ('DoubleParameterValue', 'value'), ('Item', 'numberOfItems')],
    'brakesystem': [('BrakeDisk', 'ventilated'), ('BrakeDisk', 'brakeDiskThicknessInMM'), ('BrakeDisk', 'minimumThicknessInMM'),
                    ('BrakeDisk', 'diameterInMM'), ('BrakeComponent', 'specificationType'), ('BrakeComponent', 'OEM_number'),
                    ('BrakeCaliper', 'pistonDiameterInMM'), ('BrakePad', 'thicknessInMM'), ('BrakePad', 'wearWarning')],
}
# containments whose changes are structure changes in addition to the 'inserted' list of the metamodel
STRUCTURE_EXTRA = {
    'electrical': [('Pin', 'Device', 'pins')],
    'brakesystem': [('BrakePad', 'BrakeCaliper', 'brakePads')],
}


# ============================================================================== configuration + constraints
CONFIG_EVOLUTION = '''
import tools.vitruv.methodologisttemplate.consistency.karaceing.KARaceIngUtil;

@@UVL@@
@@CONFIG@@

// ConfigCPR for removed features (complementing ConfigCPRs.reactions / feature2config, which adds every new
// feature as an unselected feature to every configuration): a removed feature, and the features below it,
// are removed from all configurations. Whether a configuration is still a valid product is not checked
// here (to be done with VitruvOCL).

reactions: featureEvolutionToConfig
in reaction to changes in uvl
execute actions in config

reaction UVLFeatureRemoved{
    after element uvl::Feature removed from uvl::Group[feature]
    with oldValue.eContainer() === null
    call removeConfigFeaturesOfSubtree(oldValue)
}


routine removeConfigFeaturesOfSubtree(uvl::Feature feature) {
    update{
        for (removedFeature : KARaceIngUtil.featureAndDescendants(feature)) {
            removeConfigFeatures(removedFeature)
        }
    }
}


routine removeConfigFeatures(uvl::Feature feature) {
    match{
        val configFeatures = retrieve many config::Feature corresponding to feature
    }
    update{
        for (configFeature : configFeatures) {
            System.out.println("Removing feature " + feature.name + " from a configuration")
            removeCorrespondenceBetween(feature, configFeature)
            removeObject(configFeature)
        }
    }
}
'''


CONFIG_COMBINED = '''
@@UVL@@
@@CONFIG@@

// Vitruvius allows only one change propagation specification per pair of metamodels (uvl -> config), so the
// ConfigCPRs of the template (feature2config) and the removal of features from the configurations
// (featureEvolutionToConfig) are combined here.

reactions: configCPRsKARaceIng
in reaction to changes in uvl
execute actions in config


import feature2config using qualified names

import featureEvolutionToConfig using qualified names
'''


def gen_config():
    write(f'{RX}/config/FeatureEvolutionToConfig.reactions', sub(CONFIG_EVOLUTION, UVL=UVL_IMPORT, CONFIG=CONFIG_IMPORT))
    write(f'{RX}/config/ConfigCPRsKARaceIng.reactions', sub(CONFIG_COMBINED, UVL=UVL_IMPORT, CONFIG=CONFIG_IMPORT))
    SEGMENTS.append(('featureEvolutionToConfig', False))
    SEGMENTS.append(('configCPRsKARaceIng', True))


# ============================================================================== pairs from the initial state
def overlap_pairs():
    ext = {'requirements': 'requirements', 'cad': 'cad', 'electrical': 'electrical', 'simulink': 'simulink', 'thermal': 'thermal',
           'testbench': 'testbench', 'bom': 'bom', 'brakesystem': 'brakesystem'}
    import xml.etree.ElementTree as ET
    pairs = set()
    for c in ET.parse(f'{INIT}/vsum/correspondences.correspondence').getroot():
        if c.get('tag') not in ('overlap', 'requirement'):
            continue
        a, b = [side.get('href').split('#')[0].split('.')[-1] for side in c]
        ma, mb = ext[a], ext[b]
        pairs.add((ma, mb)); pairs.add((mb, ma))
    return sorted(pairs, key=lambda p: (list(MMS).index(p[0]), list(MMS).index(p[1])))


JAVA_TMPL = '''package tools.vitruv.methodologisttemplate.vsum.karaceing;

import java.util.List;

import tools.vitruv.change.propagation.ChangePropagationSpecification;

/**
 * All change propagation specifications of the KA-RaceIng case study (generated by rxgen.py together with
 * the reactions in consistency/src/main/reactions/.../consistency/karaceing).
 */
public final class KARaceIngChangePropagationSpecifications {

    private KARaceIngChangePropagationSpecifications() {
    }

    /**
     * The complete set used by the scenario tests: Feature CPRs per stakeholder role (combined root +
     * feature reactions), feature removal, solution space to problem space, overlap propagation between the
     * domains and ConfigCPRs (feature2config from the template plus the removal of features from the
     * configurations).
     */
    public static List<ChangePropagationSpecification> all() {
        return List.of(
@@ITEMS@@
        );
    }
}
'''


def gen_java():
    items = []
    for seg, registered in SEGMENTS:
        if registered:
            items.append(f'            new mir.reactions.{seg}.{cap(seg)}ChangePropagationSpecification()')
    items.append('            new mir.reactions.uvl2uvl.Uvl2uvlChangePropagationSpecification()')
    os.makedirs(os.path.dirname(JAVA_LIST), exist_ok=True)
    open(JAVA_LIST, 'w').write(sub(JAVA_TMPL, ITEMS=',\n'.join(items)))


def check_unique_pairs():
    """Vitruvius accepts only one CPS per (source, target) metamodel pair, and segment names must not differ
    only in case (the generated Java packages would collide on case-insensitive file systems)."""
    import glob, re
    pairs, names = {}, {}
    for f in glob.glob(f'{TEMPLATE}/consistency/src/main/reactions/**/*.reactions', recursive=True):
        t = open(f).read()
        seg = re.search(r'reactions:\s*(\w+)', t).group(1)
        assert seg.lower() not in names, f'segment names differ only in case: {seg} / {names[seg.lower()]}'
        names[seg.lower()] = seg
        al = dict((a, ns) for ns, a in re.findall(r'import "([^"]+)" as (\w+)', t))
        m = re.search(r'in reaction to changes in (\w+)\s+execute actions in (\w+)', t)
        pairs[seg] = (al[m.group(1)], al[m.group(2)])
    seen = {}
    for seg, registered in SEGMENTS + [('uvl2uvl', True)]:
        if registered:
            assert pairs[seg] not in seen, f'{seg} and {seen[pairs[seg]]} both propagate {pairs[seg]}'
            seen[pairs[seg]] = seg


if __name__ == '__main__':
    import shutil
    shutil.rmtree(RX, ignore_errors=True)
    for r in ROLES:
        gen_role(*r)
    for mm in ['requirements', 'cad', 'electrical', 'simulink', 'thermal', 'testbench']:
        gen_removal(mm)
        gen_combined(mm)
        gen_reverse(mm)
    for mm in ['bom', 'brakesystem']:          # their artefacts need a feature as well
        gen_reverse(mm)
    for s, t in overlap_pairs():
        gen_overlap(s, t)
    gen_config()
    gen_java()
    check_unique_pairs()
    print(len(SEGMENTS), 'segments,', sum(1 for _, r in SEGMENTS if r), 'registered')
    print('overlap pairs', overlap_pairs())
