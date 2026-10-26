"""Generate the initial state of the KA-RaceIng product line as a persisted VSUM.

Inputs : the stakeholder-role feature models (SPL_FeatureModels_v2_Stakeholders/models),
         the requirement mappings (mapping/*.csv) and the three car configurations.
Output : <OUT> is the storage folder of a VSUM, in the same layout that Vitruvius writes itself
         (compare vsumexample/):
           karaceing.uvl, karaceing.config, prj.requirements, des.cad, crash.cad, kin.cad, aero.cad,
           elec.electrical, sim.simulink, thermo.thermal, val.testbench, karaceing.bom, des.brakesystem
           vsum/models.models                   the model URIs of the VSUM
           vsum/uuid.uuid                       UUID of every model element (UUID|hierarchical id)
           vsum/correspondences.correspondence  the correspondence model (ReactionsCorrespondence)
         Vitruvius stores absolute file URIs in models.models and uuid.uuid; they are written with
         the placeholder file:/KARACEING_INITIAL_STATE, which the test replaces by the folder the VSUM
         is copied to. The hrefs in correspondences.correspondence are relative ("../des.cad#//...").

The UVL and configuration metamodels are used exactly as provided: the feature model contains the
feature tree only (typed features carry an Integer attribute); cross-tree constraints cannot be
expressed with the UVL metamodel and are not part of the model (they are only used here to check
that the three configurations are valid). Configurations have no name: EV3, CV2, CV1 are the
configurations 0, 1, 2 of the configuration model.

Correspondence tags: ''            feature <-> artefact (FCPR), uvl <-> config, feature <-> config feature
      'overlap'     artefact <-> semantically overlapping artefact of another domain (or the BOM)
      'requirement' requirement (PRJ spec) <-> artefact realising a feature the requirement is mapped to
"""
import csv, glob, json, os, re, sys
from collections import OrderedDict, defaultdict
from pyecore.resources import ResourceSet, URI
from pyecore.resources.xmi import XMIResource

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import uvlcheck as U

TEMPLATE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
ECORE = os.path.join(TEMPLATE_DIR, 'model', 'src', 'main', 'ecore')
# the stakeholder-role feature models next to the template (KARaceIngSPLCaseStudyVersion1/SPL_FeatureModels_v2_Stakeholders)
SPL = os.environ.get('KARACEING_SPL', os.path.join(os.path.dirname(TEMPLATE_DIR), 'SPL_FeatureModels_v2_Stakeholders'))
OUT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(TEMPLATE_DIR, 'vsum', 'src', 'test', 'resources', 'initialstate')
os.makedirs(OUT, exist_ok=True)

ROLES = ['prj', 'des', 'crash', 'kin', 'aero', 'thermo', 'elec', 'sim', 'val']
CAD_ROLES = ['des', 'crash', 'kin', 'aero']
CARS = [('EV3', 'EV3 (25e)', 'EV3_25e.json', 'EV3_25e'), ('CV2', 'CV2 (25h)', 'CV2_25h_Hydrogen.json', 'CV2_25h_Hydrogen'),
        ('CV1', 'CV1 (19c)', 'CV1_19c_Petrol.json', 'CV1_19c_Petrol')]

# ----------------------------------------------------------------------------- metamodels
rs = ResourceSet()
MM = {}
for f in ['UVL', 'configurationsMM', 'cad', 'requirements', 'thermal', 'testbench', 'electrical', 'bom', 'brakesystem', 'simuLink']:
    p = rs.get_resource(URI(f'{ECORE}/{f}.ecore')).contents[0]
    rs.metamodel_registry[p.nsURI] = p
    MM[f] = p


def C(mm, name):
    return MM[mm].getEClassifier(name)


def new(mm, cls, **kw):
    o = C(mm, cls)()
    for k, v in kw.items():
        if v is None:
            continue
        if isinstance(v, list):
            getattr(o, k).extend(v)
        else:
            setattr(o, k, v)
    return o


# ----------------------------------------------------------------------------- feature model
M = f'{SPL}/models/'
feats, cons, roots = U.load_composite([(M + 'KARaceIngSPL.uvl', '')] + [(M + r.upper() + '.uvl', r + '.') for r in ROLES])
DOMAIN_ROOT = {r: roots[r + '.'] for r in ROLES}              # 'des' -> 'des.DesignEngineering'
children = defaultdict(list)                                     # parent -> [child] in model order
groups = OrderedDict()                                           # gkey -> (parent, kind, [children])
for f, d in feats.items():
    if d['parent']:
        children[d['parent']].append(f)
        groups.setdefault(d['gkey'], (d['parent'], d['group'], []))[2].append(f)
for r in ROLES:
    feats[DOMAIN_ROOT[r]]['parent'] = 'KARaceIngSPL'


def short(q):
    return q.split('.')[-1]


def role_of(q):
    return q.split('.')[0] if '.' in q else None


def is_leaf(q):
    return not children.get(q)


# UVL model
GROUP_CLS = {'mandatory': 'Mandatory', 'optional': 'Optional', 'alternative': 'Alternative', 'or': 'Or'}
FEATURE = {}


def mk_feature(q):
    d = feats[q]
    t = d.get('type')
    f = new('UVL', 'Feature', name=short(q))
    if t:                                   # typed feature: the UVL metamodel's Integer/Float/String attribute
        f.attribute.append(new('UVL', {'Integer': 'Integer', 'Real': 'Float', 'String': 'String'}[t]))
    FEATURE[q] = f
    return f


root_feature = mk_feature('KARaceIngSPL')
root_group = new('UVL', 'Mandatory')
root_group.feature.append(root_feature)
root_feature.group = root_group


def build_subtree(q):
    f = FEATURE[q]
    for gkey, (parent, kind, chs) in groups.items():
        if parent != q:
            continue
        g = new('UVL', GROUP_CLS[kind])
        f.children.append(g)
        for c in chs:
            cf = mk_feature(c)
            g.feature.append(cf)
            cf.group = g
            build_subtree(c)


for r in ROLES:                                  # one Mandatory group per domain root (pattern requirement)
    g = new('UVL', 'Mandatory')
    root_feature.children.append(g)
    df = mk_feature(DOMAIN_ROOT[r])
    g.feature.append(df)
    df.group = g
    build_subtree(DOMAIN_ROOT[r])
assert len(FEATURE) == len(feats), (len(FEATURE), len(feats))

# constraints (only used to check the configurations; the UVL metamodel cannot express them)
PREC = {'iff': 1, 'imp': 2, 'or': 3, 'and': 4}


def text(e, parent=0):
    k = e[0]
    if k == 'var':
        return e[1]
    if k == 'num':
        v = e[1]
        return str(int(v)) if float(v).is_integer() else str(v)
    if k == 'not':
        inner = text(e[1], 5)
        return '!' + inner
    if k in PREC:
        op = {'iff': '<=>', 'imp': '=>', 'or': '|', 'and': '&'}[k]
        s = f'{text(e[1], PREC[k] + (1 if k == "imp" else 0))} {op} {text(e[2], PREC[k])}'
        return f'({s})' if PREC[k] < parent or (k == 'imp' and parent == PREC[k] + 1) else s
    if k == 'cmp':
        return f'{text(e[2], 6)} {e[1]} {text(e[3], 6)}'
    if k == 'ar':
        return f'({text(e[2], 7)} {e[1]} {text(e[3], 7)})'
    raise ValueError(e)


CONSTRAINTS = []                                            # (name, text, expr) - only used to check the configurations
for i, (e, raw, src) in enumerate(cons, 1):
    CONSTRAINTS.append((f'C{i:03d}', text(e), e))

uvl = new('UVL', 'UVLModel', tree=new('UVL', 'FeatureTree', root=root_group))

# ----------------------------------------------------------------------------- configurations
CFG = {}
for code, label, fn, col in CARS:
    d = json.load(open(f'{SPL}/configurations/{fn}'))
    sel = dict(d['config'])
    sel.update(d.get('example_completion') or {})
    CFG[code] = sel


def selected(code, q):
    v = CFG[code].get(q)
    return bool(v) if not isinstance(v, (int, float)) or isinstance(v, bool) else True


def check_config(code):
    """Python twin of the Java ConfigurationChecker: returns the list of issues."""
    sel = {q: selected(code, q) for q in feats}
    vals = {q: float(CFG[code][q]) for q in feats if feats[q].get('type') and not isinstance(CFG[code].get(q), bool) and CFG[code].get(q) is not None}
    issues = []
    for gkey, (p, kind, chs) in list(groups.items()) + [(('root', r), ('KARaceIngSPL', 'mandatory', [DOMAIN_ROOT[r]])) for r in ROLES]:
        if not sel[p]:
            for c in chs:
                if sel[c]:
                    issues.append(f'VIOLATED: {short(c)} selected without parent {short(p)}')
            continue
        n = sum(sel[c] for c in chs)
        if kind == 'mandatory' and n != len(chs):
            issues.append(f'VIOLATED: mandatory group of {short(p)}')
        if kind == 'alternative' and n != 1:
            issues.append(f'VIOLATED: alternative group of {short(p)}')
        if kind == 'or' and n < 1:
            issues.append(f'VIOLATED: or group of {short(p)}')

    def ev(e):
        k = e[0]
        if k == 'var': return sel[e[1]]
        if k == 'not': return not ev(e[1])
        if k == 'and': return ev(e[1]) and ev(e[2])
        if k == 'or': return ev(e[1]) or ev(e[2])
        if k == 'imp': return (not ev(e[1])) or ev(e[2])
        if k == 'iff': return ev(e[1]) == ev(e[2])
        if k == 'cmp':
            try:
                return U.eval_arith(e, vals)
            except KeyError:
                return False
        raise ValueError(k)
    for name, txt, e in CONSTRAINTS:
        if not ev(e):
            issues.append(f'VIOLATED: {name} {txt}')
    return issues


