from pyecore.resources import ResourceSet, URI, global_registry
from style.updateUserPreferenceStyles import update_style_from_data
import pyecore.ecore as Ecore  # We get a reference to the Ecore metamodel implementation.
import json
import sys
import glob
import os
import shutil
import re

#QGIS_PLUGIN_PROJECT_PATH = ""
QGIS_PLUGIN_PROJECT_PATH = "/home/maximegavens/GEOMATYS/qgis-plugin-couchdb-project/qgis-plugin-couchdb/couchdb_importer"
SIRS_MOBILE_PROJECT_PATH = "/home/maximegavens/GEOMATYS/sirsMobile-project/sirsmobilev2"
FORM_TEMPLATE_MOBILE_CLASS = [
    "AmenagementHydraulique",
    "PrestationAmenagementHydraulique",
    "StructureAmenagementHydraulique",
    "OrganeProtectionCollective",
    "DesordreDependance",
    "OuvrageAssocieAmenagementHydraulique",
    "Prestation"
]

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
    copy2mobile = False
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
        if argv[i] == "-filter-general":
            general_filter = True
        if argv[i] == "-plugin":
            copy2plugin = True
        if argv[i] == "-mobile":
            copy2mobile = True

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

    return ecore_paths, properties_paths, general_filter, copy2plugin, copy2mobile
    

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


def is_child_of(classifier: Ecore.EClassifier, parentName):
    if classifier.name == parentName:
        return True
    if type(classifier) == Ecore.EClass or type(classifier) == Ecore.EProxy:
        super_types: Ecore.EOrderedSet = classifier.eAllSuperTypes()
        for st in super_types:
            if is_child_of(st, parentName):
                return True
    return False


def find_containment_classes(classifier: Ecore.EClassifier):
    if type(classifier) == Ecore.EClass or type(classifier) == Ecore.EProxy:
        super_types: Ecore.EOrderedSet = classifier.eAllSuperTypes()
        for st in super_types:
            for super_c in find_containment_classes(st):
                yield super_c
    content = list(classifier.eAllContents())
    for obj in content:
        try:
            if obj.containment:
                yield obj.eType.name
        except AttributeError:
            pass


def transform(classifier: Ecore.EClassifier, result, clazz, ll, general_filter):
    if type(classifier) == Ecore.EClass or type(classifier) == Ecore.EProxy:
        super_types: Ecore.EOrderedSet = classifier.eAllSuperTypes()
        for st in super_types:
            transform(st, result, clazz, ll, general_filter)
        content = list(classifier.eAllContents())
        for obj in content:
            if type(obj) == Ecore.EAttribute:
                a = WrapAttribute(clazz, obj, ll)
                if not (general_filter and is_general_attribute(a.name)):
                    result[clazz][a.name] = a.__dict__
            if type(obj) == Ecore.EReference:
                r = WrapReference(clazz, obj, ll)
                if not (general_filter and is_general_attribute(r.name)):
                    result[clazz][r.name] = r.__dict__


def filter_by_class(object_pilote, classes):
    return {k: v for k, v in object_pilote.items() if k in classes}


def build_form_template_for_mobile(original_pilot):
    filter_pilot = filter_by_class(original_pilot, FORM_TEMPLATE_MOBILE_CLASS)
    header = '''/* eslint-disable no-console */
/**
 * This object pilote the component BaseForm that is a generic
 * component which build edition.
 *
 * !WARNING!
 * You can generate this object by running the script /SirsMobile/buildEditionFormPilote.py founded
 * at the root of the desktop project.
 */
export const formTemplatePilote = '''

    with open("form-template-pilote.ts", "w") as ftp:
        dump = json.dumps(filter_pilot, indent=4, ensure_ascii=False)
        ftp.write(header + dump)


