"""Minimal, self-contained UVL reader + SAT-based analyser (no external deps).
Supports the UVL subset used in this case study:
  namespace / imports (alias) / include / features (tab-indented tree,
  groups mandatory|optional|alternative|or, {abstract, key 'val'} attributes,
  typed features Integer|Real) / constraints (! & | => <=> parentheses,
  arithmetic comparisons on typed features: == != < > <= >=).
"""
import re, os, itertools, sys
sys.setrecursionlimit(100000)

GROUPS = ('mandatory', 'optional', 'alternative', 'or')


class Model:
    def __init__(self):
        self.namespace = None
        self.imports = {}          # alias -> namespace
        self.features = {}         # name -> dict(parent, group, abstract, type, attrs)
        self.root = None
        self.constraints = []      # raw strings
        self.order = []


def parse_uvl(path, prefix=''):
    m = Model()
    lines = open(path, encoding='utf-8').read().split('\n')
    section = None
    stack = []   # (indent, kind, name)  kind in feature|group
    for ln, raw in enumerate(lines, 1):
        if not raw.strip() or raw.strip().startswith('//'):
            continue
        indent = len(raw) - len(raw.lstrip('\t'))
        if raw[indent:indent + 1] == ' ':
            raise SyntaxError(f'{path}:{ln}: use tabs for indentation')
        s = raw.strip()
        if indent == 0:
            if s.startswith('namespace'):
                m.namespace = s.split()[1]; section = None; continue
            if s in ('imports', 'include', 'features', 'constraints'):
                section = s; stack = []; continue
            raise SyntaxError(f'{path}:{ln}: unexpected top-level "{s}"')
        if section == 'imports':
            mm = re.fullmatch(r'([\w.]+)\s+as\s+(\w+)', s)
            if not mm: raise SyntaxError(f'{path}:{ln}: bad import')
            m.imports[mm.group(2)] = mm.group(1); continue
        if section == 'include':
            continue
        if section == 'constraints':
            m.constraints.append(s); continue
        if section == 'features':
            while stack and stack[-1][0] >= indent:
                stack.pop()
            if s in GROUPS:
                if not stack or stack[-1][1] != 'feature':
                    raise SyntaxError(f'{path}:{ln}: group without parent feature')
                stack.append((indent, 'group', s, (stack[-1][2], ln))); continue
            mm = re.fullmatch(r"(?:(Integer|Real|String|Boolean)\s+)?([\w.]+)\s*(\{.*\})?", s)
            if not mm: raise SyntaxError(f'{path}:{ln}: bad feature line "{s}"')
            ftype, name, attrs = mm.groups()
            ad = {}
            if attrs:
                for part in re.findall(r"\w+(?:\s+'[^']*'|\s+[\d.]+)?", attrs[1:-1]):
                    kv = part.split(None, 1)
                    ad[kv[0]] = kv[1].strip("'") if len(kv) > 1 else True
            full = name if '.' in name else prefix + name
            if full in m.features:
                raise SyntaxError(f'{path}:{ln}: duplicate feature {full}')
            if stack:
                if stack[-1][1] != 'group':
                    raise SyntaxError(f'{path}:{ln}: feature {name} must be inside a group')
                parent = stack[-2][2]; group = stack[-1][2]; gid = stack[-1][3]
            else:
                if m.root: raise SyntaxError(f'{path}:{ln}: second root')
                parent, group, gid = None, None, None
                m.root = full
            m.features[full] = dict(parent=parent, group=group, gkey=gid,
                                    abstract=bool(ad.get('abstract')), type=ftype, attrs=ad, ref='.' in name)
            m.order.append(full)
            stack.append((indent, 'feature', full))
    return m


# ------------------------------------------------------------ expressions
TOK = re.compile(r"\s*(<=>|=>|==|!=|<=|>=|[!&|()<>+\-*/]|\d+(?:\.\d+)?|[\w.]+)")


def tokenize(s):
    pos, out = 0, []
    s = s.strip()
    while pos < len(s):
        mm = TOK.match(s, pos)
        if not mm: raise SyntaxError(f'bad token in constraint: {s[pos:]}')
        out.append(mm.group(1)); pos = mm.end()
    return out


def parse_expr(s):
    toks = tokenize(s); i = [0]
    def peek(): return toks[i[0]] if i[0] < len(toks) else None
    def take(): t = toks[i[0]]; i[0] += 1; return t
    def equiv():
        a = impl()
        while peek() == '<=>': take(); a = ('iff', a, impl())
        return a
    def impl():
        a = orr()
        if peek() == '=>': take(); a = ('imp', a, impl())
        return a
    def orr():
        a = andd()
        while peek() == '|': take(); a = ('or', a, andd())
        return a
    def andd():
        a = neg()
        while peek() == '&': take(); a = ('and', a, neg())
        return a
    def neg():
        if peek() == '!': take(); return ('not', neg())
        return comp()
    def comp():
        a = arith()
        if peek() in ('==', '!=', '<', '>', '<=', '>='):
            op = take(); b = arith(); return ('cmp', op, a, b)
        return a
    def arith():
        a = term()
        while peek() in ('+', '-'): op = take(); a = ('ar', op, a, term())
        return a
    def term():
        a = atom()
        while peek() in ('*', '/'): op = take(); a = ('ar', op, a, atom())
        return a
    def atom():
        t = take()
        if t == '(':
            e = equiv()
            if take() != ')': raise SyntaxError('missing )')
            return e
        if re.fullmatch(r'\d+(?:\.\d+)?', t): return ('num', float(t))
        return ('var', t)
    e = equiv()
    if peek() is not None: raise SyntaxError(f'trailing tokens in "{s}"')
    return e


