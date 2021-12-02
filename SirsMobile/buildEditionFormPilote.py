from pyecore.resources import ResourceSet, URI, global_registry
import pyecore.ecore as Ecore  # We get a reference to the Ecore metamodle implem.
import json
import sys
import glob
import os


class LabelLoader:
    def __init__(self, properties_paths=None):
        self.propertieDictionnary = {}
        self.classes = []
        if properties_paths is not None:
            for p in properties_paths:
                self.complete_dict(p)

    def complete_dict(self, path_to_property_files):
        propertiesFileName = glob.glob(path_to_property_files)

        # iterate on file properties
        for path in propertiesFileName:
            if os.path.isdir(path):
                continue
            filestr = path.split('/')[-1]
            clazz = filestr.split('.')[0]
            self.propertieDictionnary[clazz] = {}
            self.classes.append(clazz)


            with open(path) as propertieFile:
                propertieFileStr = propertieFile.read()
                rows = propertieFileStr.split('\n')[2:-1]
            for row in rows:
                r = row.split('=')
                self.propertieDictionnary[clazz][r[0]] = r[1]

            # Does not exist in properties file
            self.propertieDictionnary[clazz]["_id"] = "_id (ne pas modifier/supprimer)"

    def to_label(self, clazz, attribute):
        try:
            return self.propertieDictionnary[clazz][attribute]
        except KeyError:
            return ""


def check_argument():
    plugins = ['dependance', 'aot-cot', 'lit', 'carto', 'berge', 'reglementary', 'vegetation', 'core']
    ecore_paths = []
    properties_paths = []
    P = "all"
    argv = sys.argv

    if argv[-1] == "--help":
        print("### Description")
        print("Ce script permet de créer le fichier formTemplatePilote.json à partir des différents diagrammes ecore de l'application.")
        print("formTemplatePilote.json contient l'objet formTemplatePilote utilisé par l'application mobile dans le service formstemplate.service.ts .")
        print("formTemplatePilote est une feuille de route qui permet de générer les composants des formulaires d'édition de l'application mobile.")
        print("formTemplatePilote, dans le service formstemplate.service.ts, doit être mis à jour à chaque modification du modèle SIRS.")
        print("Attention, actuellement ce fonctionnement ne concerne que les classes:")
        print("\tCore")
        print("\t\tPrestation")
        print("\tPlugin-dependance")
        print("\t\tAmenagementHydraulique")
        print("\t\tDesordreDependance")
        print("\t\tPrestationAmenagementHydraulique")
        print("\t\tStructureAmenagementHydraulique")
        print("\t\tOuvrageAssocieAmenagementHydraulique")
        print("\t\tOrganeProtectionCollective")
        print("")
        print("### Usage")
        print("Ce script doit être lancé depuis le dossier SirsMobile qui doit se trouver à la racine du projet Sirs.")
        print("Sans option, ce script génére TOUS les objets pilotes de TOUS les plugins et du coeur.")
        print("Il est possible de limiter les classes concernées à un plugin (ou coeur):")
        print("-p <nom_du_plugin> : limite la porté de la génération au plugin désigné parmi la liste: 'dependance', 'aot-cot', 'lit', 'carto', 'berge', 'reglementary', 'vegetation', 'core'")
        sys.exit(0)

    for i in range(len(argv)-1):
        if argv[i] == "-p":
            if argv[i+1] in plugins:
                P = argv[i+1]
            else:
                print("Plugins available: " + str(plugins))
            
    if P != "all":
        if P == "aot-cot":
            ecore_paths.append("../plugin-aot-cot/model/aot_cot.ecore")
            properties_paths.append("../plugin-" + P + "/src/main/resources/fr/sirs/core/model/*")
        elif P == "core":
            ecore_paths.append("../sirs-core/model/sirs.ecore")
            properties_paths.append("../sirs-core/src/main/resources/fr/sirs/core/model/*")
        else:
            ecore_paths.append("../plugin-" + P + "/model/" + P + ".ecore")
            properties_paths.append("../plugin-" + P + "/src/main/resources/fr/sirs/core/model/*")
    else:
        del plugins[-1]
        ecore_paths.append("../sirs-core/model/sirs.ecore")
        properties_paths.append("../sirs-core/src/main/resources/fr/sirs/core/model/*")
        for plugin in plugins:
            if plugin == "aot-cot":
                ecore_paths.append("../plugin-aot-cot/model/aot_cot.ecore")
            else:
                ecore_paths.append("../plugin-" + plugin + "/model/" + plugin + ".ecore")
            properties_paths.append("../plugin-" + plugin + "/src/main/resources/fr/sirs/core/model/*")

    return ecore_paths, properties_paths
    

def is_general_attribute(name):
    return name in ["geometry", "date_debut", "date_fin", "dateMaj", "commentaire", "designation", "valid", "author"]


class WrapAttribute(object):
    def __init__(self, clazz, attribute: Ecore.EAttribute, labels):
        self.name = attribute.name
        self.type = attribute.eType.name
        try:
            self.label = labels.to_label(clazz, attribute.name)
        except KeyError:
            self.label = ""
        self.reference = False
        self.min = 0 if attribute.eType.name == "EFloat" or attribute.eType.name == "EInt" else None


class WrapReference(object):
    def __init__(self, clazz, reference: Ecore.EReference, labels):
        self.name = reference.name
        self.type = reference.eType.name
        try:
            self.label = labels.to_label(clazz, reference.name)
        except KeyError:
            self.label = ""
        self.reference = True
        self.multiple = reference.upper
        self.containment = reference.containment


def transform(classifier: Ecore.EClassifier, result, clazz, ll):
    if type(classifier) == Ecore.EClass:
        super_types: Ecore.EOrderedSet = classifier.eAllSuperTypes()
        for st in super_types:
            transform(st, result, clazz, ll)
        content = list(classifier.eAllContents())
        for obj in content:
            if type(obj) == Ecore.EAttribute:
                a = WrapAttribute(clazz, obj, ll)
                if not is_general_attribute(a.name):
                    result[clazz][a.name] = a.__dict__
            if type(obj) == Ecore.EReference:
                r = WrapReference(clazz, obj, ll)
                if not is_general_attribute(r.name):
                    result[clazz][r.name] = r.__dict__


def engine(formTemplatePilote, resourceSet, path_to_ecore, path_to_properties):
    resource = resourceSet.get_resource(URI(path_to_ecore))
    graphMMRoot = resource.contents[0]  # We get the root (an EPackage here)
    ll = LabelLoader()
    ll.complete_dict(path_to_properties)

    for clazz in ll.classes:
        classifier: Ecore.EClassifier = graphMMRoot.getEClassifier(clazz)
        formTemplatePilote[clazz] = {}
        transform(classifier, formTemplatePilote, clazz, ll)
    

if __name__ == "__main__":
    ecore_paths, properties_paths = check_argument()
    formTemplatePilote = {}

    global_registry[Ecore.nsURI] = Ecore  # We load the Ecore metamodel first
    rset = ResourceSet()
    for i in range(len(ecore_paths)):
        engine(formTemplatePilote, rset, ecore_paths[i], properties_paths[i])
    dump = json.dumps(formTemplatePilote, indent=2, ensure_ascii=False)

    with open("formTemplatePilote.json", "w") as f:
         f.write(dump)


    
