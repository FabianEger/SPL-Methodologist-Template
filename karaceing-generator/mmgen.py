"""Generate / patch the KARaceIng metamodels (.ecore) and their genmodels.

New metamodels are described compactly below and written as .ecore files.
The provided CAD metamodel is patched (EClass Object -> DocumentObject) with ElementTree so that
annotations and everything else stay untouched. All genmodels are (re)generated
from the final .ecore files so genClasses / genFeatures always match.
"""
import os, sys, xml.etree.ElementTree as ET
import os as _os
HERE = _os.path.dirname(_os.path.abspath(__file__))
TEMPLATE_DIR = _os.path.dirname(HERE)

from xml.sax.saxutils import quoteattr

ECORE_DIR = sys.argv[1] if len(sys.argv) > 1 else os.path.join(TEMPLATE_DIR, 'model', 'src', 'main', 'ecore')
XSI = 'http://www.w3.org/2001/XMLSchema-instance'
XMI = 'http://www.omg.org/XMI'
ECO = 'http://www.eclipse.org/emf/2002/Ecore'
ET.register_namespace('xmi', XMI)
ET.register_namespace('xsi', XSI)
ET.register_namespace('ecore', ECO)
EDT = 'ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//'
BASE = 'tools.vitruv.methodologisttemplate.model'


# ----------------------------------------------------------------------------- new metamodels
# class spec: (name, abstract, supertypes, [features], doc)
# feature: ('a', name, type, lower, upper, default)  attribute (type = EString/EDouble/... or #enum)
#          ('r', name, type, lower, upper, containment)
def A(n, t='EString', lo=0, up=1, d=None): return ('a', n, t, lo, up, d)
def R(n, t, lo=0, up=-1, c=True): return ('r', n, t, lo, up, c)


REQUIREMENTS = dict(
    name='requirements', nsURI='http://vitruv.tools/methodologisttemplate/requirements', prefix='Requirements',
    doc='Requirements Specification of the Project Manager / Requirements Engineer (PRJ). Sections mirror the PRJ feature tree; '
        'requirements are the items of the KA-RaceIng survey (Generationsuebergreifende Erhebung).',
    classes=[
        ('RequirementsSpecification', False, [], [A('name'), A('description'), R('sections', 'Section')],
         'Root element: the requirements specification of the product line.'),
        ('Section', False, [], [A('name'), A('description'), R('sections', 'Section'), R('requirements', 'Requirement')],
         'A chapter of the specification, e.g. a rule-set baseline or a variant driver.'),
        ('Requirement', False, [], [A('name'), A('text'), A('kind', '#RequirementKind'), A('subteam'), A('category'), A('unit'),
                                    A('priority'), A('role'), R('carValues', 'CarValue'), A('allowedValues', 'EString', 0, -1)],
         'One survey item (name = item ID such as ANF-548). allowedValues: the values a constrained property of the realising '
         'artefacts may take (e.g. the permitted materials of the brake pedal); checked against BOM and CAD.'),
        ('CarValue', False, [], [A('name'), A('value')], 'The value or applicability of a requirement for one car (name = car, e.g. EV3).'),
    ],
    enums=[('RequirementKind', ['Requirement', 'Goal', 'Function'])])

THERMAL = dict(
    name='thermal', nsURI='http://vitruv.tools/methodologisttemplate/thermal', prefix='Thermal',
    doc='Thermodynamic Analysis Model of the Thermodynamics Engineer (THERMO). Units: W, degC, mm, bar.',
    classes=[
        ('ThermalAnalysisModel', False, [], [A('name'), R('elements', 'ThermalElement')], 'Root element.'),
        ('ThermalElement', True, [], [A('name'), A('description')], 'Anything in the analysis model.'),
        ('ThermalSystem', False, ['ThermalElement'], [R('elements', 'ThermalElement')], 'A (sub-)system or circuit, e.g. the cooling circuit.'),
        ('HeatSource', False, ['ThermalElement'], [A('heatLoad', 'EDouble'), A('maxTemperature', 'EDouble')], 'Component producing heat (engine, motor, exhaust).'),
        ('HeatExchanger', False, ['ThermalElement'], [A('capacity', 'EDouble'), A('medium')], 'Radiator, intercooler, charge-air cooler.'),
        ('FluidLine', False, ['ThermalElement'], [A('medium'), A('innerDiameter', 'EDouble'), A('maxPressure', 'EDouble')], 'Coolant, oil, vent or fuel line.'),
        ('ThermalProtection', False, ['ThermalElement'], [A('material'), A('temperatureResistance', 'EDouble')], 'Heat shield, insulation, cover.'),
        ('ThermalComponent', False, ['ThermalElement'], [A('material'), A('supplier'), A('thermalConductivity', 'EDouble'),
                                                          A('specificHeatCapacity', 'EDouble'), A('temperatureLimit', 'EDouble')],
         'Thermal model of a component based on supplier data (e.g. the brake caliper pistons): material properties in W/(m K) and '
         'J/(kg K), temperature limit in degC.'),
        ('AnalysisCase', False, ['ThermalElement'], [A('method'), A('ambientTemperature', 'EDouble'), A('result'), A('passed', 'EBoolean'),
                                                      R('analysedElements', 'ThermalElement', c=False)], 'A thermal or flow analysis.'),
    ], enums=[])

