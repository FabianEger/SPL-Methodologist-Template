"""Static diagram of a representative slice of the KA-RaceIng initial state.

Every box row is an instance read from the persisted initial state (data.json, extracted from
vsum/src/test/resources/initialstate), every arrow a ReactionsCorrespondence of
vsum/correspondences.correspondence between two shown instances. Nothing is drawn by hand.
"""
import json, os, sys
HERE = os.path.dirname(os.path.abspath(__file__))
from xml.sax.saxutils import escape

d = json.load(open(os.path.join(HERE, 'data.json')))
E, F, CORR = d['els'], d['files'], d['corr']
LIT = -1                                     # uvlPackage.Literals.UVL_MODEL


def find(file, name, cls=None):
    r = [i for i, e in enumerate(E) if F[e['m']] == file and e['n'] == name and (cls is None or e['c'] == cls)]
    assert len(r) >= 1, (file, name)
    return r


def anc(i):
    out = []
    while i is not None:
        out.append(i); i = E[i]['p']
    return out


FOCUS = {
    'karaceing.uvl': find('karaceing.uvl', 'HydrogenTank') + find('karaceing.uvl', 'PressureRegulator') + find('karaceing.uvl', 'SDCHydrogen'),
    'karaceing.config': [i for i in find('karaceing.config', 'HydrogenTank')],
    'des.cad': find('des.cad', 'PressureRegulatorPart') + find('des.cad', 'RelayContacts') + find('des.cad', 'HydrogenTank'),
    'elec.electrical': find('elec.electrical', 'SDCHydrogenUnit') + find('elec.electrical', 'OverPressureRelay1') + find('elec.electrical', 'OverPressureRelay2'),
    'karaceing.bom': find('karaceing.bom', 'DES-HydrogenTank') + find('karaceing.bom', 'DES-PressureRegulatorPart') + find('karaceing.bom', 'DES-RelayContacts'),
    'prj.requirements': find('prj.requirements', 'ANF-947') + find('prj.requirements', 'ANF-1001'),
}
SHOWN = set()
for f, ids in FOCUS.items():
    for i in ids:
        SHOWN.update(anc(i))

children = {}
for i, e in enumerate(E):
    children.setdefault(e['p'], []).append(i)

META = {'karaceing.uvl': 'UVL.ecore', 'karaceing.config': 'configurationsMM.ecore', 'des.cad': 'cad.ecore  (role DES)',
        'elec.electrical': 'electrical.ecore  (role ELEC)', 'karaceing.bom': 'bom.ecore', 'prj.requirements': 'requirements.ecore  (role PRJ)'}


def label(i):
    e = E[i]
    n = e['n']
    if F[e['m']] == 'karaceing.config' and e['c'] == 'Configuration':
        n = None
        s = f': Configuration   [{e["n"]}]'
        return s
    s = (n or '') + ' : ' + e['c']
    if 's' in e:
        s += f'   isSelected={str(e["s"]).lower()}'
    return s


def rows_of(file):
    """Tree rows (depth, element id) of the shown elements of a model + '…' rows for hidden siblings."""
    root = next(i for i in SHOWN if F[E[i]['m']] == file and E[i]['p'] is None)
    out = []

    def walk(i, dep):
        out.append((dep, i))
        kids = children.get(i, [])
        shown = [k for k in kids if k in SHOWN]
        for k in shown:
            walk(k, dep + 1)
        hidden = len(kids) - len(shown)
        if hidden:
            out.append((dep + 1, ('more', hidden)))
    walk(root, 0)
    return out


# ------------------------------------------------------------------ layout
ROW, HEAD, PADX = 19, 34, 10
FONT = "font-family='Inter, Helvetica, Arial, sans-serif'"
COLS = {  # file -> (x, y, width)
    'karaceing.config': (20, 150, 330),
    'karaceing.uvl': (420, 70, 380),
    'des.cad': (900, 70, 330),
    'elec.electrical': (900, 470, 330),
    'karaceing.bom': (1330, 70, 300),
    'prj.requirements': (1330, 330, 300),
}
COLOR = {'': '#1f2937', 'overlap': '#d97706', 'requirement': '#2563eb'}
DASH = {'': '', 'overlap': "stroke-dasharray='7 4'", 'requirement': "stroke-dasharray='2 3'"}
pos = {}                     # element id -> (x_left, x_right, y_center)
svg = []
MODEL_FILL = {'karaceing.uvl': '#eef2ff', 'karaceing.config': '#f5f3ff', 'des.cad': '#ecfdf5', 'elec.electrical': '#fefce8',
              'karaceing.bom': '#fff7ed', 'prj.requirements': '#eff6ff'}

