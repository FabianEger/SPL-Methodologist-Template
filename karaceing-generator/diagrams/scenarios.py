"""Build scenarios.json: how each concrete evolution scenario changes the initial state.

Inputs:
  * the scenario tests (vsum/src/test/java/.../karaceing/Scenario*Test.java): id, title, description, status;
  * the snapshots written by the tests with -Dkaraceing.snapshotDir=<dir> (state after the test +
    propagation.json, the change propagation log);
  * the initial state.
Run:  mvn ... -Dtest='Scenario*Test' -Dkaraceing.snapshotDir=<dir>   then   python3 scenarios.py <dir>
"""
import json, os, re, sys, html
import states

HERE = states.HERE
TEMPLATE = states.TEMPLATE
TESTS = os.path.join(TEMPLATE, 'vsum', 'src', 'test', 'java', 'tools', 'vitruv', 'methodologisttemplate', 'vsum', 'karaceing')
INIT = os.path.join(TEMPLATE, 'vsum', 'src', 'test', 'resources', 'initialstate')


def clean_javadoc(t):
    t = re.sub(r'^\s*/\*\*|\*/\s*$', '', t.strip())
    t = '\n'.join(re.sub(r'^\s*\* ?', '', l) for l in t.splitlines())
    t = re.sub(r'\{@code ([^}]*)\}', r'\1', t)
    t = re.sub(r'\{@link (?:[\w.#]+ )?([^}]*)\}', r'\1', t)
    t = re.sub(r'<p>', '\n\n', t)
    t = re.sub(r'<[^>]+>', '', t)
    t = html.unescape(t)
    return re.sub(r'[ \t]+', ' ', re.sub(r'(?<!\n)\n(?!\n)', ' ', t)).strip()


TEST_CLASSES = [(k, f'Scenario{k}Test') for k in range(1, 7)] + [(7, 'ReportScenarioTest')]   # 7: TechReport scenarios R1-R10


def parse_tests():
    out, families = [], {}
    for k, cls in TEST_CLASSES:
        src = open(os.path.join(TESTS, f'{cls}.java')).read()
        m = re.search(r'(/\*\*(?:(?!\*/).)*\*/)\s*(?:@Disabled\("([^"]*)"\)\s*)?public class', src, re.S)
        families[k] = dict(text=clean_javadoc(m.group(1)), disabled=m.group(2))
        for t in re.finditer(r'(/\*\*(?:(?!\*/).)*\*/)\s*@Test\s*(?:@Disabled\("([^"]*)"\)\s*)?@DisplayName\("([^"]*)"\)\s*public void (\w+)\(\)', src, re.S):
            doc, method_disabled, title, method = t.groups()
            sid = title.split(' ')[0]
            text = clean_javadoc(doc)
            ttl = title[len(sid):].strip()
            if text.startswith(sid):                       # the Javadoc starts with id and title: keep only what follows
                rest = text[len(sid):].lstrip()
                if rest.startswith(ttl):
                    rest = rest[len(ttl):].lstrip(' .')
                    rest = rest[1:].lstrip() if rest.startswith(':') else rest
                text = rest
            out.append(dict(id=sid, family=k, title=title[len(sid):].strip(), method=f'{cls}.{method}',
                            text=text, disabled=families[k]['disabled'] or method_disabled))
    return out, families


# ------------------------------------------------------------------ propagation log -> readable steps
def lab(o):
    if o is None:
        return '?'
    if isinstance(o, dict):
        return f"{o.get('name') or ''} : {o.get('cls')}".strip()
    return str(o)


def where(o):
    return o.get('file') if isinstance(o, dict) else None