TESTBENCH = dict(
    name='testbench', nsURI='http://vitruv.tools/methodologisttemplate/testbench', prefix='Testbench',
    doc='Test-bench configuration of the Validation Engineer (VAL).',
    classes=[
        ('TestBenchConfiguration', False, [], [A('name'), R('elements', 'TestElement'), R('instruments', 'Instrument')], 'Root element.'),
        ('TestElement', True, [], [A('name'), A('description')], 'A test suite or a test case.'),
        ('TestSuite', False, ['TestElement'], [R('elements', 'TestElement')], 'Groups test cases, e.g. the technical inspection.'),
        ('TestCase', False, ['TestElement'], [A('procedure'), A('passCriterion'), R('steps', 'TestStep'), R('instruments', 'Instrument', c=False),
                                              A('verifiedRequirements', 'EString', 0, -1)], 'One test.'),
        ('TestStep', False, [], [A('name'), A('description'), A('expectedResult')], 'One step of a test case.'),
        ('Instrument', False, [], [A('name'), A('measuredQuantity'), A('unit'), A('range')], 'Measurement equipment of the test bench.'),
    ], enums=[])

ELECTRICAL = dict(
    name='electrical', nsURI='http://vitruv.tools/methodologisttemplate/electrical', prefix='Electrical',
    doc='Electrical Design Model of the Electrical Engineer (ELEC): circuits, devices, pins, wires and buses.',
    classes=[
        ('ElectricalDesign', False, [], [A('name'), R('elements', 'ElectricalElement')], 'Root element.'),
        ('ElectricalElement', True, [], [A('name'), A('description'), A('partNumber')], 'Anything in the electrical design.'),
        ('Circuit', False, ['ElectricalElement'], [R('elements', 'ElectricalElement'), A('nominalVoltage', 'EDouble'), A('voltageClass', '#VoltageClass')],
         'A circuit or sub-system, e.g. the shutdown circuit.'),
        ('Device', False, ['ElectricalElement'], [A('kind', '#DeviceKind'), A('ratedVoltage', 'EDouble'), A('ratedCurrent', 'EDouble'), R('pins', 'Pin'),
                                                  A('mountingPosition')],
         'An electrical or electronic component. mountingPosition: the CAD element the device is mounted on (e.g. a caliper bracket).'),
        ('Pin', False, [], [A('name'), A('signal')], 'A connector pin of a device.'),
        ('Wire', False, ['ElectricalElement'], [R('source', 'Pin', 0, 1, False), R('target', 'Pin', 0, 1, False), A('crossSection', 'EDouble')],
         'A wire between two pins.'),
        ('Bus', False, ['ElectricalElement'], [A('protocol'), R('nodes', 'Device', c=False)], 'A communication bus (CAN, Ethernet).'),
    ],
    enums=[('VoltageClass', ['LV', 'HV']),
           ('DeviceKind', ['Other', 'Switch', 'Relay', 'Sensor', 'Actuator', 'Fuse', 'Battery', 'ControlUnit', 'Indicator', 'Connector',
                           'Motor', 'Inverter', 'Valve'])])

NEW = [REQUIREMENTS, THERMAL, TESTBENCH, ELECTRICAL]


def doc_ann(doc, ind):
    if not doc:
        return ''
    return (f'{ind}<eAnnotations source="http://www.eclipse.org/emf/2002/GenModel">\n'
            f'{ind}  <details key="documentation" value={quoteattr(doc)}/>\n{ind}</eAnnotations>\n')


