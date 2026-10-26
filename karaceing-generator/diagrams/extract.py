"""Extract all instances and correspondences of the persisted initial state to data.json (input of the diagrams)."""
import json, os, xml.etree.ElementTree as ET
from pyecore.resources import ResourceSet, URI
HERE = os.path.dirname(os.path.abspath(__file__))
T = os.path.dirname(os.path.dirname(HERE))
E=T+'/model/src/main/ecore'; S=T+'/vsum/src/test/resources/initialstate'
rs=ResourceSet()
for f in ['UVL','configurationsMM','cad','requirements','thermal','testbench','electrical','bom','brakesystem','simuLink']:
    p=rs.get_resource(URI(f'{E}/{f}.ecore')).contents[0]; rs.metamodel_registry[p.nsURI]=p
FILES=['karaceing.uvl','karaceing.config','prj.requirements','des.cad','crash.cad','kin.cad','aero.cad','elec.electrical','sim.simulink','thermo.thermal','val.testbench','karaceing.bom','des.brakesystem']
els=[]; byfrag={}
def segs(o):
    out=[]
    while o.eContainer() is not None:
        f=o.eContainmentFeature(); c=o.eContainer()
        out.append(f'@{f.name}.{list(c.eGet(f)).index(o)}' if f.many else f'@{f.name}'); o=c
    return list(reversed(out))
def nm(o):
    for a in ('name','id','instanceName'):
        if o.eClass.findEStructuralFeature(a) is not None and getattr(o,a,None) not in (None,''): return str(getattr(o,a))
    return None
for fi,fn in enumerate(FILES):
    r=rs.get_resource(URI(f'{S}/{fn}')); root=r.contents[0]
    idx={}
    for o in [root]+list(root.eAllContents()):
        frag='//'+'/'.join(segs(o)) if o.eContainer() is not None else '/'
        i=len(els); idx[id(o)]=i
        p=idx.get(id(o.eContainer())) if o.eContainer() is not None else None
        extra={}
        if o.eClass.name=='Feature' and fn=='karaceing.config': extra['sel']=bool(o.isSelected)
        if o.eClass.name=='Configuration': extra['name']=['EV3','CV2','CV1'][list(o.eContainer().configuration).index(o)]
        els.append(dict(m=fi,p=p,c=o.eClass.name,n=extra.get('name') or nm(o),fr=frag,**({'s':extra['sel']} if 'sel' in extra else {})))
        byfrag[(fn,frag)]=i
# UVL_MODEL literal pseudo element
corr=[]
for c in ET.parse(f'{S}/vsum/correspondences.correspondence').getroot():
    ends=[]
    for s in c:
        h=s.get('href')
        if h.startswith('http'): ends.append(-1); continue
        f,frag=h.replace('../','').split('#')
        ends.append(byfrag[(f,frag)])
    corr.append([ends[0],ends[1],c.get('tag') or ''])
json.dump(dict(files=FILES,els=els,corr=corr),open(os.path.join(HERE, 'data.json'),'w'),separators=(',',':'))
print(len(els),len(corr),os.path.getsize(os.path.join(HERE, 'data.json')))