def summarise(changes):
    """Atomic changes -> short sentences (create + insert + set name merged)."""
    created = {}
    lines = []
    for c in changes:
        t = c['type']
        el = c.get('affectedElement')
        if t == 'CreateEObject':
            created[json.dumps(el, sort_keys=True)] = True
            continue
        if t in ('InsertEReference', 'InsertRootEObject'):
            nv = c.get('newValue')
            key = json.dumps(nv, sort_keys=True)
            if key in created and c.get('affectedFeature'):
                lines.append(('add', f"add {lab(nv)} to {lab(el)}.{c['affectedFeature']}", where(nv) or where(el)))
                continue
            if t == 'InsertRootEObject':
                lines.append(('add', f"add root {lab(nv)}", where(nv)))
            else:
                lines.append(('ref', f"{lab(el)}.{c['affectedFeature']} += {lab(nv)}", where(el)))
            continue
        if t in ('RemoveEReference', 'RemoveRootEObject'):
            ov = c.get('oldValue')
            lines.append(('del', f"remove {lab(ov)} from {lab(el)}.{c.get('affectedFeature', 'root')}", where(el) or where(ov)))
            continue
        if t == 'DeleteEObject':
            lines.append(('del', f"delete {lab(el)}", where(el)))
            continue
        if t.startswith('Replace') or t.startswith('Insert') or t.startswith('Remove') or t == 'UnsetFeature':
            f = c.get('affectedFeature')
            if t == 'ReplaceSingleValuedEAttribute' and f in ('name', 'id', 'label') and json.dumps(el, sort_keys=True) in created:
                continue
            nv = c.get('newValue')
            ov = c.get('oldValue')
            val = lab(nv) if isinstance(nv, dict) else nv
            old = lab(ov) if isinstance(ov, dict) else ov
            lines.append(('set', f"{lab(el)}.{f} = {val}" + (f" (was {old})" if old not in (None, 'null') else ''), where(el)))
            continue
        lines.append(('other', f"{t} {lab(el)}", where(el)))
    # drop duplicates, keep order
    seen, out = set(), []
    for l in lines:
        if l not in seen:
            seen.add(l); out.append(l)
    return out


def corr_end(o):
    if isinstance(o, dict):
        return f"{lab(o)} ({o.get('file') or 'deleted'})"
    return 'uvlPackage.Literals.UVL_MODEL' if o == 'UVLModel' else str(o)


def steps_of(folder):
    p = os.path.join(folder, 'propagation.json')
    if not os.path.exists(p):
        return []
    out = []
    for entry in json.load(open(p)):
        # {"hops": [...], "corrAdded": [...], "corrRemoved": [...]} (older logs: only the list of hops)
        commit = entry['hops'] if isinstance(entry, dict) else entry
        hops = []
        seen = set()            # Vitruvius attaches the dialogs of a propagation to several hops: list each once
        for pc in commit:
            if 'error' in pc:
                hops.append(dict(error=pc['error'])); continue
            inter = []
            for i in pc.get('interactions', []):
                ch = i.get('choices') or []
                idx = int(i['selectedIndex']) if 'selectedIndex' in i else None
                ans = ch[idx] if idx is not None and 0 <= idx < len(ch) else i.get('selectedIndex')
                key = (i.get('message'), ans)
                if key in seen:
                    continue
                seen.add(key)
                inter.append(dict(q=i.get('message'), a=ans, n=len(ch)))
            hops.append(dict(original=summarise(pc['original']), consequential=summarise(pc['consequential']), dialogs=inter))
        corr = lambda cs: [[c.get('tag', ''), corr_end(c.get('left')), corr_end(c.get('right'))] for c in cs if 'error' not in c]
        notes = []
        for msg in (entry.get('notifications', []) if isinstance(entry, dict) else []):
            m = re.match(r'\[Notify \u2192 ([^\]]+)\] (.*)', msg, re.S)
            notes.append([m.group(1), m.group(2)] if m else ['', msg])
        out.append(dict(hops=hops, corr_added=corr(entry.get('corrAdded', []) if isinstance(entry, dict) else []),
                        corr_removed=corr(entry.get('corrRemoved', []) if isinstance(entry, dict) else []), notes=notes))
    return out