def write_new(mm):
    L = ['<?xml version="1.0" encoding="UTF-8"?>',
         f'<ecore:EPackage xmi:version="2.0" xmlns:xmi="{XMI}" xmlns:xsi="{XSI}"',
         f'    xmlns:ecore="{ECO}" name="{mm["name"]}" nsURI="{mm["nsURI"]}" nsPrefix="{mm["name"]}">']
    L.append(doc_ann(mm['doc'], '  ').rstrip('\n'))
    for cname, abstract, sups, feats, doc in mm['classes']:
        attrs = f'name="{cname}"' + (' abstract="true"' if abstract else '')
        if sups:
            attrs += ' eSuperTypes="' + ' '.join('#//' + s for s in sups) + '"'
        L.append(f'  <eClassifiers xsi:type="ecore:EClass" {attrs}>')
        L.append(doc_ann(doc, '    ').rstrip('\n'))
        for f in feats:
            if f[0] == 'a':
                _, n, t, lo, up, d = f
                et = f'#//{t[1:]}' if t.startswith('#') else EDT + t
                s = f'    <eStructuralFeatures xsi:type="ecore:EAttribute" name="{n}"'
                if lo: s += f' lowerBound="{lo}"'
                if up != 1: s += f' upperBound="{up}"'
                s += f' eType="{et}"'
                if d is not None: s += f' defaultValueLiteral="{d}"'
                L.append(s + '/>')
            else:
                _, n, t, lo, up, c = f
                s = f'    <eStructuralFeatures xsi:type="ecore:EReference" name="{n}"'
                if lo: s += f' lowerBound="{lo}"'
                if up != 1: s += f' upperBound="{up}"'
                s += f' eType="#//{t}"'
                if c: s += ' containment="true"'
                L.append(s + '/>')
        L.append('  </eClassifiers>')
    for ename, lits in mm['enums']:
        L.append(f'  <eClassifiers xsi:type="ecore:EEnum" name="{ename}">')
        for i, l in enumerate(lits):
            L.append(f'    <eLiterals name="{l}"' + (f' value="{i}"' if i else '') + '/>')
        L.append('  </eClassifiers>')
    L.append('</ecore:EPackage>')
    open(os.path.join(ECORE_DIR, mm['name'] + '.ecore'), 'w').write('\n'.join(x for x in L if x) + '\n')


# ----------------------------------------------------------------------------- patching helpers
T = f'{{{XSI}}}type'


def load(fn):
    return ET.parse(os.path.join(ECORE_DIR, fn))


def save(tree, fn):
    ET.indent(tree, '  ')
    tree.write(os.path.join(ECORE_DIR, fn), encoding='UTF-8', xml_declaration=True)


def cls(root, name):
    for c in root.findall('eClassifiers'):
        if c.get('name') == name:
            return c
    raise KeyError(name)


def feat(c, name):
    for f in c.findall('eStructuralFeatures'):
        if f.get('name') == name:
            return f
    return None


def add_attr(c, n, t, lo=0, up=1, default=None, doc=None):
    if feat(c, n) is not None:
        return
    e = ET.SubElement(c, 'eStructuralFeatures', {T: 'ecore:EAttribute', 'name': n})
    if lo: e.set('lowerBound', str(lo))
    if up != 1: e.set('upperBound', str(up))
    e.set('eType', f'#//{t[1:]}' if t.startswith('#') else EDT + t)
    if default is not None: e.set('defaultValueLiteral', default)
    if doc: add_doc(e, doc)


def add_ref(c, n, t, lo=0, up=1, containment=False, doc=None):
    if feat(c, n) is not None:
        return
    e = ET.SubElement(c, 'eStructuralFeatures', {T: 'ecore:EReference', 'name': n})
    if lo: e.set('lowerBound', str(lo))
    if up != 1: e.set('upperBound', str(up))
    e.set('eType', '#//' + t)
    if containment: e.set('containment', 'true')
    if doc: add_doc(e, doc)


def add_doc(e, doc):
    a = ET.Element('eAnnotations', {'source': 'http://www.eclipse.org/emf/2002/GenModel'})
    ET.SubElement(a, 'details', {'key': 'documentation', 'value': doc})
    e.insert(0, a)


def add_enum(root, name, lits, doc=None):
    for c in root.findall('eClassifiers'):
        if c.get('name') == name:
            return
    e = ET.SubElement(root, 'eClassifiers', {T: 'ecore:EEnum', 'name': name})
    if doc: add_doc(e, doc)
    for i, l in enumerate(lits):
        d = {'name': l}
        if i: d['value'] = str(i)
        ET.SubElement(e, 'eLiterals', d)


