"""Build the interactive explorer of the KA-RaceIng initial state (explorer.html) from data.json."""
import json, os
HERE = os.path.dirname(os.path.abspath(__file__))

d = json.load(open(os.path.join(HERE, 'data.json')))
classes = sorted({e['c'] for e in d['els']})
ci = {c: i for i, c in enumerate(classes)}
TAG = {'': 0, 'overlap': 1, 'requirement': 2}
packed = dict(
    files=d['files'],
    meta=['UVL.ecore', 'configurationsMM.ecore', 'requirements.ecore', 'cad.ecore', 'cad.ecore', 'cad.ecore', 'cad.ecore',
          'electrical.ecore', 'simuLink.ecore', 'thermal.ecore', 'testbench.ecore', 'bom.ecore', 'brakesystem.ecore'],
    role=['Problem space', 'Problem space', 'PRJ', 'DES', 'CRASH', 'KIN', 'AERO', 'ELEC', 'SIM', 'THERMO', 'VAL', 'linked to CAD', 'linked to DES'],
    classes=classes,
    e=[[e['m'], -1 if e['p'] is None else e['p'], ci[e['c']], e['n'] if e['n'] is not None else 0] + ([1 if e['s'] else 0] if 's' in e else [])
       for e in d['els']],
    c=[[a, b, TAG[t]] for a, b, t in d['corr']],
)
# overlap kinds (docs/OVERLAP_CATALOGUE.md): correspondence index -> kind, plus the kind descriptions
ov = os.path.join(HERE, 'overlaps.json')
if os.path.exists(ov):
    cat = json.load(open(ov))
    idx = {}
    for i, e in enumerate(d['els']):
        f = d['files'][e['m']]
        idx[(f, e['n'] if e['p'] is not None else '<root>')] = i
    by_pair = {}
    for o in cat['overlaps']:
        (fa, na), (fb, nb) = o['a'].split(':', 1), o['b'].split(':', 1)
        a, b = idx.get((fa, na)), idx.get((fb, nb))
        if a is not None and b is not None:
            by_pair[(min(a, b), max(a, b))] = o['kind']
    packed['ok'] = {str(i): by_pair[(min(a, b), max(a, b))] for i, (a, b, t) in enumerate(d['corr'])
                    if t == 'overlap' and (min(a, b), max(a, b)) in by_pair}
    packed['kinds'] = cat['kinds']
sc = os.path.join(HERE, 'scenarios.json')
if os.path.exists(sc):
    packed['sc'] = json.load(open(sc))
data = json.dumps(packed, separators=(',', ':'))
html = open(os.path.join(HERE, 'explorer_template.html')).read().replace('/*__DATA__*/null', data)
open(os.path.join(os.path.dirname(os.path.dirname(HERE)), 'docs', 'initial_state_explorer.html'), 'w').write(html)
print(len(html))