config_model = new('configurationsMM', 'ConfigurationModel')
CONFIG_FEATURE = {}
for code, label, fn, col in CARS:
    iss = check_config(code)
    assert not iss, (code, iss)
    conf = new('configurationsMM', 'Configuration', PLVersion=1)
    CONFIG_FEATURE[code] = {}
    for q in FEATURE:                                     # same order as the feature tree
        cf = new('configurationsMM', 'Feature', name=short(q), isSelected=selected(code, q))
        conf.feature.append(cf)
        CONFIG_FEATURE[code][q] = cf
    config_model.configuration.append(conf)

# ----------------------------------------------------------------------------- domain artefacts
CORR = []                      # (left, right, tag); left/right = object or '<file>#name:X' reference
UVL_MODEL_LITERAL = object()   # stands for uvlPackage.Literals.UVL_MODEL (correspondence used by uvl2uvl)
REALISATION = {}               # qualified feature -> (file, name) of its main artefact


def ref(file, name=None):
    return f'{file}#' + (f'name:{name}' if name is not None else '')


class Model:
    """Domain model under construction; enforces unique names per file."""
    def __init__(self, file, root, name_attr='name'):
        self.file, self.root, self.names, self.by_name = file, root, set(), {}
        self.name_attr = name_attr

    def reg(self, obj, name):
        assert name not in self.names, (self.file, name)
        self.names.add(name)
        self.by_name[name] = obj
        return obj


def feature_corr(q, file, name):
    CORR.append((f'karaceing.uvl#name:{short(q)}', ref(file, name), ''))
    REALISATION.setdefault(q, (file, name))


def kw_kind(n, table, default):
    for k, v in table:
        if k.lower() in n.lower():
            return v
    return default


# --- overrides and scenario artefacts ------------------------------------------------
# name overrides: the artefact realising a feature has another name than the feature
NAME_OVERRIDE = {'des.TankSystemIntegration': 'CV1ChassisAdapterFrame'}
# leaf features whose default realisation content is replaced: q -> [(class, name, attrs)]
LEAF_CONTENT = {
    'crash.MechanicallyAttachedBracing': [('Box', 'LugJoints', {}), ('Box', 'SleeveJoints', {})],
    'thermo.BackfireProtection': [('AnalysisCase', 'BackfireReliefAnalysis', {'method': '1D gas dynamics', 'result': 'relief valve opens at 3 bar', 'passed': True})],
    'thermo.EngineConversionToHydrogen': [('AnalysisCase', 'EngineConversionAnalysis', {'method': '0D/1D engine simulation', 'result': 'petrol base engine converted to H2 port injection', 'passed': True})],
    'des.TankSystemIntegration': [],
    'sim.VehicleDynamicsControl': [('Block', 'VDCSimulinkModel', {}), ('Block', 'VDCParameterSet', {})],
}
# features without a default realisation (realised by an extra artefact only)
SKIP_DEFAULT = {'kin.KinematicsForTyreModel'}   # realised only by the sketch KinematicsTables below Suspension (see EXTRA)
# extra artefacts: (role, parent element name, class, name, attrs, also-realises-feature or None)
EXTRA = [
    ('des', 'AccumulatorContainer', 'Sketch', 'CellFixationSketch', {}, None),
    ('des', 'IntakeSystem', 'Assembly', 'ReliefValveAndFlameArrestor', {}, None),
    ('des', 'Fasteners', 'Assembly', 'CriticalBracingBolts', {}, None),
    ('des', 'CombustionEngine', 'Assembly', 'HydrogenConversionParts', {}, None),
    ('des', 'HydrogenConversionParts', 'Box', 'AdaptedCylinderHead', {}, None),
    ('des', 'HydrogenConversionParts', 'Box', 'InjectorAdapters', {}, None),
    ('des', 'PressureRegulator', 'Box', 'RelayContacts', {}, None),
    ('crash', 'MainHoop', 'Box', 'LidarBracket', {}, 'crash.MainHoop'),
    ('kin', 'Suspension', 'Sketch', 'KinematicsTables', {}, 'kin.KinematicsForTyreModel'),
    ('elec', 'SDCHydrogen', 'Circuit', 'OverPressureRelayChain', {}, None),
    ('elec', 'OverPressureRelayChain', 'Device', 'OverPressureRelay1', {'kind': 'Relay'}, None),
    ('elec', 'OverPressureRelayChain', 'Device', 'OverPressureRelay2', {'kind': 'Relay'}, None),
    ('elec', 'TSWiring', 'Circuit', 'PlugDetectionCircuit', {}, None),
    ('elec', 'HighVoltage', 'Device', 'OldInverter', {'kind': 'Inverter', 'ratedVoltage': 600.0}, None),
    ('elec', 'AutonomousSystemElectrics', 'Device', 'CarPC', {'kind': 'ControlUnit'}, None),
    ('elec', 'AutonomousSystemElectrics', 'Device', 'ROS1BridgePort', {'kind': 'Connector'}, None),
    ('elec', 'BrakeSystemPlausibilityDevice', 'Device', 'BSPDTestConnector', {'kind': 'Connector'}, 'elec.BrakeSystemPlausibilityDevice'),
    ('sim', 'AutonomousFunctions', 'Block', 'LidarPoseMainHoop', {}, None),
    ('sim', 'ASStateMachine', 'Block', 'ROS1StateMachineNode', {}, None),
    ('sim', 'StatusIndication', 'Block', 'ROS1StatusNode', {}, None),
    ('sim', 'TSActivationAndReadyToDrive', 'Block', 'PlugDetectionSignal', {}, None),
    ('sim', 'PowerLimitation', 'Block', 'OldInverterInterface', {}, None),
    ('sim', 'Regeneration', 'Block', 'OldInverterRegenInterface', {}, None),
    ('sim', 'PowerLimitation', 'SubSystem', 'CellTemperatureDerating', {}, None),
    ('val', 'TechnicalInspection', 'TestCase', 'BSPDSignalInjectionTest', {'procedure': 'inject a test signal into the BSPD and check the SDC opens'}, None),
]
# overlaps between artefacts (by element name): (role, name, role, name)
EXTRA_OVERLAP = [
    ('des', 'HydrogenStorageSystem', 'crash', 'PrimaryStructure'),
    ('crash', 'AntiIntrusionPlate', 'aero', 'AeroDevices'),
    ('elec', 'CellTemperatureMonitoring', 'sim', 'CellTemperatureDerating'),
    ('thermo', 'WaterInjection', 'des', 'IntakeSystem'),
    ('sim', 'ASStateMachine', 'elec', 'AutonomousSystemElectrics'),
    ('thermo', 'BackfireReliefAnalysis', 'des', 'ReliefValveAndFlameArrestor'),
    ('crash', 'MechanicallyAttachedBracing', 'des', 'CriticalBracingBolts'),
    ('thermo', 'EngineConversionAnalysis', 'des', 'HydrogenConversionParts'),
    ('thermo', 'EngineConversionAnalysis', 'des', 'CV1ChassisAdapterFrame'),
    ('elec', 'OverPressureRelayChain', 'des', 'RelayContacts'),
    ('crash', 'LidarBracket', 'sim', 'LidarPoseMainHoop'),
    ('sim', 'ROS1StateMachineNode', 'elec', 'ROS1BridgePort'),
    ('sim', 'ROS1StatusNode', 'elec', 'ROS1BridgePort'),
    ('elec', 'PlugDetectionCircuit', 'sim', 'PlugDetectionSignal'),
    ('sim', 'VDCParameterSet', 'kin', 'KinematicsTables'),
    ('elec', 'OldInverter', 'sim', 'OldInverterInterface'),
    ('elec', 'OldInverter', 'sim', 'OldInverterRegenInterface'),
    ('val', 'BSPDSignalInjectionTest', 'elec', 'BSPDTestConnector'),
]

FILES = {'prj': 'prj.requirements', 'des': 'des.cad', 'crash': 'crash.cad', 'kin': 'kin.cad', 'aero': 'aero.cad',
         'thermo': 'thermo.thermal', 'elec': 'elec.electrical', 'sim': 'sim.simulink', 'val': 'val.testbench'}
MODELS = {}


def ordered_children(q):
    return children.get(q, [])


# ---- CAD ----------------------------------------------------------------------------
def cad_model(role):
    doc = new('cad', 'CADDocument', name=role.upper())
    m = Model(FILES[role], doc)
    for mat, rho in [('Steel', 7850.0), ('Aluminium', 2700.0), ('CFRP', 1600.0)]:
        doc.materials.append(new('cad', 'Material', name=mat, density=rho))
    steel = doc.materials[0]

    def realise(q, container_ref):
        name = NAME_OVERRIDE.get(q, short(q))
        d = feats[q]
        if q in SKIP_DEFAULT:
            assert is_leaf(q)
            return
        if d.get('type'):
            sk = new('cad', 'Sketch', name=name, label=f'{name} (design parameter)')
            line = new('cad', 'LineSegment', start=new('cad', 'Vector2', x=0.0, y=0.0), end=new('cad', 'Vector2', x=0.1, y=0.0))
            sk.geometries.append(line)
            v = CFG['EV3'].get(q) or CFG['CV2'].get(q) or CFG['CV1'].get(q) or 0
            sk.constraints.append(new('cad', 'Constraint', name=name, type=C('cad', 'ConstraintType').getEEnumLiteral('Distance'),
                                      value=float(v), first=line))
            container_ref.append(m.reg(sk, name))
            feature_corr(q, m.file, name)
            return
        a = new('cad', 'Assembly', name=name, label=name)
        container_ref.append(m.reg(a, name))
        feature_corr(q, m.file, name)
        for c in ordered_children(q):
            realise(c, a.children)
        if is_leaf(q) and not d.get('abstract'):
            content = LEAF_CONTENT.get(q, [('Box', name + 'Part', {})])
            for cls, n, attrs in (content or []):
                o = new('cad', cls, name=n, label=n, **attrs)
                if cls == 'Box':
                    o.length, o.width, o.height, o.material = 0.1, 0.05, 0.02, steel
                a.children.append(m.reg(o, n))
    for c in ordered_children(DOMAIN_ROOT[role]):
        realise(c, doc.objects)
    CORR.append((f'karaceing.uvl#name:{short(DOMAIN_ROOT[role])}', ref(m.file), ''))
    return m