def print_information(positionables, containment_classes, plugin_succeed, mobile_succeed):
    print("\nAll positionable classes of the plugin Qgis")
    positionables.sort()
    for p in positionables:
        print("\t" + p)
    print("\nAll containment classes from positionable")
    print(containment_classes)
    print()
    if not plugin_succeed:
        print("Impossible de copier les fichiers générés vers le plugin QGIS. Vérifiez que la variable QGIS_PLUGIN_PROJECT_PATH est un chemin valide.")
    if not mobile_succeed:
        print("Impossible de copier le fichier form-template-pilote.ts dans le projet Sirs mobile. Vérifiez que la variable SIRS_MOBILE_PROJECT_PATH est un chemin valide.")
    if plugin_succeed and mobile_succeed:
        print("Exécuté avec succés!")


def copy_to_plugin():
    try:
        shutil.copy("formTemplatePilote.json", QGIS_PLUGIN_PROJECT_PATH)
        shutil.copy("user_preference_correspondence.json", QGIS_PLUGIN_PROJECT_PATH)
        return True
    except TypeError or FileNotFoundError:
        return False


def copy_to_mobile():
    try:
        path = SIRS_MOBILE_PROJECT_PATH
        if path[-1] == "/":
            path = path[:-1]
        path = path + "/src/app/utils"
        shutil.copy("form-template-pilote.ts", path)
        return True
    except TypeError or FileNotFoundError:
        return False


def engine(formTemplatePilote, resourceSet, path_to_ecore, path_to_properties, general_filter):
    resource = resourceSet.get_resource(URI(path_to_ecore))
    graphMMRoot = resource.contents[0]  # We get the root (an EPackage here)
    ll = LabelLoader()
    ll.complete_dict(path_to_properties)

    for clazz in ll.classes:
        # Retrieve the corresponding classifier from the graph
        classifier: Ecore.EClassifier = graphMMRoot.getEClassifier(clazz)
        if classifier is None:
            print(f"class {clazz} has a property file, but model not found in {path_to_ecore}")
            continue
        # Complete the formTemplatePilote object
        formTemplatePilote[clazz] = {}
        transform(classifier, formTemplatePilote, clazz, ll, general_filter)
        # Check if the current class is positionable (implement WithGeometrie and not abstract)
        if is_child_of(classifier, "AvecGeometrie") and not classifier.abstract:
            all_positionable.append(clazz)
            for c in find_containment_classes(classifier):
                containment_classes_from_positionable.add(c)


if __name__ == "__main__":
    # Récupération des arguments
    ecore_paths, properties_paths, general_filter, copy2plugin, copy2mobile = check_argument()

    # Initialisation
    formTemplatePilote = {}
    preferences = {}
    global_registry[Ecore.nsURI] = Ecore  # We load the Ecore metamodel first
    rset = ResourceSet()

    # Construction du fichier formTemplatePilote.json
    for i in range(len(ecore_paths)):
        engine(formTemplatePilote, rset, ecore_paths[i], properties_paths[i], general_filter)

    # Construction du ficher user_preference_correspondance.json
    build_preferences(formTemplatePilote, preferences)
    update_style_from_data(preferences)

    # Impression des fichiers de sortie
    with open("formTemplatePilote.json", "w") as ftp:
        dump = json.dumps(formTemplatePilote, indent=2, ensure_ascii=False)
        ftp.write(dump)
    with open("user_preference_correspondence.json", "w") as lc:
        dump = json.dumps(preferences, indent=2, ensure_ascii=False)
        lc.write(dump)

    # Construction et impression du fichier mobile
    build_form_template_for_mobile(formTemplatePilote)

    # Copie des fichiers de sortie
    pluginSucceed = True
    mobileSucceed = True
    if copy2plugin:
        pluginSucceed = copy_to_plugin()
    if copy2mobile:
        mobileSuceed = copy_to_mobile()

    # Impression des informations
    print_information(all_positionable, containment_classes_from_positionable, pluginSucceed, mobileSucceed)
