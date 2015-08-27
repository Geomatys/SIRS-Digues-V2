package fr.sirs.plugin.vegetation;

import fr.sirs.core.model.ParamCoutTraitementVegetation;
import fr.sirs.core.model.ZoneVegetation;
import java.util.Objects;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * Classe utilitaire utilisée en jonction des traitements des zones de
 * végétation et des paramétrages de couts dans le plan.
 * 
 * @author Samuel Andrés
 */
@Deprecated
class TraitementSummary {
        private final ObjectProperty<Class<? extends ZoneVegetation>> typeVegetationClass = new SimpleObjectProperty<>();
        private final StringProperty typeVegetationId = new SimpleStringProperty();
        private final StringProperty typeTraitementId = new SimpleStringProperty();
        private final StringProperty typeSousTraitementId = new SimpleStringProperty();
        private final StringProperty typeFrequenceId = new SimpleStringProperty();
        private final BooleanProperty ponctuel = new SimpleBooleanProperty();

        public StringProperty typeTraitementId(){return typeTraitementId;}
        public StringProperty typeSousTraitementId(){return typeSousTraitementId;}
        public StringProperty typeFrequenceId(){return typeFrequenceId;}
        public BooleanProperty ponctuel(){return ponctuel;}
        public ObjectProperty<Class<? extends ZoneVegetation>> typeVegetationClass(){return typeVegetationClass;}
        public StringProperty typeVegetationId(){return typeVegetationId;}

        @Deprecated
        private TraitementSummary(final Class<? extends ZoneVegetation> typeVegetationClass,
                final String typeTraitementId, final String typeSousTraitementId,
                final String typeFrequenceId, final boolean ponctuel){
            this.typeVegetationClass.set(typeVegetationClass);
            this.typeTraitementId.set(typeTraitementId);
            this.typeSousTraitementId.set(typeSousTraitementId);
            this.typeFrequenceId.set(typeFrequenceId);
            this.ponctuel.set(ponctuel);
            this.typeVegetationId.set(null);
        }

        private TraitementSummary(final Class<? extends ZoneVegetation> typeVegetationClass, final String typeVegetationId,
                final String typeTraitementId, final String typeSousTraitementId,
                final String typeFrequenceId, final boolean ponctuel){
            this.typeVegetationClass.set(typeVegetationClass);
            this.typeTraitementId.set(typeTraitementId);
            this.typeSousTraitementId.set(typeSousTraitementId);
            this.typeFrequenceId.set(typeFrequenceId);
            this.ponctuel.set(ponctuel);
            this.typeVegetationId.set(typeVegetationId);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.typeVegetationClass);
            hash = 83 * hash + Objects.hashCode(this.typeVegetationId);
            hash = 83 * hash + Objects.hashCode(this.typeTraitementId);
            hash = 83 * hash + Objects.hashCode(this.typeSousTraitementId);
            hash = 83 * hash + Objects.hashCode(this.typeFrequenceId);
            hash = 83 * hash + Objects.hashCode(this.ponctuel);
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TraitementSummary other = (TraitementSummary) obj;
            if (!Objects.equals(this.typeVegetationClass.get(), other.typeVegetationClass.get())) {
                return false;
            }
            if (!Objects.equals(this.typeVegetationId.get(), other.typeVegetationId.get())) {
                return false;
            }
            if (!Objects.equals(this.typeTraitementId.get(), other.typeTraitementId.get())) {
                return false;
            }
            if (!Objects.equals(this.typeSousTraitementId.get(), other.typeSousTraitementId.get())) {
                return false;
            }
            if (!Objects.equals(this.typeFrequenceId.get(), other.typeFrequenceId.get())) {
                return false;
            }
            if (!Objects.equals(this.ponctuel.get(), other.ponctuel.get())) {
                return false;
            }
            return true;
        }

        /**
         * Detects if the parameter TraitementSummary is equivalent to the
         * current object.
         *
         * @param obj
         * @return true if this and the parameter reference the same typeTraitementId and the same typeSousTraitementId. false otherwise.
         */
        public boolean equalsTraitementSummary(final TraitementSummary obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }

            if (!Objects.equals(this.typeTraitementId.get(), obj.typeTraitementId.get())) {
                return false;
            }
            if (!Objects.equals(this.typeSousTraitementId.get(), obj.typeSousTraitementId.get())) {
                return false;
            }
            return true;
        }

        /**
         * Construit une ébauche de TraitementSummary à l'aide des informations
         * présentens dans le ParamCoutTraitementVegetation donné en paramètres.
         *
         * Cette opération est réalisée à des fins de simple comparaison de manière
         * à évaluer si un ParamCoutTraitementVegetation prend en charge un
         * Traitement summary (c'est-à-dire correspond à son type et sous-type de
         * traitement).
         *
         * @param param
         * @return
         */
        public static TraitementSummary toSummary(final ParamCoutTraitementVegetation param){
            return new TraitementSummary(null, null, param.getTraitementId(), param.getSousTraitementId(), null, true);
        }
}