# ---- generic tree domains ------------------------------------------------------------
ELEC_KIND = [('Switch', 'Switch'), ('Button', 'Switch'), ('Relay', 'Relay'), ('Sensor', 'Sensor'), ('Sensing', 'Sensor'),
             ('Monitoring', 'Sensor'), ('Light', 'Indicator'), ('Indicat', 'Indicator'), ('Sound', 'Indicator'), ('Battery', 'Battery'),
             ('Accumulator', 'Battery'), ('Plug', 'Connector'), ('Wiring', 'Connector'), ('Point', 'Connector'), ('Motor', 'Motor'),
             ('Device', 'ControlUnit'), ('Management', 'ControlUnit'), ('Disconnect', 'Switch'), ('Enclosure', 'Other')]
THERMO_KIND = [('Cooling', 'HeatExchanger'), ('ChargeAir', 'HeatExchanger'), ('Exhaust', 'HeatSource'), ('Protection', 'ThermalProtection'),
               ('Shield', 'ThermalProtection'), ('Cover', 'ThermalProtection'), ('Ventilation', 'FluidLine'), ('CatchCan', 'FluidLine'),
               ('Injection', 'FluidLine'), ('Fuel', 'FluidLine'), ('98RON', 'FluidLine')]


def tree_model(role):
    if role == 'elec':
        root = new('electrical', 'ElectricalDesign', name='ELEC'); top = root.elements
    elif role == 'sim':
        root = new('simuLink', 'SimulinkModel', name='SIM', version='R2024b', file='karaceing_control.slx', library=False); top = root.contains
    elif role == 'thermo':
        root = new('thermal', 'ThermalAnalysisModel', name='THERMO'); top = root.elements
    elif role == 'val':
        root = new('testbench', 'TestBenchConfiguration', name='VAL'); top = root.elements
        for n, qn, u in [('Stopwatch', 'time', 's'), ('TiltPlatform', 'angle', 'deg'), ('SoundLevelMeter', 'sound pressure', 'dB(A)'),
                         ('IsolationTester', 'isolation resistance', 'MOhm'), ('BrakeDeceleration', 'deceleration', 'm/s2')]:
            root.instruments.append(new('testbench', 'Instrument', name=n, measuredQuantity=qn, unit=u))
    else:
        raise ValueError(role)
    m = Model(FILES[role], root)
    hv = set()
    stack = ['elec.HighVoltage']
    while stack:
        x = stack.pop(); hv.add(x); stack += children.get(x, [])

    def container(q, name):
        if role == 'elec':
            o = new('electrical', 'Circuit', name=name, voltageClass=C('electrical', 'VoltageClass').getEEnumLiteral('HV' if q in hv else 'LV'),
                    nominalVoltage=600.0 if q in hv else 24.0)
            return o, o.elements
        if role == 'sim':
            o = new('simuLink', 'SubSystem', name=name, tag=short(q)); return o, o.subBlocks
        if role == 'thermo':
            o = new('thermal', 'ThermalSystem', name=name); return o, o.elements
        if role == 'val':
            o = new('testbench', 'TestSuite', name=name); return o, o.elements

    def leaf_content(q, name):
        if q in LEAF_CONTENT:
            out = []
            for cls, n, attrs in (LEAF_CONTENT[q] or []):
                mm = {'thermo': 'thermal', 'sim': 'simuLink', 'elec': 'electrical', 'val': 'testbench'}[role]
                out.append(new(mm, cls, name=n, **attrs))
            return out
        if role == 'elec':
            return [new('electrical', 'Device', name=name + 'Unit', kind=C('electrical', 'DeviceKind').getEEnumLiteral(kw_kind(name, ELEC_KIND, 'Other')))]
        if role == 'sim':
            b = new('simuLink', 'Block', name=name + 'Logic')
            b.parameters.append(new('simuLink', 'Parameter', name='SampleTime', type='double', value='0.001', readOnly=False))
            return [b]
        if role == 'thermo':
            k = kw_kind(name, THERMO_KIND, 'AnalysisCase')
            n = name + ('Analysis' if k == 'AnalysisCase' else 'Element')
            o = new('thermal', k, name=n)
            if k == 'AnalysisCase':
                o.method, o.passed = 'thermal analysis', True
            return [o]
        return []

    def realise(q, container_ref):
        name = NAME_OVERRIDE.get(q, short(q))
        d = feats[q]
        if role == 'val' and is_leaf(q):
            o = new('testbench', 'TestCase', name=name, procedure=f'Procedure of {name}', passCriterion='according to the rules')
            o.steps.append(new('testbench', 'TestStep', name=name + 'Setup', description='prepare the car'))
            o.steps.append(new('testbench', 'TestStep', name=name + 'Execution', description='run the test'))
            container_ref.append(m.reg(o, name)); feature_corr(q, m.file, name); return
        if role == 'thermo' and d.get('type'):
            o = new('thermal', 'ThermalProtection', name=name, material='silicone', temperatureResistance=float(CFG['EV3'].get(q) or 120))
            container_ref.append(m.reg(o, name)); feature_corr(q, m.file, name); return
        o, sub = container(q, name)
        container_ref.append(m.reg(o, name))
        feature_corr(q, m.file, name)
        for c in ordered_children(q):
            realise(c, sub)
        if is_leaf(q) and not d.get('abstract'):
            for x in leaf_content(q, name):
                sub.append(m.reg(x, x.name))

    for c in ordered_children(DOMAIN_ROOT[role]):
        realise(c, top)
    CORR.append((f'karaceing.uvl#name:{short(DOMAIN_ROOT[role])}', ref(m.file), ''))
    return m


# ---- requirements (PRJ) --------------------------------------------------------------
rows = []
for f in sorted(glob.glob(f'{SPL}/mapping/*_mapping.csv')):
    rows += list(csv.DictReader(open(f, encoding='utf-8-sig'), delimiter=';'))
REQ_FEATURES = defaultdict(list)
REQ_ROW = OrderedDict()
for r in rows:
    REQ_FEATURES[r['Item_ID']].append(r['Feature'])
    REQ_ROW.setdefault(r['Item_ID'], r)
KIND = {'Anforderung': 'Requirement', 'Ziel': 'Goal', 'Funktion': 'Function'}


def requirement(r):
    q = new('requirements', 'Requirement', name=r['Item_ID'], text=r['Beschreibung_kurz'], subteam=r['Subteam'], category=r['Subkategorie'],
            unit=r['Einheit'], priority=r['Prioritaet'], role=r['Role'], kind=C('requirements', 'RequirementKind').getEEnumLiteral(KIND[r['Typ']]))
    for code, label, fn, col in CARS:
        v = (r.get(col) or '').strip()
        if v:
            q.carValues.append(new('requirements', 'CarValue', name=code, value=v))
    return q


def req_model():
    spec = new('requirements', 'RequirementsSpecification', name='PRJ',
               description='Requirements specification of the KA-RaceIng product line (EV3, CV2, CV1)')
    m = Model(FILES['prj'], spec)
    sec_of = {}

    def realise(q, container_ref):
        name = short(q)
        s = new('requirements', 'Section', name=name, description=f'Requirements and decisions for {name}')
        container_ref.append(m.reg(s, name))
        feature_corr(q, m.file, name)
        sec_of[q] = s
        for c in ordered_children(q):
            realise(c, s.sections)
    for c in ordered_children(DOMAIN_ROOT['prj']):
        realise(c, spec.sections)
    sec_of[DOMAIN_ROOT['prj']] = None
    dom = new('requirements', 'Section', name='DomainRequirements', description='Requirements realised by the other stakeholder roles')
    spec.sections.append(m.reg(dom, 'DomainRequirements'))
    role_sec = {}
    for rid, r in REQ_ROW.items():
        fs = REQ_FEATURES[rid]
        prj_fs = [f for f in fs if role_of(f) == 'prj']
        req = requirement(r)
        if prj_fs and sec_of.get(prj_fs[0]) is not None:
            sec_of[prj_fs[0]].requirements.append(m.reg(req, rid))
        elif prj_fs:
            spec_root_sec = role_sec.setdefault('PRJ', None)
            if spec_root_sec is None:
                spec_root_sec = new('requirements', 'Section', name='PRJRequirements', description='General project requirements')
                dom.sections.append(m.reg(spec_root_sec, 'PRJRequirements')); role_sec['PRJ'] = spec_root_sec
            spec_root_sec.requirements.append(m.reg(req, rid))
        else:
            rc = r['Role']
            if rc not in role_sec:
                s = new('requirements', 'Section', name=rc + 'Requirements', description=f'Requirements realised by {r["Stakeholder"]}')
                dom.sections.append(m.reg(s, rc + 'Requirements')); role_sec[rc] = s
            role_sec[rc].requirements.append(m.reg(req, rid))
    CORR.append((f'karaceing.uvl#name:{short(DOMAIN_ROOT["prj"])}', ref(m.file), ''))
    return m


# ---- build all ----------------------------------------------------------------------
MODELS['prj'] = req_model()
for r in CAD_ROLES:
    MODELS[r] = cad_model(r)
for r in ['elec', 'sim', 'thermo', 'val']:
    MODELS[r] = tree_model(r)


def find_parent(role, name):
    return MODELS[role].by_name[name]


def child_ref(o):
    cn = o.eClass.name
    return {'Assembly': 'children', 'Circuit': 'elements', 'SubSystem': 'subBlocks', 'ThermalSystem': 'elements', 'TestSuite': 'elements'}[cn]


MMOF = {'des': 'cad', 'crash': 'cad', 'kin': 'cad', 'aero': 'cad', 'elec': 'electrical', 'sim': 'simuLink', 'thermo': 'thermal', 'val': 'testbench'}
for role, parent, cls, name, attrs, realises in EXTRA:
    p = find_parent(role, parent)
    a = dict(attrs)
    if 'kind' in a:
        a['kind'] = C('electrical', 'DeviceKind').getEEnumLiteral(a['kind'])
    o = new(MMOF[role], cls, name=name, **a)
    if cls == 'Box':
        o.length, o.width, o.height = 0.05, 0.05, 0.01
    if cls == 'Sketch':
        line = new('cad', 'LineSegment', start=new('cad', 'Vector2', x=0.0, y=0.0), end=new('cad', 'Vector2', x=0.2, y=0.0))
        o.geometries.append(line)
        o.constraints.append(new('cad', 'Constraint', name=name + 'Length', type=C('cad', 'ConstraintType').getEEnumLiteral('Distance'), value=0.2, first=line))
    getattr(p, child_ref(p)).append(MODELS[role].reg(o, name))
    if realises:
        feature_corr(realises, MODELS[role].file, name)