# hidden correspondence counts (to elements not shown)
hidden_corr = {}
for a, b, t in CORR:
    for x, y in ((a, b), (b, a)):
        if x in SHOWN and not (y in SHOWN or y == LIT):
            hidden_corr[x] = hidden_corr.get(x, 0) + 1

for file, (x, y, w) in COLS.items():
    rows = rows_of(file)
    h = HEAD + len(rows) * ROW + 10
    svg.append(f"<rect x='{x}' y='{y}' width='{w}' height='{h}' rx='8' fill='{MODEL_FILL[file]}' stroke='#94a3b8'/>")
    svg.append(f"<text x='{x + PADX}' y='{y + 16}' {FONT} font-size='13' font-weight='700' fill='#0f172a'>{file}</text>")
    svg.append(f"<text x='{x + PADX}' y='{y + 29}' {FONT} font-size='10.5' fill='#475569'>instance of {META[file]}</text>")
    last = []                                   # containment: elbow line from the container to each contained row
    for r, (dep, i) in enumerate(rows):
        cy = y + HEAD + r * ROW + ROW / 2 + 4
        tx = x + PADX + dep * 13
        if dep > 0 and len(last) >= dep:
            px, py = last[dep - 1]
            svg.append(f"<path d='M{px + 3} {py + 6} V{cy} H{tx - 3}' fill='none' stroke='#a9b4c2' stroke-width='1'/>")
        del last[dep:]
        last.append((tx, cy))
        if isinstance(i, tuple):
            svg.append(f"<text x='{tx}' y='{cy + 4}' {FONT} font-size='11' fill='#94a3b8' font-style='italic'>… {i[1]} more</text>")
            continue
        e = E[i]
        has_feature_corr = any((i in (a, b)) for a, b, t in CORR)
        detail = not has_feature_corr
        col = '#64748b' if detail else '#0f172a'
        style = "font-style='italic'" if detail else ''
        svg.append(f"<text x='{tx}' y='{cy + 4}' {FONT} font-size='11.5' fill='{col}' {style}>{escape(label(i))}</text>")
        if hidden_corr.get(i):
            svg.append(f"<text x='{x + w - 8}' y='{cy + 4}' {FONT} font-size='9.5' fill='#94a3b8' text-anchor='end'>+{hidden_corr[i]}</text>")
        pos[i] = (x, x + w, cy, tx)

# literal node
lx, ly, lw = 20, 70, 330
svg.append(f"<rect x='{lx}' y='{ly}' width='{lw}' height='46' rx='8' fill='#f8fafc' stroke='#94a3b8' stroke-dasharray='4 3'/>")
svg.append(f"<text x='{lx + PADX}' y='{ly + 18}' {FONT} font-size='12' font-weight='700' fill='#0f172a'>uvlPackage.Literals.UVL_MODEL</text>")
svg.append(f"<text x='{lx + PADX}' y='{ly + 34}' {FONT} font-size='10.5' fill='#475569'>EClass of UVL.ecore (as uvl2uvl stores it)</text>")
pos[LIT] = (lx, lx + lw, ly + 23, lx)

# ------------------------------------------------------------------ arrows
COLX = {f: c[0] for f, c in COLS.items()}
drawn = []
for a, b, t in CORR:
    if not ((a in SHOWN or a == LIT) and (b in SHOWN or b == LIT)):
        continue
    drawn.append((a, b, t))
loop_slot = {}
arrows = []
for a, b, t in drawn:
    ax0, ax1, ay, _ = pos[a]
    bx0, bx1, by, _ = pos[b]
    if ax0 == bx0:                               # same column: loop on the right side
        k = loop_slot.get(ax0, 0); loop_slot[ax0] = k + 1
        off = 34 + 16 * k
        p = f"M{ax1} {ay} C{ax1 + off} {ay} {bx1 + off} {by} {bx1 + 2} {by}"
    elif ax0 < bx0 and bx0 - ax1 > 300:          # skips a column: route through the gap between the boxes in between
        ch = 400
        p = (f"M{ax1} {ay} C{ax1 + 50} {ay} {ax1 + 40} {ch} {ax1 + 90} {ch} L{bx0 - 90} {ch} "
             f"C{bx0 - 40} {ch} {bx0 - 50} {by} {bx0 - 2} {by}")
    elif ax0 < bx0:                              # left -> right
        mid = (ax1 + bx0) / 2
        p = f"M{ax1} {ay} C{mid} {ay} {mid} {by} {bx0 - 2} {by}"
    else:                                        # right -> left
        mid = (ax0 + bx1) / 2
        p = f"M{ax0} {ay} C{mid} {ay} {mid} {by} {bx1 + 2} {by}"
    arrows.append(f"<path d='{p}' fill='none' stroke='#ffffff' stroke-width='5.5' stroke-linecap='round'/>")   # halo
    arrows.append(f"<path d='{p}' fill='none' stroke='{COLOR[t]}' stroke-width='2' {DASH[t]} marker-end='url(#h{['', 'overlap', 'requirement'].index(t)})'/>")
    arrows.append(f"<circle cx='{p.split()[0][1:]}' cy='{ay}' r='2.6' fill='{COLOR[t]}'/>")

