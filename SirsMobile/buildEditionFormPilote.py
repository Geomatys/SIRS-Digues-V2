from pyecore.resources import ResourceSet, URI, global_registry
import pyecore.ecore as Ecore  # We get a reference to the Ecore metamodle implem.
import json


class LabelLoader:
    def __init__(self, path_to_property_files):
        self.propertieDictionnary = {}
        propertiesFileName = glob.glob(path_to_property_files)

    	# iterate on file properties
    	for path in propertiesFileName:
            filestr = path.split('/')[1]
            clazz = filestr.split('.')[0]
            self.propertieDictionnary[clazz] = {}

            with open(path) as propertieFile:
                propertieFileStr = propertieFile.read()
                rows = propertieFileStr.split('\n')[2:-1]
            for row in rows:
                r = row.split('=')
                self.propertieDictionnary[clazz][r[0]] = r[1]

            # Does not exist in properties file
            self.propertieDictionnary[clazz]["_id"] = "_id (ne pas modifier/supprimer)"



def is_general_attribute(name):
    return name in ["geometry", "date_debut", "date_fin", "dateMaj", "commentaire", "designation", "valid", "author"]


class WrapAttribute(object):
    def __init__(self, clazz, attribute: Ecore.EAttribute):
        self.name = attribute.name
        self.type = attribute.eType.name
        try:
            self.label = to_label(clazz, attribute.name)
        except KeyError:
            self.label = ""
        self.reference = False
        self.min = 0 if attribute.eType.name == "EFloat" or attribute.eType.name == "EInt" else None


class WrapReference(object):
    def __init__(self, clazz, reference: Ecore.EReference):
        self.name = reference.name
        self.type = reference.eType.name
        try:
            self.label = to_label(clazz, reference.name)
        except KeyError:
            self.label = ""
        self.reference = True
        self.multiple = reference.upper
        self.containment = reference.containment


def transform(classifier: Ecore.EClassifier, result, clazz):
    if type(classifier) == Ecore.EClass:
        super_types: Ecore.EOrderedSet = classifier.eAllSuperTypes()
        for st in super_types:
            transform(st, result, clazz)
        content = list(classifier.eAllContents())
        for obj in content:
            if type(obj) == Ecore.EAttribute:
                a = WrapAttribute(clazz, obj)
                if not is_general_attribute(a.name):
                    result[clazz][a.name] = a.__dict__
            if type(obj) == Ecore.EReference:
                r = WrapReference(clazz, obj)
                if not is_general_attribute(r.name):
                    result[clazz][r.name] = r.__dict__


def read_graph(graph: Ecore.EPackage, clazz):
    classifier: Ecore.EClassifier = graph.getEClassifier(clazz)
    transform(classifier, result, clazz)
    print(json.dumps(result, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    arg1 = "AmenagementHydraulique"
    plugin = "dependance"
    path_to_ecore = "../plugin-" + plugin + "/model/" + plugin + ".ecore"
    path_to_properties = "../plugin-" + plugin + "/src/main/resources/fr/sirs/core/model"
    core_path = "../sirs-core/model/sirs.ecore"
    global_registry[Ecore.nsURI] = Ecore  # We load the Ecore metamodel first
    rset = ResourceSet()
    resource = rset.get_resource(URI(path_to_ecore))
    graphMMRoot = resource.contents[0]  # We get the root (an EPackage here)
    read_graph(graphMMRoot, arg1)