# ---- report scenarios: brake system, brake pedal, tyres (Inconsistency Scenarios TechReport, KA-RaceIng) ----
# The ten KA-RaceIng scenarios of the report are mapped onto the existing models (FM unchanged):
#   Brake Specification Model = des.brakesystem, SysML architecture / electrical design = elec.electrical,
#   supplier data sheets / material data = BOM item parameters, wear data = brake specification attributes.
# The instances below are the minimum the scenarios need. Values are plausible FS values, not measured ones.
MATERIAL_DATA = {           # density kg/m3, specific heat J/(kg K), thermal conductivity W/(m K) (same table in Java)
    'Steel': (7850.0, 460.0, 45.0), 'Aluminium': (2700.0, 900.0, 205.0), 'CFRP': (1600.0, 1000.0, 5.0),
    'Titanium': (4500.0, 520.0, 7.0), 'StainlessSteel': (8000.0, 500.0, 16.0)}
des_doc = MODELS['des'].root
for mat in ['Titanium', 'StainlessSteel']:
    des_doc.materials.append(new('cad', 'Material', name=mat, density=MATERIAL_DATA[mat][0]))
CAD_MAT = {m.name: m for m in des_doc.materials}


def cad_add(parent, cls, name, material=None, **attrs):
    o = new('cad', cls, name=name, label=name, **attrs)
    if material:
        o.material = CAD_MAT[material]
    parent.children.append(MODELS['des'].reg(o, name))
    return o


hyd = MODELS['des'].by_name['HydraulicBrakes']
REPORT_CAD = {}
for pos, r in [('Front', 0.110), ('Rear', 0.100)]:
    disc = cad_add(hyd, 'Assembly', f'{pos}BrakeDisc')                       # R1: solid disc, holes added in the scenario
    cad_add(disc, 'Cylinder', f'{pos}BrakeDiscBody', 'Steel', radius=r, height=0.004)
    cal = cad_add(hyd, 'Assembly', f'{pos}BrakeCaliper')                     # R2: monobloc caliper (one housing body)
    cad_add(cal, 'Box', f'{pos}CaliperBody', 'Aluminium', length=0.12, width=0.04, height=0.05)
    cad_add(cal, 'Cylinder', f'{pos}CaliperPiston', 'Titanium', radius=0.0125 if pos == 'Front' else 0.011, height=0.02)   # R3
    cad_add(cal, 'Box', f'{pos}CaliperMountingBracket', 'Aluminium', length=0.08, width=0.03, height=0.008, x=0.0, y=0.0, z=0.0)  # R2
    cad_add(cal, 'Box', f'{pos}BrakePads', 'StainlessSteel', length=0.05, width=0.04, height=0.008)   # R7
cad_add(hyd, 'Cylinder', 'FrontBrakeHose', 'StainlessSteel', radius=0.002, height=0.45)
# R4: the brake pedal is machined from titanium (allowed by ANF-217 before the regulation change)
MODELS['des'].by_name['BrakePedalPart'].material = CAD_MAT['Titanium']


def tree_add(role, parent_name, mm, cls, name, realises=None, **attrs):
    p = MODELS[role].by_name[parent_name] if parent_name else MODELS[role].root
    o = new(mm, cls, name=name, **attrs)
    ref_name = child_ref(p) if parent_name else {'elec': 'elements', 'sim': 'contains', 'thermo': 'elements', 'val': 'elements', 'aero': 'objects'}[role]
    getattr(p, ref_name).append(MODELS[role].reg(o, name))
    if realises:
        feature_corr(realises, MODELS[role].file, name)
    return o


def sim_param(block, name, value, typ='double'):
    block.parameters.append(new('simuLink', 'Parameter', name=name, type=typ, value=str(value), readOnly=False))


# SIM (R1, R6, R7, R8, R9): brake and tyre models used by the vehicle dynamics control
tree_add('sim', 'VehicleDynamicsControl', 'simuLink', 'SubSystem', 'BrakeSystemModel', tag='BrakeSystemModel')
for pos in ['Front', 'Rear']:
    tree_add('sim', 'BrakeSystemModel', 'simuLink', 'Block', f'{pos}BrakeDiscModel')
    tree_add('sim', 'BrakeSystemModel', 'simuLink', 'Block', f'{pos}BrakePadModel')
tree_add('sim', 'BrakeSystemModel', 'simuLink', 'Block', 'ABSControl')
tree_add('sim', 'BrakeSystemModel', 'simuLink', 'Block', 'BOTSSwitchLogic')
tree_add('sim', 'VehicleDynamicsControl', 'simuLink', 'SubSystem', 'TyreModel', tag='TyreModel')
for n, vals in [('DryTyreParameterSet', (1.6, 120000.0, 0.015, 3.2, 0.35)), ('WetTyreParameterSet', (1.2, 125000.0, 0.02, 3.6, 0.38))]:
    b = tree_add('sim', 'TyreModel', 'simuLink', 'Block', n)
    for pn, v in zip(['grip', 'verticalStiffness', 'rollingResistance', 'mass', 'inertia'], vals):
        sim_param(b, pn, v)
# ELEC (R2, R6, R9): brake sensors, ABS control unit, brake over-travel switch in the shutdown circuit
tree_add('elec', 'Sensors', 'electrical', 'Circuit', 'BrakeSensors', voltageClass=C('electrical', 'VoltageClass').getEEnumLiteral('LV'), nominalVoltage=12.0)
SENSOR = C('electrical', 'DeviceKind')
for pos in ['Front', 'Rear']:
    d = tree_add('elec', 'BrakeSensors', 'electrical', 'Device', f'{pos}CaliperTemperatureSensor', kind=SENSOR.getEEnumLiteral('Sensor'),
                 partNumber='TS-100', mountingPosition=f'{pos}CaliperMountingBracket')
    d.pins.append(new('electrical', 'Pin', name='Temperature', signal=f'{pos.lower()}_caliper_temperature'))
    d = tree_add('elec', 'BrakeSensors', 'electrical', 'Device', f'{pos}WheelSpeedSensor', kind=SENSOR.getEEnumLiteral('Sensor'),
                 partNumber='WS-20', mountingPosition=f'{pos}BrakeDisc')
    d.pins.append(new('electrical', 'Pin', name='WheelSpeed', signal=f'{pos.lower()}_wheel_speed'))
d = tree_add('elec', 'BrakeSensors', 'electrical', 'Device', 'ABSControlUnit', kind=SENSOR.getEEnumLiteral('ControlUnit'), partNumber='ABS-7')
for pn, sig in [('WheelSpeedFront', 'front_wheel_speed'), ('WheelSpeedRear', 'rear_wheel_speed'), ('ValveCommand', 'abs_valve_command')]:
    d.pins.append(new('electrical', 'Pin', name=pn, signal=sig))
d = tree_add('elec', 'ShutdownCircuit', 'electrical', 'Device', 'BOTSSwitch', realises='des.BrakeOverTravelSwitch',
             kind=SENSOR.getEEnumLiteral('Switch'), partNumber='BOTS-1', mountingPosition='BrakePedal')
d.pins.append(new('electrical', 'Pin', name='BOTSState', signal='bots_sdc_open'))
# SIM signal parameters mirror the pins of the ELEC counterparts (rule AR11)
for dev, blk in [('ABSControlUnit', 'ABSControl'), ('BOTSSwitch', 'BOTSSwitchLogic')]:
    for pin in MODELS['elec'].by_name[dev].pins:
        sim_param(MODELS['sim'].by_name[blk], 'signal:' + pin.name, pin.signal, 'string')
    sim_param(MODELS['sim'].by_name[blk], 'SampleTime', 0.001)
# THERMO (R1, R3): thermal models of the brake parts (material data from the supplier data sheets)
tree_add('thermo', None, 'thermal', 'ThermalSystem', 'BrakeThermalManagement')
PISTON_THERMO = {}
for pos in ['Front', 'Rear']:
    rho, cp, k = MATERIAL_DATA['Titanium']
    PISTON_THERMO[pos] = tree_add('thermo', 'BrakeThermalManagement', 'thermal', 'ThermalComponent', f'{pos}CaliperPistonThermalModel',
                                  material='Titanium', supplier='PistonSupplier A', thermalConductivity=k, specificHeatCapacity=cp,
                                  temperatureLimit=350.0)
    tree_add('thermo', 'BrakeThermalManagement', 'thermal', 'HeatSource', f'{pos}BrakeDiscHeatSource', heatLoad=25000.0, maxTemperature=650.0)
ac = tree_add('thermo', 'BrakeThermalManagement', 'thermal', 'AnalysisCase', 'CaliperPistonTemperatureAnalysis', method='transient FE (endurance)',
              ambientTemperature=30.0, result='piston temperature 310 C < limit 350 C', passed=True)
ac.analysedElements.extend(PISTON_THERMO.values())
# VAL (R2, R3, R4, R5, R1): brake validation suite
tree_add('val', None, 'testbench', 'TestSuite', 'BrakeValidation')
VERIFIES = {}


def test_case(name, procedure, criterion, steps, verifies=()):
    o = tree_add('val', 'BrakeValidation', 'testbench', 'TestCase', name, procedure=procedure, passCriterion=criterion)
    for sn, desc, exp in steps:
        o.steps.append(new('testbench', 'TestStep', name=sn, description=desc, expectedResult=exp))
    o.verifiedRequirements.extend(verifies)
    VERIFIES[name] = list(verifies)
    return o


test_case('BrakeAssemblyBuild', 'build the brake assembly from the ordered parts', 'all parts fit the CAD model',
          [('BrakeAssemblyBuildCheck', 'compare ordered parts with the CAD model', 'parts fit')], ['ANF-205'])
