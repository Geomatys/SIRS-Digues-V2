
### Description
Ce script permet de créer:
 - le fichier formTemplatePilote.json (à partir des diagrammes ecore et des fichiers .properties).
 - le fichier user_preference_correspondence.json

##### SirsMobile
formTemplatePilote.json contient l'objet formTemplatePilote utilisé par l'application mobile dans le service formstemplate.service.ts .
formTemplatePilote est une feuille de route qui permet de générer les composants des formulaires d'édition de l'application mobile.
formTemplatePilote, dans le service formstemplate.service.ts, doit être mis à jour à chaque modification du modèle SIRS.
Attention, actuellement ce fonctionnement ne concerne que les classes:
	Core
		Prestation
	Plugin-dependance
		AmenagementHydraulique
		DesordreDependance
		PrestationAmenagementHydraulique
		StructureAmenagementHydraulique
		OuvrageAssocieAmenagementHydraulique
		OrganeProtectionCollective

##### Plugin QGIS
formTemplatePilote.json et user_preference_correspondence.json sont des fichiers de configuration que l'on retrouve à la racine du plugin.
formTemplatePilote.json permet de structurer la couches de données générées dans QGIS.
user_preference_correspondence.json conserve les préférences de l'utilisateur concernant les attributs sélectionnés par
couche, le style des couches dans Qgis par type et par géométrie et le projection par type.

### Usage
Ce script doit être lancé depuis le dossier SirsMobile qui doit se trouver à la racine du projet Sirs.
-plugin : permet de copier les fichiers de configuration formTemplatePilote.json et user_preference_correspondence.json dans le module Qgis
(vous devez renseigner le chemin absolu du plugin dans la variable QGIS_PLUGIN_PROJECT_PATH de ce script)
-mobile : permet de remplacer le fichier de configuration form-template-pilotes.ts dans le projet SIRSMOBILEV2
(vous devez renseigner le chemin absolu du plugin dans la variable SIRS_MOBILE_PROJECT_PATH de ce script et choisir les classes qui apparaitront
dans le fichier form-template-pilote.ts dans la variable FORM_TEMPLATE_MOBILE_CLASS)
-filter-general : permet de d'enlever des classes générées les attributs généraux: geometry, date_debut, date_fin, dateMaj, commentaire, designation, valid, author.