# ------------------------------------------------------------------ diff -> nodes and edges for the page
def build(snap_dir, init_index):
    tests, families = parse_tests()
    init = states.load_state(INIT)
    eb = init[0]
    out = []
    for t in tests:
        folder = os.path.join(snap_dir, t['method'])
        entry = dict(t)
        if t['disabled'] or not os.path.isdir(folder):
            entry['status'] = 'disabled' if t['disabled'] else 'missing'
            out.append(entry); continue
        after = states.load_state(folder)
        ea = after[0]
        d = states.diff(init, after)
        st = {}
        for u in d['added']: st[u] = 'add'
        for u in d['removed']: st[u] = 'del'
        for u in d['moved']: st.setdefault(u, 'mov')
        chg = {}
        for u, ch in d['changed']:
            st.setdefault(u, 'chg'); chg[u] = {k: [b, a] for k, (b, a) in ch.items()}
        show = set(st)
        for a, b, _ in d['corr_added'] + d['corr_removed']:
            show.update(x for x in (a, b) if x != states.LITERAL)

        def el(u):
            return ea.get(u) or eb.get(u)

        def parent(u):
            e = ea.get(u)
            return e['parent'] if e is not None else eb[u]['parent']
        for u in list(show):
            p = parent(u)
            while p is not None:
                show.add(p); p = parent(p)
        # moved elements: also show the former container chain
        for u in d['moved']:
            p = eb[u]['parent']
            while p is not None:
                show.add(p); p = eb[p]['parent'] if p in eb else None
        keys = {}
        nodes = []

        def key(u):
            if u == states.LITERAL:
                return 'L'
            if u not in keys:
                keys[u] = init_index[(eb[u]['file'], eb[u]['frag'])] if u in eb else f'n{len(keys)}'
            return keys[u]
        order = {f: i for i, f in enumerate(states.FILES)}
        for u in show:
            e = el(u)
            n = dict(k=key(u), f=order[e['file']], c=e['cls'], n=e['name'], p=None if parent(u) is None else key(parent(u)), s=st.get(u, 'ctx'))
            if u in d['moved']:
                n['op'] = key(eb[u]['parent'])
            if u in chg:
                n['a'] = chg[u]
            if e['cls'] == 'Feature' and e['file'] == 'karaceing.config' and 'isSelected' in e['attrs']:
                n['sel'] = e['attrs']['isSelected'] == 'true'
            # position among siblings (after state, else before)
            src = ea if u in ea else eb
            sib = [x for x, y in src.items() if y['parent'] == src[u]['parent'] and y['file'] == src[u]['file']] if src[u]['parent'] else []
            n['i'] = sib.index(u) if u in sib else 0
            nodes.append(n)
        edges = [[key(a), key(b), t_, 'add'] for a, b, t_ in d['corr_added']] + [[key(a), key(b), t_, 'del'] for a, b, t_ in d['corr_removed']]
        # instances that gain or lose correspondences; existing, otherwise unchanged ones are marked as re-linked
        cc = {}
        for a, b, _, st_ in edges:
            for x in (a, b):
                cc.setdefault(x, [0, 0])[0 if st_ == 'add' else 1] += 1
        for n in nodes:
            if n['k'] in cc:
                n['cc'] = cc[n['k']]
                if n['s'] == 'ctx':
                    n['s'] = 'lnk'
        # unchanged correspondences of changed instances (context), from the state after the scenario
        changed = {u for u, s in st.items() if s in ('chg', 'mov')}
        for a, b, t_ in after[1]:
            if (a in changed or b in changed) and a in show and b in show and [key(a), key(b), t_, 'add'] not in edges:
                edges.append([key(a), key(b), t_, 'ctx'])
        entry.update(status='executed', nodes=nodes, edges=edges, steps=steps_of(folder),
                     counts=dict(added=len(d['added']), removed=len(d['removed']), moved=len(d['moved']), changed=len(d['changed']),
                                 corr_added=len(d['corr_added']), corr_removed=len(d['corr_removed']),
                                 relinked=sum(1 for n in nodes if n['s'] == 'lnk')))
        out.append(entry)
    return dict(families={str(k): v for k, v in families.items()}, scenarios=out)


if __name__ == '__main__':
    snap = sys.argv[1]
    data = json.load(open(os.path.join(HERE, 'data.json')))
    E, F = data['els'], data['files']
    # (file, fragment) -> index of the instance in data.json (same order as extract.py)
    frag_index = {(F[e['m']], e['fr']): i for i, e in enumerate(E)}
    res = build(snap, frag_index)
    json.dump(res, open(os.path.join(HERE, 'scenarios.json'), 'w'), separators=(',', ':'))
    ex = [s for s in res['scenarios'] if s['status'] == 'executed']
    print(len(res['scenarios']), 'scenarios,', len(ex), 'executed;', sum(len(s['nodes']) for s in ex), 'nodes,', sum(len(s['edges']) for s in ex), 'edges')