test_case('CaliperPistonTemperatureTest', 'endurance run with thermocouples on the caliper pistons', 'piston temperature below the limit',
          [('CaliperPistonTemperatureMeasurement', 'measure the piston temperature', 'FrontCaliperPistonThermalModel temperature <= 350.0 C')])
test_case('BrakeDiscSimulationCorrelation', 'brake test on the test bench, compared with the brake model of the simulation',
          'deceleration deviates at most 5 % from the simulation',
          [('BrakeDiscDecelerationMeasurement', 'measure the deceleration', 'deviation <= 5 %')])
test_case('BrakePedalMaterialCompliance', 'check the material certificate of the brake pedal', 'material in {Steel, Aluminium, Titanium} (ANF-217)',
          [('BrakePedalMaterialCertificate', 'inspect the certificate', 'material certified')], ['ANF-217'])
test_case('ScatterShieldCompliance', 'technical inspection of the scatter shields (T 7.3.2 - T 7.3.4)', 'scatter shield present and compliant',
          [('ScatterShieldInspection', 'inspect position, width and fasteners', 'compliant')],
          ['ANF-259', 'ANF-260', 'ANF-261', 'ANF-262', 'ANF-263', 'ANF-264', 'ANF-265'])
test_case('BrakeOverTravelSwitchTest', 'simulate a brake circuit failure and press the pedal beyond its travel', 'the SDC opens',
          [('BOTSActuation', 'press the pedal beyond its travel', 'SDC open')], ['ANF-221', 'ANF-222'])
# AERO (R1, FT-08): the region around wheels and brakes in which the aerodynamics engineer assesses the airflow
tree_add('aero', None, 'cad', 'Assembly', 'WheelAndBrakeFlowRegion', label='WheelAndBrakeFlowRegion')
# PRJ (R4): ANF-217 lists the allowed brake pedal materials (before the regulation change: also machined titanium)
ALLOWED_VALUES = {'ANF-217': ['Steel', 'Aluminium', 'Titanium']}

# overlaps: interface constraints between roles (except PRJ variant drivers) + scenario overlaps
OVERLAP = []
OVERLAP_SOURCE = {}            # sorted ((file, name), (file, name)) -> (source, justification[, kind])


def vars_of(e):
    if e[0] == 'var': return [e[1]]
    out = []
    for x in e[1:]:
        if isinstance(x, tuple): out += vars_of(x)
    return out


for name, txt, e in CONSTRAINTS:
    vs = [v for v in dict.fromkeys(vars_of(e)) if role_of(v) not in (None, 'prj')]
    for i, a in enumerate(vs):
        for b in vs[i + 1:]:
            if role_of(a) != role_of(b):
                OVERLAP.append((a, b, f'interface constraint {name}: {txt}'))
seen = set()
for a, b, why in OVERLAP:
    fa, na = REALISATION[a]; fb, nb = REALISATION[b]
    key = tuple(sorted([(fa, na), (fb, nb)]))
    if key in seen: continue
    seen.add(key)
    CORR.append((ref(fa, na), ref(fb, nb), 'overlap'))
    OVERLAP_SOURCE[key] = ('interface constraint', why)
for ra, na, rb, nb in EXTRA_OVERLAP:
    assert na in MODELS[ra].names and nb in MODELS[rb].names, (ra, na, rb, nb)
    key = tuple(sorted([(FILES[ra], na), (FILES[rb], nb)]))
    if key in seen: continue
    seen.add(key)
    CORR.append((ref(FILES[ra], na), ref(FILES[rb], nb), 'overlap'))
    OVERLAP_SOURCE[key] = ('evolution scenario', 'needed by an evolution scenario (EXTRA_OVERLAP)')

# report overlaps (element level): (file, name, file, name, kind, justification); kinds: see OVERLAP_KINDS
REPORT_OVERLAP = []


def rov(fa, na, fb, nb, kind, why):
    REPORT_OVERLAP.append((fa, na, fb, nb, kind, why))


for pos in ['Front', 'Rear']:
    rov('des.cad', f'{pos}BrakeDisc', 'sim.simulink', f'{pos}BrakeDiscModel', 'K1', 'R1: mass, inertia, heat capacity and venting of the disc are simulation parameters')
    rov('des.cad', f'{pos}BrakePads', 'sim.simulink', f'{pos}BrakePadModel', 'K1', 'R7: pad geometry is the nominal pad of the brake model')
    rov('des.cad', f'{pos}BrakeDisc', 'thermo.thermal', f'{pos}BrakeDiscHeatSource', 'K2', 'R1: the vented disc stores less heat and dissipates more')
    rov('des.cad', f'{pos}CaliperPiston', 'thermo.thermal', f'{pos}CaliperPistonThermalModel', 'K2', 'R3: piston geometry and material feed the thermal model')
    rov('des.cad', f'{pos}CaliperMountingBracket', 'elec.electrical', f'{pos}CaliperTemperatureSensor', 'K3', 'R2: the temperature sensor is mounted on the caliper bracket')
    rov('des.cad', f'{pos}BrakeDisc', 'elec.electrical', f'{pos}WheelSpeedSensor', 'K3', 'R2/R6: the wheel speed sensor reads the disc (mounting interface)')
    rov('des.cad', f'{pos}BrakeDisc', 'kin.cad', 'Wheels', 'K9', 'R1 (FT-09): the disc is unsprung rotating mass of the wheel')
    rov('des.cad', f'{pos}BrakeDisc', 'aero.cad', 'WheelAndBrakeFlowRegion', 'K9', 'R1 (FT-08): venting holes change the airflow around wheel and brake')
    rov('des.cad', f'{pos}BrakeCaliper', 'val.testbench', 'BrakeAssemblyBuild', 'K7', 'R2: the validation engineer builds the brake assembly from the ordered parts')
    rov('des.cad', f'{pos}BrakeDisc', 'val.testbench', 'BrakeDiscSimulationCorrelation', 'K7', 'R1: test bench results are compared with the simulation of the disc')
    rov('des.brakesystem', f'{pos}BrakeDisk', 'des.cad', f'{pos}BrakeDisc', 'K5', 'R1: specification and CAD describe the same disc (ventilated, thickness)')
    rov('des.brakesystem', f'{pos}BrakeDisk', 'sim.simulink', f'{pos}BrakeDiscModel', 'K5', 'R7: worn disc thickness is a simulation parameter')
    rov('des.brakesystem', f'{pos}BrakeCaliper', 'des.cad', f'{pos}BrakeCaliper', 'K5', 'R2: specification type monobloc / two-piece')
    rov('des.brakesystem', f'{pos}BrakePads', 'des.cad', f'{pos}BrakePads', 'K5', 'R7: specified and designed pad')
    rov('des.brakesystem', f'{pos}BrakePads', 'sim.simulink', f'{pos}BrakePadModel', 'K5', 'R7: pad wear (thickness, wear warning) is a simulation parameter')
    rov('des.brakesystem', f'{pos}WheelSpeedSensor', 'elec.electrical', f'{pos}WheelSpeedSensor', 'K5', 'R6: ABS sensor of the specification is the electrical wheel speed sensor')
    rov('thermo.thermal', f'{pos}CaliperPistonThermalModel', 'val.testbench', 'CaliperPistonTemperatureTest', 'K7', 'R3: the temperature limit is the pass criterion of the test')
rov('des.brakesystem', 'FrontBrakeHose', 'des.cad', 'FrontBrakeHose', 'K5', 'specified and designed brake hose')
rov('elec.electrical', 'ABSControlUnit', 'sim.simulink', 'ABSControl', 'K8', 'R6: ABS signal interfaces are the inputs/outputs of the ABS control block')
rov('elec.electrical', 'BOTSSwitch', 'sim.simulink', 'BOTSSwitchLogic', 'K8', 'R6/R9: BOTS signal is evaluated by the switch logic')
rov('elec.electrical', 'BrakeSensors', 'sim.simulink', 'BrakeSystemModel', 'K8', 'R6/R9: new brake devices need a counterpart in the brake model')
rov('elec.electrical', 'BOTSSwitch', 'des.cad', 'BrakeOverTravelSwitch', 'K3', 'R9: the switch is mounted at the pedal box')
rov('elec.electrical', 'BOTSSwitch', 'val.testbench', 'BrakeOverTravelSwitchTest', 'K7', 'R9: the BOTS function is validated')
rov('kin.cad', 'Wheels', 'sim.simulink', 'TyreModel', 'K10', 'R8: every tyre configuration has a parameter set in the tyre model')
rov('kin.cad', 'DryTyres', 'sim.simulink', 'DryTyreParameterSet', 'K10', 'R8: slick tyre parameters')
rov('kin.cad', 'WetTyres', 'sim.simulink', 'WetTyreParameterSet', 'K10', 'R8: rain tyre parameters')
rov('des.cad', 'BrakePedalPart', 'val.testbench', 'BrakePedalMaterialCompliance', 'K7', 'R4: material compliance of the brake pedal')
rov('des.cad', 'ScatterShield', 'val.testbench', 'ScatterShieldCompliance', 'K7', 'R5: the scatter shield is inspected')
for fa, na, fb, nb, kind, why in REPORT_OVERLAP:
    key = tuple(sorted([(fa, na), (fb, nb)]))
    assert key not in seen, key
    seen.add(key)
    CORR.append((ref(fa, na), ref(fb, nb), 'overlap'))
    OVERLAP_SOURCE[key] = ('report scenario', why, kind)

# ---- BOM and brake system ---------------------------------------------------------------
# BOM item parameters (supplier data / procurement data of the report): mass and material are derived from the
# CAD geometry (rule AR2), supplier and part number come from procurement, specification is the variant that is
# ordered (solid / vented disc, monobloc / two-piece caliper).
import math
bom = new('bom', 'BOM', name='KA-RaceIng BOM')
types = {}
PARAMS = [('mass', 'EDouble'), ('material', 'EString'), ('supplier', 'EString'), ('specification', 'EString'), ('partNumber', 'EString')]
for tn, desc in [('Part', 'manufactured part'), ('Assembly', 'assembly'), ('PurchasedPart', 'purchased part'), ('Fastener', 'standard part'),
                 ('ElectricalComponent', 'electrical / electronic component')]:
    t = new('bom', 'ItemType', name=tn, description=desc)
    for pn, dt in PARAMS:
        t.parameterDefinitions.append(new('bom', 'ParameterDefinition', name=pn, dataType=C('bom', 'DataType').getEEnumLiteral(dt)))
    bom.itemTypes.append(t); types[tn] = t