# ------------------------------------------------------------------ legend + title
W, H = 1660, 860
title = [f"<text x='20' y='30' {FONT} font-size='18' font-weight='700' fill='#0f172a'>KA-RaceIng initial state – slice: hydrogen tank (DES) and hydrogen shutdown circuit (ELEC)</text>",
         f"<text x='20' y='50' {FONT} font-size='11.5' fill='#475569'>Rows are instances (name : Class) read from the persisted VSUM; arrows are all {len(drawn)} stored correspondences between the shown instances (from left to right object).</text>"]
ly0 = 760
leg = [f"<rect x='20' y='{ly0 - 18}' width='1610' height='96' rx='8' fill='#ffffff' stroke='#cbd5e1'/>",
       f"<text x='34' y='{ly0}' {FONT} font-size='12' font-weight='700' fill='#0f172a'>Legend</text>"]
items = [('', "tag \"\"  – feature ↔ realising element, domain root ↔ model root, feature ↔ configuration feature, UVL model ↔ configuration model / UVL_MODEL"),
         ('overlap', "tag \"overlap\"  – semantically overlapping artefacts (interface constraint, scenario overlap, CAD part ↔ BOM item)"),
         ('requirement', "tag \"requirement\"  – requirement ↔ artefact realising the feature the requirement is mapped to")]
for k, (t, txt) in enumerate(items):
    yy = ly0 + 20 + k * 18
    leg.append(f"<line x1='34' y1='{yy - 4}' x2='84' y2='{yy - 4}' stroke='{COLOR[t]}' stroke-width='1.8' {DASH[t]} marker-end='url(#h{k})'/>")
    leg.append(f"<text x='96' y='{yy}' {FONT} font-size='11.5' fill='#0f172a'>{escape(txt)}</text>")
leg.append(f"<text x='1060' y='{ly0 + 20 - 0}' {FONT} font-size='11.5' fill='#64748b' font-style='italic'>grey italic: instance without any correspondence (group, value object; every artefact has a feature)</text>")
leg.append(f"<text x='1060' y='{ly0 + 38}' {FONT} font-size='11.5' fill='#64748b'>+n: further correspondences of this instance to instances not shown</text>")
leg.append(f"<text x='1060' y='{ly0 + 56}' {FONT} font-size='11.5' fill='#64748b'>… n more: further contained instances not shown</text>")
leg.append(f"<path d='M640 {ly0 - 10} V{ly0 - 1} H656' fill='none' stroke='#a9b4c2' stroke-width='1.2'/>")
leg.append(f"<text x='664' y='{ly0}' {FONT} font-size='11.5' fill='#0f172a'>containment (inside one model) – arrows are correspondences (VSUM correspondence model, ● left object → right object)</text>")

defs = "<defs>" + ''.join(
    f"<marker id='h{k}' viewBox='0 0 10 10' refX='9' refY='5' markerUnits='userSpaceOnUse' markerWidth='10' markerHeight='10' orient='auto-start-reverse'><path d='M0 0 L10 5 L0 10 z' fill='{COLOR[t]}'/></marker>"
    for k, t in enumerate(['', 'overlap', 'requirement'])) + "</defs>"
out = (f"<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 {W} {H}' width='{W}' height='{H}'>"
       f"<rect width='{W}' height='{H}' fill='#ffffff'/>{defs}" + ''.join(title + svg + arrows + leg) + "</svg>")
open(sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(os.path.dirname(HERE)), 'docs', 'initial_state_slice.svg'), 'w').write(out)
print('rows', {f: len(rows_of(f)) for f in COLS}, 'arrows', len(drawn), {t: sum(1 for *_, x in drawn if x == t) for t in ['', 'overlap', 'requirement']})
