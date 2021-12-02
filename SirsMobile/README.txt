
### Description
Ce script permet de créer le fichier formTemplatePilote.json à partir des différents diagrammes ecore de l'application.
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

### Usage
Ce script doit être lancé depuis le dossier SirsMobile qui doit se trouver à la racine du projet Sirs.
Sans option, ce script génére TOUS les objets pilotes de TOUS les plugins et du coeur.
Il est possible de limiter les classes concernées à un plugin (ou coeur):
-p <nom_du_plugin> : limite la porté de la génération au plugin désigné parmi la liste: 'dependance', 'aot-cot', 'lit', 'carto', 'berge', 'reglementary', 'vegetation', 'core'