BOM_IDS = set()


def volume(o):
    """Volume of a CAD solid in m3 (same rules as KARaceIngOverlapRules.volume)."""
    c = o.eClass.name
    if c == 'Box': return o.length * o.width * o.height
    if c == 'Cylinder': return math.pi * o.radius ** 2 * o.height
    if c == 'Sphere': return 4 / 3 * math.pi * o.radius ** 3
    if c == 'Cone': return math.pi * o.height * (o.radius1 ** 2 + o.radius1 * o.radius2 + o.radius2 ** 2) / 3
    if c == 'BooleanOperation':
        op = o.operation.name if o.operation else 'Fuse'
        vb = volume(o.base) if o.base else 0.0
        vt = [volume(t) for t in o.tools]
        return max(vb - sum(vt), 0.0) if op == 'Cut' else (min([vb] + vt) if op == 'Common' else vb + sum(vt))
    return 0.0


def density(o):
    if o.eClass.name == 'BooleanOperation':
        return density(o.base) if o.base else 0.0
    m = getattr(o, 'material', None)
    return m.density if m is not None and m.density else 0.0


def mass(o):
    """Mass in kg: solids volume x density, assemblies the sum of their parts (solids consumed by a boolean operation are skipped)."""
    if o.eClass.name == 'Assembly':
        consumed = set()
        for ch in o.children:
            if ch.eClass.name == 'BooleanOperation':
                consumed.update(id(x) for x in [ch.base] + list(ch.tools) if x is not None)
        return sum(mass(ch) for ch in o.children if id(ch) not in consumed)
    return volume(o) * density(o) if o.eClass.findEStructuralFeature('material') else 0.0


def materials(o):
    if o.eClass.name == 'Assembly':
        return sorted({m for ch in o.children for m in materials(ch)})
    m = getattr(o, 'material', None) if o.eClass.findEStructuralFeature('material') else None
    return [m.name] if m is not None else []


def bom_item(item_id, tname, file, name, n=1, material=None, supplier='', specification='', partNumber=''):
    assert item_id not in BOM_IDS, item_id
    BOM_IDS.add(item_id)
    it = new('bom', 'Item', id=item_id, type=types[tname], numberOfItems=n)
    o = MODELS_BY_FILE[file].by_name[name]
    defs = {d.name: d for d in types[tname].parameterDefinitions}
    if o.eClass.findEStructuralFeature('material') or o.eClass.name == 'Assembly':
        m = mass(o)
        mats = materials(o)
        if m > 0:
            it.parameterValues.append(new('bom', 'DoubleParameterValue', definition=defs['mass'], value=round(m, 3), unit='kg'))
        material = material or (mats[0] if len(mats) == 1 else None)
    for pn, v in [('material', material), ('supplier', supplier), ('specification', specification), ('partNumber', partNumber)]:
        if v:
            it.parameterValues.append(new('bom', 'StringParameterValue', definition=defs[pn], value=v))
    bom.items.append(it)
    CORR.append((ref(file, name), f'karaceing.bom#name:{item_id}', 'overlap'))
    OVERLAP_SOURCE[tuple(sorted([(file, name), ('karaceing.bom', item_id)]))] = ('physical part', 'the CAD part / electrical component is the BOM item')
    return it


MODELS_BY_FILE = {m.file: m for m in MODELS.values()}
BRAKE_PARTS = {'FrontBrakeDisc', 'RearBrakeDisc', 'FrontBrakeCaliper', 'RearBrakeCaliper', 'FrontCaliperPiston', 'RearCaliperPiston',
               'FrontBrakeHose'}
for role in CAD_ROLES:
    m = MODELS[role]
    for n, o in m.by_name.items():
        if o.eClass.name == 'Box':
            if o.material is None:
                o.material = m.root.materials[0]        # parts without material are steel parts
            bom_item(f'{role.upper()}-{n}', 'Part', m.file, n)
bom_item('DES-HydrogenTank', 'PurchasedPart', 'des.cad', 'HydrogenTank', material='CFRP')
bom_item('DES-CriticalBracingBolts', 'Fastener', 'des.cad', 'CriticalBracingBolts', n=8, material='Steel')
bom_item('ELEC-OldInverter', 'ElectricalComponent', 'elec.electrical', 'OldInverter', material='-', partNumber='INV-600')
# brake parts (R1-R4): purchased discs and pistons with supplier, the caliper as an assembly with its specification
for pos in ['Front', 'Rear']:
    bom_item(f'DES-{pos}BrakeDisc', 'PurchasedPart', 'des.cad', f'{pos}BrakeDisc', n=2, supplier='DiscSupplier A', specification='solid',
             partNumber=f'BD-{pos[0]}-220S')
    bom_item(f'DES-{pos}BrakeCaliper', 'Assembly', 'des.cad', f'{pos}BrakeCaliper', n=2, specification='monobloc')
    bom_item(f'DES-{pos}CaliperPiston', 'PurchasedPart', 'des.cad', f'{pos}CaliperPiston', n=4, supplier='PistonSupplier A',
             partNumber=f'CP-{pos[0]}-25T')
    for dev, pn in [(f'{pos}CaliperTemperatureSensor', 'TS-100'), (f'{pos}WheelSpeedSensor', 'WS-20')]:
        bom_item(f'ELEC-{dev}', 'ElectricalComponent', 'elec.electrical', dev, n=2, material='-', partNumber=pn)
bom_item('DES-FrontBrakeHose', 'PurchasedPart', 'des.cad', 'FrontBrakeHose', n=2, supplier='HoseSupplier A', partNumber='BH-450')
bom_item('ELEC-ABSControlUnit', 'ElectricalComponent', 'elec.electrical', 'ABSControlUnit', material='-', partNumber='ABS-7')
bom_item('ELEC-BOTSSwitch', 'ElectricalComponent', 'elec.electrical', 'BOTSSwitch', material='-', partNumber='BOTS-1')
# supplier data sheets (K6): BOM piston items <-> thermal piston models; BOM pistons and calipers <-> validation build
for pos in ['Front', 'Rear']:
    rov('karaceing.bom', f'DES-{pos}CaliperPiston', 'thermo.thermal', f'{pos}CaliperPistonThermalModel', 'K6',
        'R3: supplier and material of the piston determine the material properties of the thermal model')
    rov('karaceing.bom', f'DES-{pos}CaliperPiston', 'val.testbench', 'BrakeAssemblyBuild', 'K7', 'R3: new pistons must be integrated in the test bench')
    rov('karaceing.bom', f'DES-{pos}BrakeDisc', 'des.brakesystem', f'{pos}BrakeDisk', 'K5', 'R1: the ordered disc follows the specification (vented)')
    rov('karaceing.bom', f'DES-{pos}BrakeCaliper', 'des.brakesystem', f'{pos}BrakeCaliper', 'K5', 'R2: the ordered caliper follows the specification (two-piece)')
for fa, na, fb, nb, kind, why in REPORT_OVERLAP[-8:]:
    CORR.append((ref(fa, na), ref(fb, nb), 'overlap'))
    OVERLAP_SOURCE[tuple(sorted([(fa, na), (fb, nb)]))] = ('report scenario', why, kind)

# SIM brake model parameters derived from the CAD geometry (rule AR2) and the brake specification (AR12)
def fmt5(v):
    """5 significant digits, plain notation (as KARaceIngOverlapRules.fmt)."""
    from decimal import Decimal
    return format(Decimal('%.5g' % v).normalize(), 'f') if v else '0'


def heat_capacity(o):
    if o.eClass.name == 'Assembly':
        return sum(heat_capacity(ch) for ch in o.children)
    m = mass(o)
    mat = getattr(o, 'material', None) if o.eClass.findEStructuralFeature('material') else None
    return m * MATERIAL_DATA[mat.name][1] if mat is not None else 0.0


for pos in ['Front', 'Rear']:
    disc = MODELS['des'].by_name[f'{pos}BrakeDisc']
    m = mass(disc)
    r = MODELS['des'].by_name[f'{pos}BrakeDiscBody'].radius
    b = MODELS['sim'].by_name[f'{pos}BrakeDiscModel']
    for pn, v in [('mass', m), ('inertia', 0.5 * m * r * r), ('heatCapacity', heat_capacity(disc))]:
        sim_param(b, pn, fmt5(v))
    sim_param(b, 'ventilated', 'false', 'boolean')
    sim_param(b, 'discThickness', 4, 'double')
    b = MODELS['sim'].by_name[f'{pos}BrakePadModel']
    sim_param(b, 'padThickness', 8, 'double')
    sim_param(b, 'wearWarning', 'false', 'boolean')

brake = new('brakesystem', 'Brakesystem', instanceName='BRAKE', specificationType='hydraulic dual circuit')
for pos, d_mm, piston in [('Front', 220, 25), ('Rear', 200, 22)]:
    brake.BrakeComponents.append(new('brakesystem', 'BrakeDisk', id=f'{pos}BrakeDisk', fittingPosition=pos.lower(), specificationType='KA-RaceIng',
                                     OEM_number=f'BD-{pos[0]}-220S', diameterInMM=d_mm, brakeDiskThicknessInMM=4, minimumThicknessInMM=3,
                                     ventilated=False))
    cal = new('brakesystem', 'BrakeCaliper', id=f'{pos}BrakeCaliper', fittingPosition=pos.lower(), specificationType='monobloc',
              pistonDiameterInMM=piston, brakeDiskThickness=4)
    cal.brakePads.append(new('brakesystem', 'BrakePad', id=f'{pos}BrakePads', fittingPosition=pos.lower(), specificationType='sintered',
                             heightInMM=40, widthInMM=50, thicknessInMM=8, wearWarning=False))
    brake.BrakeComponents.append(cal)
    brake.BrakeComponents.append(new('brakesystem', 'ABSSensor', id=f'{pos}WheelSpeedSensor', fittingPosition=pos.lower(),
                                     specificationType='hall effect', sensorType='hall', numberOfPins=3, lengthInMM=40))