def is_arith(e):
    return e[0] in ('cmp',)


# ------------------------------------------------------------ CNF + SAT
class CNF:
    def __init__(self):
        self.var = {}; self.clauses = []; self.n = 0
    def v(self, name):
        if name not in self.var:
            self.n += 1; self.var[name] = self.n
        return self.var[name]
    def fresh(self):
        self.n += 1; return self.n
    def add(self, *cl): self.clauses.append(list(cl))

    def tseitin(self, e, arith_atoms):
        k = e[0]
        if k == 'var': return self.v(e[1])
        if k == 'cmp':
            # arithmetic comparison: abstract boolean atom, evaluated on configurations
            key = 'ARITH::' + repr(e); arith_atoms[key] = e
            return self.v(key)
        if k == 'not': return -self.tseitin(e[1], arith_atoms)
        a = self.tseitin(e[1], arith_atoms); b = self.tseitin(e[2], arith_atoms); x = self.fresh()
        if k == 'and': self.add(-x, a); self.add(-x, b); self.add(x, -a, -b)
        elif k == 'or': self.add(-x, a, b); self.add(x, -a); self.add(x, -b)
        elif k == 'imp': self.add(-x, -a, b); self.add(x, a); self.add(x, -b)
        elif k == 'iff': self.add(-x, -a, b); self.add(-x, a, -b); self.add(x, a, b); self.add(x, -a, -b)
        else: raise ValueError(k)
        return x


def solve(clauses, assumptions=()):
    """small DPLL with unit propagation; returns model dict or None"""
    assign = {}
    for a in assumptions:
        if assign.get(abs(a), a > 0) != (a > 0): return None
        assign[abs(a)] = a > 0
    cls = [c[:] for c in clauses]
    def val(l):
        v = assign.get(abs(l));
        return None if v is None else (v if l > 0 else not v)
    def propagate():
        changed = True
        while changed:
            changed = False
            for c in cls:
                unassigned = []; sat = False
                for l in c:
                    x = val(l)
                    if x is True: sat = True; break
                    if x is None: unassigned.append(l)
                if sat: continue
                if not unassigned: return False
                if len(unassigned) == 1:
                    l = unassigned[0]; assign[abs(l)] = l > 0; changed = True
        return True
    def rec():
        if not propagate(): return False
        for c in cls:
            if not any(val(l) is True for l in c):
                for l in c:
                    if val(l) is None:
                        snap = dict(assign)
                        assign[abs(l)] = l > 0
                        if rec(): return True
                        assign.clear(); assign.update(snap)
                        assign[abs(l)] = l < 0
                        return rec()
        return True
    return dict(assign) if rec() else None


# ------------------------------------------------------------ semantics
def load_composite(files):
    """files: list of (path, alias_prefix). Returns merged features, constraints"""
    feats, cons, roots = {}, [], {}
    for path, pre in files:
        m = parse_uvl(path, pre)
        for f, d in m.features.items():
            if d['ref']:
                continue  # reference to imported root - handled by composition
            feats[f] = d
        roots[pre] = m.root
        for c in m.constraints:
            # qualify unqualified names with prefix
            e = parse_expr(c)
            cons.append((qualify(e, pre, m.imports), c, path))
        if pre == '':
            comp = m
    return feats, cons, roots


def qualify(e, pre, imports):
    k = e[0]
    if k == 'var':
        n = e[1]
        if '.' in n: return e
        return ('var', pre + n)
    if k == 'num': return e
    if k == 'not': return ('not', qualify(e[1], pre, imports))
    if k in ('cmp', 'ar'): return (k, e[1], qualify(e[2], pre, imports), qualify(e[3], pre, imports))
    return (k, qualify(e[1], pre, imports), qualify(e[2], pre, imports))


def build_cnf(feats, cons, extra_tree=()):
    cnf = CNF(); arith = {}
    for f in feats: cnf.v(f)
    groups = {}
    root_feats = [f for f, d in feats.items() if d['parent'] is None]
    for f, d in feats.items():
        if d['parent'] is None: continue
        p = d['parent']
        cnf.add(-cnf.v(f), cnf.v(p))                    # child => parent
        groups.setdefault(d['gkey'], (p, d['group'], []))[2].append(f)
    for (p, g, ch) in groups.values():
        P = cnf.v(p)
        if g == 'mandatory':
            for c in ch: cnf.add(-P, cnf.v(c))
        elif g in ('alternative', 'or'):
            cnf.add(-P, *[cnf.v(c) for c in ch])
            if g == 'alternative':
                for a, b in itertools.combinations(ch, 2): cnf.add(-cnf.v(a), -cnf.v(b))
    for parent, child, gtype in extra_tree:   # composition: imported roots
        cnf.add(-cnf.v(child), cnf.v(parent))
        if gtype == 'mandatory': cnf.add(-cnf.v(parent), cnf.v(child))
    for e, raw, src in cons:
        cnf.add(cnf.tseitin(e, arith))
    return cnf, arith, groups


def eval_arith(e, values):
    k = e[0]
    if k == 'num': return e[1]
    if k == 'var': return values[e[1]]
    if k == 'ar':
        a, b = eval_arith(e[2], values), eval_arith(e[3], values)
        return {'+': a + b, '-': a - b, '*': a * b, '/': a / b}[e[1]]
    if k == 'cmp':
        a, b = eval_arith(e[2], values), eval_arith(e[3], values)
        return {'==': a == b, '!=': a != b, '<': a < b, '>': a > b, '<=': a <= b, '>=': a >= b}[e[1]]
    raise ValueError(k)
