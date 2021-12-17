from pyecore.resources import ResourceSet, URI, global_registry
from style.updateUserPreferenceStyles import update_style_from_data
import pyecore.ecore as Ecore  # We get a reference to the Ecore metamodel implementation.
import json
import sys
import glob
import os
import shutil
import re

QGIS_PLUGIN_PROJECT_PATH = ""
#QGIS_PLUGIN_PROJECT_PATH = "/home/maximegavens/GEOMATYS/qgis-plugin-couchdb-project/qgis-plugin-couchdb/couchdb_importer"

all_positionable = []
containment_classes_from_positionable = set()

def unicodize(seg):
    if re.match(r'\\u[0-9a-f]{4}', seg):
        return seg.decode('unicode-escape')

    return seg.decode('utf-8')


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

            with open(path, "r") as propertieFile:
                propertieFileStr = propertieFile.read()
                propertieFileStr = propertieFileStr.encode("UTF-8").decode("unicode_escape")

                rows = propertieFileStr.split('\n')[2:-1]
            for row in rows:
                r = row.split('=')
                self.propertieDictionnary[clazz][r[0]] = r[1]

    def to_label(self, clazz, attribute):
        try:
            return self.propertieDictionnary[clazz][attribute]
        except KeyError:
            return ""

    def load_user_preferences_from_labels(self):
        user_preferences = {}

        for clazz in self.propertieDictionnary:
            user_preferences[clazz] = {"attributes": {}}

            # Does not exist in propertie file
            user_preferences[clazz]["attributes"]["_id"] = True
            user_preferences[clazz]["attributes"]["_rev"] = True
            user_preferences[clazz]["attributes"]["@class"] = True

            for attribute in self.propertieDictionnary[clazz]:
                user_preferences[clazz]["attributes"][attribute] = True
        return user_preferences


def check_argument():
    copy2plugin = False
    general_filter = False
    plugins = ['dependance', 'aot-cot', 'lit', 'carto', 'berge', 'reglementary', 'vegetation', 'core']
    ecore_paths = []
    properties_paths = []
    P = "all"
    argv = sys.argv

    if argv[-1] == "--help":
        with open("README.txt", "r") as f:
            content = f.read()
            print(content)
        sys.exit(0)

    for i in range(len(argv)):
        if argv[i] == "-f":
            general_filter = True
        if argv[i] == "-c":
            copy2plugin = True

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

    return ecore_paths, properties_paths, general_filter, copy2plugin
    

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


def build_preferences(formTemplatePilote, preferences):
    for clazz in formTemplatePilote:
        preferences[clazz] = {"attributes": {}}
        for attribute in formTemplatePilote[clazz]:
            preferences[clazz]["attributes"][attribute] = True


def transform(classifier: Ecore.EClassifier, result, clazz, ll, filter_general):
    if type(classifier) == Ecore.EClass or type(classifier) == Ecore.EProxy:
        super_types: Ecore.EOrderedSet = classifier.eAllSuperTypes()
        for st in super_types:
            transform(st, result, clazz, ll, filter_general)
        content = list(classifier.eAllContents())
        for obj in content:
            if type(obj) == Ecore.EAttribute:
                a = WrapAttribute(clazz, obj, ll)
                if not (filter_general and is_general_attribute(a.name)):
                    result[clazz][a.name] = a.__dict__
            if type(obj) == Ecore.EReference:
                r = WrapReference(clazz, obj, ll)
                if not (filter_general and is_general_attribute(r.name)):
                    result[clazz][r.name] = r.__dict__


def engine(formTemplatePilote, resourceSet, path_to_ecore, path_to_properties, filter_general):
    resource = resourceSet.get_resource(URI(path_to_ecore))
    graphMMRoot = resource.contents[0]  # We get the root (an EPackage here)
    ll = LabelLoader()
    ll.complete_dict(path_to_properties)
    p = ll.load_user_preferences_from_labels()

    for clazz in ll.classes:
        classifier: Ecore.EClassifier = graphMMRoot.getEClassifier(clazz)
        formTemplatePilote[clazz] = {}
        transform(classifier, formTemplatePilote, clazz, ll, filter_general)
    

if __name__ == "__main__":
    ecore_paths, properties_paths, filter_general, copy2plugin = check_argument()
    formTemplatePilote = {}
    preferences = {}
    global_registry[Ecore.nsURI] = Ecore  # We load the Ecore metamodel first
    rset = ResourceSet()

    for i in range(len(ecore_paths)):
        engine(formTemplatePilote, rset, ecore_paths[i], properties_paths[i], filter_general)

    build_preferences(formTemplatePilote, preferences)
    update_style_from_data(preferences)

    with open("formTemplatePilote.json", "w") as ftp:
        dump = json.dumps(formTemplatePilote, indent=2, ensure_ascii=False)
        ftp.write(dump)

    with open("user_preference_correspondence.json", "w") as lc:
        dump = json.dumps(preferences, indent=2, ensure_ascii=False)
        lc.write(dump)

    try:
        if copy2plugin:
            shutil.copy("formTemplatePilote.json", QGIS_PLUGIN_PROJECT_PATH)
            shutil.copy("user_preference_correspondence.json", QGIS_PLUGIN_PROJECT_PATH)
    except TypeError or FileNotFoundError:
        print("Impossible de copier les fichiers générés vers le plugin QGIS. Vérifiez que la variable QGIS_PLUGIN_PROJECT_PATH est un chemin valide.")