brake.BrakeComponents.append(new('brakesystem', 'BrakeHose', id='FrontBrakeHose', fittingPosition='front', specificationType='steel braided',
                                 threadSize1='M10x1', threadSize2='M10x1', lengthInMM=450))
CORR.append(('des.brakesystem#', ref('des.cad', 'HydraulicBrakes'), 'overlap'))
OVERLAP_SOURCE[tuple(sorted([('des.brakesystem', None), ('des.cad', 'HydraulicBrakes')]))] = ('report scenario', 'the brake specification describes the hydraulic brakes', 'K5')     # the specification describes the hydraulic brakes

# ---- UVL <-> config and feature <-> config feature --------------------------------------------
CORR.append(('karaceing.uvl#', 'karaceing.config#', ''))
CORR.append(('karaceing.uvl#', UVL_MODEL_LITERAL, ''))
for code, *_ in CARS:
    for q in FEATURE:
        CORR.append((FEATURE[q], CONFIG_FEATURE[code][q], ''))


# ----------------------------------------------------------------------------- write the persisted VSUM
import hashlib, shutil
from xml.sax.saxutils import quoteattr

PLACEHOLDER = 'file:/KARACEING_INITIAL_STATE'
ROOTS = OrderedDict([('karaceing.uvl', uvl), ('karaceing.config', config_model)])
for role, m in MODELS.items():
    ROOTS[m.file] = m.root
ROOTS['karaceing.bom'] = bom
ROOTS['des.brakesystem'] = brake
FILE_OF_ROOT = {id(r): f for f, r in ROOTS.items()}


def root_of(o):
    while o.eContainer() is not None:
        o = o.eContainer()
    return o


def path_segments(o):
    """EMF URI fragment segments (EObjectImpl.eURIFragmentSegment): @feature or @feature.index."""
    segs = []
    while o.eContainer() is not None:
        f = o.eContainmentFeature()
        c = o.eContainer()
        segs.append(f'@{f.name}.{list(c.eGet(f)).index(o)}' if f.many else f'@{f.name}')
        o = c
    return list(reversed(segs))


_RESOLVED = {}


def resolve_name_ref(reference):
    if reference not in _RESOLVED:
        _RESOLVED[reference] = _resolve_name_ref(reference)
    return _RESOLVED[reference]


def _resolve_name_ref(reference):
    """'<file>#' (root) or '<file>#name:X[/name:Y]' (first element below with that name / id / instanceName)."""
    file, path = reference.split('#', 1)
    cur = ROOTS[file]
    for seg in [p for p in path.split('/') if p]:
        name = seg.split(':', 1)[1]
        cur = next(x for x in cur.eAllContents()
                   if any(getattr(x, a, None) == name for a in ('name', 'id', 'instanceName') if x.eClass.findEStructuralFeature(a)))
    return cur


def href(o):
    """href from vsum/correspondences.correspondence to the element (EMF default XMI fragment)."""
    if o is UVL_MODEL_LITERAL:
        return 'http://vitruv.tools/methodologisttemplate/uvl#//UVLModel'
    segs = path_segments(o)
    return f'../{FILE_OF_ROOT[id(root_of(o))]}#/' + ('/' + '/'.join(segs) if segs else '')


def obj(x):
    return x if not isinstance(x, str) else resolve_name_ref(x)


# ---- every artefact corresponds to a feature ------------------------------------------------
# Rule (Fabian, 2026-10-25): an artefact without a feature is not allowed. Artefacts are all elements of the
# solution-space models that represent something; value objects inside them are exempt. Elements that are
# not the main realisation of a feature (generated details such as ...Part / ...Unit / ...Logic, the
# scenario-specific artefacts, the role sections of the requirements specification, instruments) get the
# feature of their containing element; requirements get the features they are mapped to in the mapping;
# BOM items and brake components get the features of the CAD element they overlap with.
EXEMPT = {'Material', 'LineSegment', 'Circle', 'Vector2', 'Constraint', 'Parameter', 'TestStep', 'CarValue', 'ItemType',
          'ParameterDefinition', 'StringParameterValue', 'DoubleParameterValue', 'IntegerParameterValue', 'BoolParameterValue',
          'Pin', 'BusSignalMapping', 'IdentifierReference', 'LibraryLinkReference'}
ARTEFACT_FILES = [m.file for m in MODELS.values()] + ['karaceing.bom', 'des.brakesystem']


def elem_ref(o):
    f = FILE_OF_ROOT[id(root_of(o))]
    if o.eContainer() is None:
        return ref(f)
    n = getattr(o, 'name', None) or getattr(o, 'id', None) or getattr(o, 'instanceName', None)
    assert n, o
    return ref(f, n)


FEATURES_OF = defaultdict(list)             # id(artefact) -> feature refs ('karaceing.uvl#name:X')
for a, b, t in CORR:
    if t == '' and isinstance(a, str) and a.startswith('karaceing.uvl#name:') and isinstance(b, str) and b.split('#')[0] in ARTEFACT_FILES:
        FEATURES_OF[id(obj(b))].append(a)
OVERLAP_OF = defaultdict(list)
for a, b, t in CORR:
    if t == 'overlap':
        oa, ob = obj(a), obj(b)
        OVERLAP_OF[id(oa)].append(ob); OVERLAP_OF[id(ob)].append(oa)
added = defaultdict(int)


def map_to(o, feature_refs, why):
    for fr in dict.fromkeys(feature_refs):
        CORR.append((fr, elem_ref(o), ''))
        FEATURES_OF[id(o)].append(fr)
    added[why] += 1


# requirements -> mapped features (all of them, PRJ features included)
for req in [x for x in ROOTS['prj.requirements'].eAllContents() if x.eClass.name == 'Requirement']:
    map_to(req, [f'karaceing.uvl#name:{short(q)}' for q in REQ_FEATURES[req.name]], 'requirement -> mapped features')
# all remaining artefacts: feature of the nearest containing element that has one (parents before children; the
# role models first, so that BOM items and brake components find the features of the overlapping CAD elements)
for fn in [m.file for m in MODELS.values()] + ['karaceing.bom', 'des.brakesystem']:
    root = ROOTS[fn]
    for o in [root] + list(root.eAllContents()):
        if o.eClass.name in EXEMPT or FEATURES_OF[id(o)]:
            continue
        if fn in ('karaceing.bom', 'des.brakesystem') and o.eContainer() is not None:
            partners = OVERLAP_OF[id(o)]
            cad = [p for p in partners if FILE_OF_ROOT[id(root_of(p))].endswith('.cad')]
            fs = [fr for p in (cad or [p for p in partners if FILE_OF_ROOT[id(root_of(p))] != 'des.brakesystem']) for fr in FEATURES_OF[id(p)]]
            if fs:
                map_to(o, fs, f'{fn}: feature of the overlapping CAD element'); continue
        if o is ROOTS['karaceing.bom']:
            map_to(o, ['karaceing.uvl#name:KARaceIngSPL'], 'BOM root -> root feature'); continue
        if o is ROOTS['des.brakesystem']:
            map_to(o, FEATURES_OF[id(obj(ref('des.cad', 'HydraulicBrakes')))], 'brake system root -> HydraulicBrakes'); continue
        p = o.eContainer()
        while p is not None and not FEATURES_OF[id(p)]:
            p = p.eContainer()
        assert p is not None, (fn, o)
        map_to(o, FEATURES_OF[id(p)][:1], f'{fn.split(".")[1]}: feature of the containing element')
missing = [(fn, o.eClass.name, getattr(o, 'name', None)) for fn in ARTEFACT_FILES for o in [ROOTS[fn]] + list(ROOTS[fn].eAllContents())
           if o.eClass.name not in EXEMPT and not FEATURES_OF[id(o)]]
assert not missing, missing[:10]
print('feature correspondences added for artefacts without feature:', dict(added))

# ---- requirement traces ---------------------------------------------------------------------------
# A requirement traces to every artefact of the features it is mapped to (all solution-space models, BOM and
# brake specification included); requirements of PRJ features are traced like all others. The PRJ model
# itself (sections, requirements) is not traced. Test cases additionally trace to the requirements they
# verify (TestCase.verifiedRequirements).
REQS_OF_FEATURE = defaultdict(list)
for rid, fs in REQ_FEATURES.items():
    for q in dict.fromkeys(fs):
        REQS_OF_FEATURE[f'karaceing.uvl#name:{short(q)}'].append(rid)
for rid, vals in ALLOWED_VALUES.items():
    MODELS['prj'].by_name[rid].allowedValues.extend(vals)
traced = set()
TRACE_FILES = [f for f in ARTEFACT_FILES if f != 'prj.requirements']
for fn in TRACE_FILES:
    for o in [ROOTS[fn]] + list(ROOTS[fn].eAllContents()):
        if o.eClass.name in EXEMPT:
            continue
        for fr in dict.fromkeys(FEATURES_OF[id(o)]):
            for rid in REQS_OF_FEATURE.get(fr, []):
                if (rid, id(o)) not in traced:
                    traced.add((rid, id(o)))
                    CORR.append((ref('prj.requirements', rid), o, 'requirement'))
for tc, rids in VERIFIES.items():
    o = MODELS['val'].by_name[tc]
    for rid in rids:
        if (rid, id(o)) not in traced:
            traced.add((rid, id(o)))
            CORR.append((ref('prj.requirements', rid), o, 'requirement'))

for fn, root in ROOTS.items():
    r = rs.create_resource(URI(f'{OUT}/{fn}'))
    r.append(root)
    r.save()

vsum_dir = os.path.join(OUT, 'vsum')
shutil.rmtree(vsum_dir, ignore_errors=True)
os.makedirs(vsum_dir)
old_csv = os.path.join(OUT, 'correspondences.csv')
if os.path.exists(old_csv):
    os.remove(old_csv)
