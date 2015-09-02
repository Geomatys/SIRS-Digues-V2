package fr.sirs.plugin.vegetation.map;

/**
 * État de planification d'une parcelle dans un plan pour une année donnée.
 * 
 * @author Samuel Andrés
 */
public enum PlanifState {

    /**
     * La parcelle n'est pas planifiée cette année.
     */
    NON_PLANIFIE,
    
    /**
     * La parcelle est planifiée cette année, pour la première fois dans le plan.
     */
    PLANIFIE_PREMIERE_FOIS,
    
    /**
     * La parcelle est planifiée cette année, mais ce n'est pas la première fois dans le plan.
     */
    PLANIFIE
}
