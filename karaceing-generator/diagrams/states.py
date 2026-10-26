"""Load persisted VSUM states and compare them.

A state is a VSUM storage folder (the initial state in vsum/src/test/resources/initialstate, or a snapshot
written by a scenario test with -Dkaraceing.snapshotDir=...). Every instance is identified by the UUID that
Vitruvius keeps for it in vsum/uuid.uuid, so instances can be matched across states even when they move or
their siblings change. A correspondence is identified by (left UUID, right UUID, tag).
"""
import os, re, xml.etree.ElementTree as ET
from pyecore.resources import ResourceSet, URI

HERE = os.path.dirname(os.path.abspath(__file__))
TEMPLATE = os.path.dirname(os.path.dirname(HERE))
ECORE = os.path.join(TEMPLATE, 'model', 'src', 'main', 'ecore')
FILES = ['karaceing.uvl', 'karaceing.config', 'prj.requirements', 'des.cad', 'crash.cad', 'kin.cad', 'aero.cad',
         'elec.electrical', 'sim.simulink', 'thermo.thermal', 'val.testbench', 'karaceing.bom', 'des.brakesystem']
LITERAL = 'UVL_MODEL'          # pseudo UUID of uvlPackage.Literals.UVL_MODEL


def _resource_set():
    rs = ResourceSet()
    for f in ['UVL', 'configurationsMM', 'cad', 'requirements', 'thermal', 'testbench', 'electrical', 'bom', 'brakesystem', 'simuLink']:
        p = rs.get_resource(URI(f'{ECORE}/{f}.ecore')).contents[0]
        rs.metamodel_registry[p.nsURI] = p
    return rs


def _segments(o):
    out = []
    while o.eContainer() is not None:
        f = o.eContainmentFeature(); c = o.eContainer()
        out.append(f'@{f.name}.{list(c.eGet(f)).index(o)}' if f.many else f'@{f.name}')
        o = c
    return list(reversed(out))


def _name(o):
    for a in ('name', 'id', 'instanceName'):
        if o.eClass.findEStructuralFeature(a) is not None and getattr(o, a, None) not in (None, ''):
            return str(getattr(o, a))
    return None


def _attrs(o):
    out = {}
    for a in o.eClass.eAllAttributes():
        if a.derived or a.transient:
            continue
        v = o.eGet(a)
        if a.many:
            v = list(v)
            if not v:
                continue
            out[a.name] = ', '.join(str(x) for x in v)
        elif v is not None and o.eIsSet(a):
            out[a.name] = str(getattr(v, 'name', v))
    return out


def load_state(folder):
    """{uuid: element}, [(left uuid, right uuid, tag)].  element = dict(file, frag, parent, cls, name, attrs, ref)"""
    rs = _resource_set()
    by_frag = {}
    for line in open(os.path.join(folder, 'vsum', 'uuid.uuid')):
        line = line.strip()
        if not line:
            continue
        uid, uri = line.split('|', 1)
        path, hid = uri.split('#', 1)
        segs = hid.split('/')[2:]                        # '/0/@a/@b.1' -> ['@a', '@b.1']
        by_frag[(os.path.basename(path), '/'.join(segs))] = uid
    els = {}
    for fn in FILES:
        res = rs.get_resource(URI(os.path.join(folder, fn)))
        root = res.contents[0]
        for o in [root] + list(root.eAllContents()):
            segs = _segments(o)
            uid = by_frag[(fn, '/'.join(segs))]
            parent = None if o.eContainer() is None else by_frag[(fn, '/'.join(_segments(o.eContainer())))]
            els[uid] = dict(file=fn, frag='//' + '/'.join(segs) if segs else '/', parent=parent, cls=o.eClass.name,
                            name=_name(o), attrs=_attrs(o), ref=o.eContainmentFeature().name if o.eContainer() is not None else None)
    frag_uid = {(e['file'], e['frag']): u for u, e in els.items()}
    # EMF writes references to objects with an ID attribute (BOM Item.id) as '#<id>' instead of a path
    frag_uid.update({(e['file'], e['attrs']['id']): u for u, e in els.items() if 'id' in e['attrs']})
    corr = []
    for c in ET.parse(os.path.join(folder, 'vsum', 'correspondences.correspondence')).getroot():
        ends = []
        for side in c:
            h = side.get('href')
            if h.startswith('http'):
                ends.append(LITERAL)
            else:
                f, frag = h.replace('../', '').split('#')
                ends.append(frag_uid[(f, frag)])
        corr.append((ends[0], ends[1], c.get('tag') or ''))
    return els, corr


def diff(before, after):
    """Changes from state `before` to state `after` (both from load_state)."""
    (eb, cb), (ea, ca) = before, after
    added = [u for u in ea if u not in eb]
    removed = [u for u in eb if u not in ea]
    moved, changed = [], []
    for u in ea:
        if u in eb:
            b, a = eb[u], ea[u]
            if b['parent'] != a['parent'] or b['ref'] != a['ref']:
                moved.append(u)
            ch = {k: (b['attrs'].get(k), a['attrs'].get(k)) for k in set(b['attrs']) | set(a['attrs']) if b['attrs'].get(k) != a['attrs'].get(k)}
            if ch:
                changed.append((u, ch))
    sb, sa = set(cb), set(ca)
    return dict(added=added, removed=removed, moved=moved, changed=changed,
                corr_added=[c for c in ca if c not in sb], corr_removed=[c for c in cb if c not in sa])


if __name__ == '__main__':
    import sys
    init = load_state(os.path.join(TEMPLATE, 'vsum', 'src', 'test', 'resources', 'initialstate'))
    print('initial:', len(init[0]), 'instances,', len(init[1]), 'correspondences')
    for folder in sys.argv[1:]:
        d = diff(init, load_state(folder))
        print(os.path.basename(folder), {k: len(v) for k, v in d.items()})