with open(os.path.join(vsum_dir, 'models.models'), 'w') as fh:
    for fn in ROOTS:
        fh.write(f'{PLACEHOLDER}/{fn}\n')


def uuid_for(key):
    """Deterministic UUID in the form Vitruvius generates ("ord_" + EcoreUtil.generateUUID())."""
    import base64
    digest = base64.urlsafe_b64encode(hashlib.sha1(key.encode()).digest()[:16]).decode().rstrip('=')
    return 'ord__' + digest


with open(os.path.join(vsum_dir, 'uuid.uuid'), 'w') as fh:
    for fn, root in ROOTS.items():
        for o in [root] + list(root.eAllContents()):
            segs = path_segments(o)
            hid = f'{PLACEHOLDER}/{fn}#/0' + ('/' + '/'.join(segs) if segs else '')
            fh.write(f'{uuid_for(hid)}|{hid}\n')

lines = ['<?xml version="1.0" encoding="ASCII"?>',
         '<correspondence:Correspondences xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" '
         'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" '
         'xmlns:correspondence="http://vitruv.tools/metamodels/change/correspondence/1.0" '
         'xmlns:correspondence_1="http://vitruv.tools/metamodels/dsls/reactions/runtime/correspondence/1.0">']
for a, b, t in CORR:
    lines.append(f'  <correspondences xsi:type="correspondence_1:ReactionsCorrespondence" tag={quoteattr(t)}>')
    lines.append(f'    <leftEObjects href={quoteattr(href(obj(a)))}/>')
    lines.append(f'    <rightEObjects href={quoteattr(href(obj(b)))}/>')
    lines.append('  </correspondences>')
lines.append('</correspondence:Correspondences>')
with open(os.path.join(vsum_dir, 'correspondences.correspondence'), 'w', encoding='ascii', errors='xmlcharrefreplace') as fh:
    fh.write('\n'.join(lines) + '\n')

stats = defaultdict(int)
for a, b, t in CORR: stats[t or 'feature/config'] += 1
print('features', len(FEATURE), 'constraints', len(CONSTRAINTS), 'requirements', len(REQ_ROW), 'bom items', len(BOM_IDS))
print('elements per model', {r: len(m.names) for r, m in MODELS.items()})
print('correspondences', dict(stats))

# ----------------------------------------------------------------------------- overlap catalogue (docs/OVERLAP_CATALOGUE.md)
# Every overlap correspondence gets a kind. The kinds are the semantic overlaps the KA-RaceIng scenarios of the
# Inconsistency Scenarios TechReport need (R1-R10); overlaps from the feature-model constraints and from the
# evolution scenarios are classified by the pair of models they connect.
OVERLAP_KINDS = OrderedDict([
    ('K1', ('CAD geometry / mass -> SIM parameters', 'R1 (mass, inertia, heat capacity of the vented disc), R7',
            'AR2: mass, inertia, heat capacity and venting are derived from the CAD geometry and written to the block parameters')),
    ('K2', ('CAD <-> THERMO thermal model', 'R1 (cooling of the vented disc), R3 (piston)',
            'AR2/AR6: material and geometry changes update the thermal model or notify the thermodynamics engineer')),
    ('K3', ('CAD <-> ELEC mounting / packaging', 'R2 (mounting interfaces of the two-piece caliper), R9',
            'AR4: Device.mountingPosition names the CAD element; renames are propagated, moves are notified')),
    ('K4', ('CAD part <-> BOM item', 'R1, R2, R4 (the ordered part is the designed part)',
            'AR2: mass, material and specification of the item are derived from the CAD part')),
    ('K5', ('Brake specification <-> CAD / BOM / SIM / ELEC', 'R1 (ventilated), R2 (monobloc / two-piece), R6, R7 (wear)',
            'AR1/AR3/AR12: specification attributes are propagated to BOM and SIM, CAD mismatches are notified')),
    ('K6', ('BOM supplier / material data <-> THERMO', 'R3 (supplier change of the caliper pistons)',
            'AR6: supplier and material of the item update the thermal component (material table), analyses become outdated')),
    ('K7', ('Artefact <-> validation (test cases, limits)', 'R2 (assembly build), R3 (temperature limit), R4, R5 (compliance)',
            'AR7/AR8/AR10: limits and pass criteria are updated, the validation engineer is notified')),
    ('K8', ('ELEC signals <-> SIM control model', 'R6 (integrated ABS/BOTS unit), R9 (new BOTS sensor)',
            'AR11: pins and signals of the device are the signal parameters of the block; new devices get a block')),
    ('K9', ('CAD <-> CAD across roles (shared space, mass)', 'R1 (FT-08 aerodynamics, FT-09 kinematics), constraints',
            'AR2: the other CAD role is notified (mass difference for KIN; notification only for AERO)')),
    ('K10', ('KIN tyres <-> SIM tyre parameter sets', 'R8 (intermediate tyre)',
             'AR13: a new tyre set gets a parameter set initialised from the existing sets')),
    ('K11', ('ELEC component <-> BOM item', 'R2 (step 7), R6',
             'AR5: the part number of the device is the part number of the item')),
])


def dom(f):
    return 'CAD' if f.endswith('.cad') else {'sim.simulink': 'SIM', 'thermo.thermal': 'THERMO', 'elec.electrical': 'ELEC', 'val.testbench': 'VAL',
                                              'karaceing.bom': 'BOM', 'des.brakesystem': 'BRAKE', 'prj.requirements': 'PRJ'}[f]


def kind_of(fa, fb):
    p = {dom(fa), dom(fb)}
    if 'BRAKE' in p: return 'K5'
    if 'VAL' in p: return 'K7'
    if p == {'CAD'}: return 'K9'
    if p == {'CAD', 'BOM'}: return 'K4'
    if p == {'ELEC', 'BOM'}: return 'K11'
    if p == {'BOM', 'THERMO'}: return 'K6'
    if p == {'CAD', 'THERMO'}: return 'K2'
    if p == {'CAD', 'ELEC'}: return 'K3'
    if p == {'CAD', 'SIM'}: return 'K1'
    if p == {'ELEC', 'SIM'}: return 'K8'
    raise ValueError(p)


TESTS = os.path.join(TEMPLATE_DIR, 'vsum', 'src', 'test', 'java', 'tools', 'vitruv', 'methodologisttemplate', 'vsum', 'karaceing')
SCEN = []                                          # (scenario id, body) from the test sources
for tf in sorted(glob.glob(os.path.join(TESTS, '*ScenarioTest.java')) + glob.glob(os.path.join(TESTS, 'Scenario*Test.java'))):
    for part in open(tf).read().split('@Test')[1:]:
        m = re.search(r'@DisplayName\("(\S+)', part)
        if m:
            SCEN.append((m.group(1), part))
catalogue = []
for a, b, t in CORR:
    if t != 'overlap':
        continue
    oa, ob = obj(a), obj(b)
    fa, fb = FILE_OF_ROOT[id(root_of(oa))], FILE_OF_ROOT[id(root_of(ob))]
    na = None if oa.eContainer() is None else elem_ref(oa).split('name:')[1]
    nb = None if ob.eContainer() is None else elem_ref(ob).split('name:')[1]
    src = OVERLAP_SOURCE.get(tuple(sorted([(fa, na), (fb, nb)])), ('?', ''))
    kind = src[2] if len(src) > 2 else kind_of(fa, fb)
    used = [sid for sid, body in SCEN if (na and f'"{na}"' in body) or (nb and f'"{nb}"' in body)]
    catalogue.append(dict(kind=kind, a=f'{fa}:{na or "<root>"}', b=f'{fb}:{nb or "<root>"}', source=src[0], why=src[1], scenarios=used))
os.makedirs(os.path.join(TEMPLATE_DIR, 'docs'), exist_ok=True)
json.dump(dict(kinds={k: list(v) for k, v in OVERLAP_KINDS.items()}, overlaps=catalogue),
          open(os.path.join(TEMPLATE_DIR, 'karaceing-generator', 'diagrams', 'overlaps.json'), 'w'), indent=1)
md = ['# Overlap catalogue (generated by karaceing-generator/initstate.py)', '',
      'Every correspondence tagged `overlap` in the initial state, with its kind, where it comes from and the scenario',
      'tests whose source code names one of the two elements. The kinds are motivated by the KA-RaceIng scenarios of the',
      '*Inconsistency Scenarios* TechReport (R1-R10, see REPORT_SCENARIOS.md); the rules AR1-AR13 are implemented in',
      '`KARaceIngOverlapRules` (consistency module) and run in the generated `Overlap<Src>To<Tgt>` reactions.', '',
      '## Kinds', '', '| Kind | Overlap | Report scenarios | Rule | Count |', '|---|---|---|---|---|']
cnt = defaultdict(int)
for c in catalogue: cnt[c['kind']] += 1
for k, (title, rep, rule) in OVERLAP_KINDS.items():
    md.append(f'| {k} | {title} | {rep} | {rule} | {cnt[k]} |')
md += ['', f'Sources: ' + ', '.join(f'{s} {n}' for s, n in sorted(defaultdict(int, {s: sum(1 for c in catalogue if c["source"] == s) for s in {c["source"] for c in catalogue}}).items())), '']
for k in OVERLAP_KINDS:
    rows = [c for c in catalogue if c['kind'] == k]
    if not rows:
        continue
    md += [f'## {k} - {OVERLAP_KINDS[k][0]} ({len(rows)})', '', '| Element | Element | Source | Justification | Used by tests |', '|---|---|---|---|---|']
    for c in rows:
        md.append(f"| `{c['a']}` | `{c['b']}` | {c['source']} | {c['why'].replace('|', '/')} | {', '.join(c['scenarios'][:6]) + (' …' if len(c['scenarios']) > 6 else '')} |")
    md.append('')
open(os.path.join(TEMPLATE_DIR, 'docs', 'OVERLAP_CATALOGUE.md'), 'w').write('\n'.join(md) + '\n')
print('overlap kinds', dict(sorted(cnt.items(), key=lambda x: int(x[0][1:]))))