def patch_cad():
    tr = load('cad.ecore'); r = tr.getroot()
    for c in r.iter('eClassifiers'):
        if c.get('name') == 'Object':
            c.set('name', 'DocumentObject')
    for e in r.iter():
        for k in ('eType', 'eSuperTypes'):
            v = e.get(k)
            if v and '#//Object' in v.split():
                e.set(k, ' '.join('#//DocumentObject' if x == '#//Object' else x for x in v.split()))
    save(tr, 'cad.ecore')


# ----------------------------------------------------------------------------- genmodel
def genmodel(ecore_file, gm_file, prefix, base_package, model_name, plugin_id, compliance='17.0'):
    r = ET.parse(os.path.join(ECORE_DIR, ecore_file)).getroot()
    E = ecore_file
    L = ['<?xml version="1.0" encoding="UTF-8"?>',
         '<genmodel:GenModel xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"',
         '    xmlns:genmodel="http://www.eclipse.org/emf/2002/GenModel" modelDirectory="/tools.vitruv.methodologisttemplate.model/target/generated-sources/ecore"',
         f'    creationIcons="false" modelPluginID="{plugin_id}" modelName="{model_name}"',
         '    rootExtendsClass="org.eclipse.emf.ecore.impl.MinimalEObjectImpl$Container" codeFormatting="true"',
         f'    importerID="org.eclipse.emf.importer.ecore" complianceLevel="{compliance}" copyrightFields="false"',
         '    operationReflection="true" importOrganizing="true">',
         f'  <foreignModel>{E}</foreignModel>']
    bp = f' basePackage="{base_package}"' if base_package else ''
    L.append(f'  <genPackages prefix="{prefix}"{bp} disposableProviderFactory="true" ecorePackage="{E}#/">')
    for c in r.findall('eClassifiers'):
        if c.get(T) == 'ecore:EEnum':
            L.append(f'    <genEnums typeSafeEnumCompatible="false" ecoreEnum="{E}#//{c.get("name")}">')
            for l in c.findall('eLiterals'):
                L.append(f'      <genEnumLiterals ecoreEnumLiteral="{E}#//{c.get("name")}/{l.get("name")}"/>')
            L.append('    </genEnums>')
    for c in r.findall('eClassifiers'):
        if c.get(T) != 'ecore:EClass':
            continue
        cn = c.get('name')
        fs = c.findall('eStructuralFeatures')
        img = ' image="false"' if c.get('abstract') == 'true' else ''
        if not fs:
            L.append(f'    <genClasses{img} ecoreClass="{E}#//{cn}"/>')
            continue
        L.append(f'    <genClasses{img} ecoreClass="{E}#//{cn}">')
        for f in fs:
            fn = f.get('name')
            if f.get(T) == 'ecore:EAttribute':
                L.append(f'      <genFeatures createChild="false" ecoreFeature="ecore:EAttribute {E}#//{cn}/{fn}"/>')
            elif f.get('containment') == 'true':
                L.append(f'      <genFeatures property="None" children="true" createChild="true" ecoreFeature="ecore:EReference {E}#//{cn}/{fn}"/>')
            else:
                L.append(f'      <genFeatures notify="false" createChild="false" propertySortChoices="true" ecoreFeature="ecore:EReference {E}#//{cn}/{fn}"/>')
        L.append('    </genClasses>')
    L += ['  </genPackages>', '</genmodel:GenModel>']
    open(os.path.join(ECORE_DIR, gm_file), 'w').write('\n'.join(L) + '\n')


GENMODELS = [
    # ecore, genmodel, prefix, basePackage, modelName, pluginID, compliance
    ('cad.ecore', 'cad.genmodel', 'Cad', BASE, 'Cad', 'tools.vitruv.methodologisttemplate.model', '17.0'),
    ('requirements.ecore', 'requirements.genmodel', 'Requirements', BASE, 'Requirements', 'tools.vitruv.methodologisttemplate.model', '17.0'),
    ('thermal.ecore', 'thermal.genmodel', 'Thermal', BASE, 'Thermal', 'tools.vitruv.methodologisttemplate.model', '17.0'),
    ('testbench.ecore', 'testbench.genmodel', 'Testbench', BASE, 'Testbench', 'tools.vitruv.methodologisttemplate.model', '17.0'),
    ('electrical.ecore', 'electrical.genmodel', 'Electrical', BASE, 'Electrical', 'tools.vitruv.methodologisttemplate.model', '17.0'),
]

if __name__ == '__main__':
    for mm in NEW:
        write_new(mm)
    # UVL.ecore and configurationsMM.ecore are used exactly as provided (not patched)
    patch_cad()
    for g in GENMODELS:
        genmodel(*g)
    print('ok')
