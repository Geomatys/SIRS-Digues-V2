from pyecore.resources import ResourceSet, URI, global_registry
import pyecore.ecore as Ecore  # We get a reference to the Ecore metamodle implem.
import json


def to_label(clazz, name):
    labelMap = {
        "AmenagementHydraulique": {
            "_id": "_id (ne pas modifier/supprimer)",
            "author": "Auteur",
            "capaciteStockage": "Capacité de stockage (m³)",
            "class": "Aménagement hydraulique",
            "classAbrege": "AH",
            "classPlural": "Aménagements hydrauliques",
            "collectiviteCompetence": "Collectivité compétence",
            "commentaire": "Commentaire",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "designation": "Désignation",
            "desordreIds": "Désordres",
            "editedGeoCoordinate": "Coordonnées Geo éditées",
            "fonctionnementId": "Fonctionnement",
            "geometry": "Géométrie",
            "geometryMode": "geometry Mode",
            "gestionnaire": "Gestionnaire",
            "gestionnaireIds": "Gestionnaires",
            "gestions": "Gestions",
            "libelle": "Libellé",
            "observationGenerale": "Observation Générale",
            "observations": "Observations",
            "ouvrageAssocieIds": "Ouvrages associés",
            "photos": "Photos",
            "prestationIds": "Prestations",
            "profondeurMoyenne": "Profondeur moyenne (m)",
            "proprietaireIds": "Proprietaires",
            "proprietes": "Proprietes",
            "structureIds": "Structures",
            "superficie": "Superficie (m²)",
            "traits": "Traits d'aménagement hydraulique",
            "tronconIds": "Tronçons",
            "typeId": "Type",
            "valid": "Validé"
        },
        "DesordreDependance": {
            "_id": "_id (ne pas modifier/supprimer)",
            "abstractDependanceId": "Dépendance ou AH",
            "amenagementHydrauliqueId": "Aménagement hydraulique",
            "articleIds": "Articles",
            "author": "Auteur",
            "categorieDesordreId": "Catégorie de désordre",
            "class": "Désordre (dépendance et AH)",
            "classAbrege": "DD",
            "classPlural": "Désordres (dépendance et AH)",
            "commentaire": "Commentaire",
            "cote": "Côte",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "dependanceId": "Dépendance",
            "designation": "Désignation",
            "editedGeoCoordinate": "Coordonnées Geo éditées",
            "evenementHydrauliqueIds": "Evènements hydrauliques",
            "geometry": "Géométrie",
            "geometryMode": "geometry Mode",
            "lieuDit": "Lieu dit",
            "observations": "Observations",
            "ouvrageAssocieAH": "Ouvrage associé AH",
            "ouvrageAssocieIds": "Ouvrages associés",
            "positionId": "Position",
            "prestationIds": "Prestations",
            "sourceId": "Source",
            "typeDesordreId": "Type de désordre",
            "valid": "Validé"
        },
        "OrganeProtectionCollective": {
            "_id": "_id (ne pas modifier/supprimer)",
            "amenagementHydrauliqueId": "Aménagement hydraulique",
            "author": "Author",
            "class": "Organe de protection collective (AH)",
            "classAbrege": "OPC",
            "classPlural": "Organes de protection collective (AH)",
            "commentaire": "Commentaire",
            "cote": "Côte",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "designation": "Désignation",
            "editedGeoCoordinate": "Coordonnées Geo éditées",
            "etatId": "État",
            "geometry": "Géometrie",
            "geometryMode": "geometry Mode",
            "observations": "Observations",
            "photos": "Photos",
            "typeId": "Type",
            "valid": "Validé"
        },
        "OuvrageAssocieAmenagementHydraulique": {
            "_id": "_id (ne pas modifier/supprimer)",
            "amenagementHydrauliqueAssocieIds": "Aménagements hydrauliques",
            "amenagementHydrauliqueId": "Aménagement hydraulique",
            "author": "Auteur",
            "class": "Ouvrage associé (AH)",
            "classAbrege": "OAAH",
            "classPlural": "Ouvrages associés (AH)",
            "commentaire": "Commentaire",
            "cote": "Côte",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "designation": "Désignation",
            "desordreDependanceAssocieIds": "Désordres",
            "diametre": "Diamètre",
            "editedGeoCoordinate": "Coordonnées Geo éditées",
            "etatId": "État",
            "fonctionnementId": "Fonctionnement",
            "geometry": "Geometrie",
            "geometryMode": "Géometry Mode",
            "gestionnaireIds": "Gestionnaires",
            "hauteur": "Hauteur",
            "materiauId": "Materiau",
            "nombre": "Nombre",
            "numCouche": "Numéro de couche",
            "observations": "Observations",
            "ouvrageDeversant": "Ouvrage Deversant",
            "photos": "Photos",
            "profondeur": "Profondeur",
            "proprietaireIds": "Proprietaires",
            "section": "Section",
            "sourceId": "Source",
            "structureDetaillees": "Structures détaillées",
            "superficie": "Superficie",
            "typeId": "Type d'ouvrage",
            "valid": "Validé"
        },
        "PrestationAmenagementHydraulique": {
            "_id": "_id (ne pas modifier/supprimer)",
            "amenagementHydrauliqueId": "Aménagement hydraulique",
            "author": "Auteur",
            "class": "Prestation (AH)",
            "classAbrege": "PAH",
            "classPlural": "Prestations (AH)",
            "commentaire": "Commentaire",
            "cote": "Coté",
            "coutGlobal": "Coût global (euros HT)",
            "coutMetre": "Coût au mètre (euros HT)",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "designation": "Désignation",
            "desordreIds": "Désordres",
            "editedGeoCoordinate": "Coordonnées Geo éditées",
            "evenementHydrauliqueIds": "Événements hydrauliques",
            "geometry": "Géométrie",
            "geometryMode": "geometry Mode",
            "intervenantIds": "Intervenants",
            "libelle": "Libellé",
            "marcheId": "Marché",
            "mesureDiverse": "Mesure Diverse",
            "observationIds": "Observations",
            "observations": "Observations",
            "ouvrageAssocieAmenagementHydrauliqueIds": "Ouvrages associés",
            "photos": "Photos",
            "rapportEtudeIds": "Rapport d'études",
            "realisationInterne": "Réalisation Interne",
            "sourceId": "Source",
            "typePrestationId": "type de prestation",
            "valid": "Validé"
        },
        "StructureAmenagementHydraulique": {
            "_id": "_id (ne pas modifier/supprimer)",
            "amenagementHydrauliqueId": "Aménagement hydraulique",
            "author": "Auteur",
            "class": "Structure (AH)",
            "classAbrege": "SAH",
            "classPlural": "Structures (AH)",
            "commentaire": "Commentaire",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "designation": "Désignation",
            "epaisseur": "Épaisseur",
            "fonctionId": "Fonction",
            "geometry": "Géometrie",
            "materiauId": "Materiau",
            "natureId": "Nature",
            "numCouche": "Numéro de couche",
            "observations": "Observations",
            "photos": "Photos",
            "sourceId": "Source",
            "valid": "Validé"
        },
        "TraitAmenagementHydraulique": {
            "_id": "_id (ne pas modifier/supprimer)",
            "amenagementHydrauliqueId": "Aménagement hydraulique",
            "author": "Auteur",
            "class": "Trait (AH)",
            "classAbrege": "TAH",
            "classPlural": "Traits (AH)",
            "commentaire": "Commentaire",
            "dateMaj": "Mise à jour",
            "date_debut": "Date de début",
            "date_fin": "Date de fin",
            "designation": "Désignation",
            "geometry": "Géométrie",
            "valid": "Validé"
        }
    }
    return labelMap[clazz][name]


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


def read_graph(graph: Ecore.EPackage):
    result = {
        "AmenagementHydraulique": {},
        "PrestationAmenagementHydraulique": {},
        "StructureAmenagementHydraulique": {},
        "OrganeProtectionCollective": {},
        "DesordreDependance": {},
        "OuvrageAssocieAmenagementHydraulique": {}
    }
    for clazz in result:
        classifier: Ecore.EClassifier = graph.getEClassifier(clazz)
        transform(classifier, result, clazz)
    print(json.dumps(result, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    global_registry[Ecore.nsURI] = Ecore  # We load the Ecore metamodel first
    rset = ResourceSet()
    resource = rset.get_resource(URI('./dependance.ecore'))
    graphMMRoot = resource.contents[0]  # We get the root (an EPackage here)
    read_graph(graphMMRoot)
