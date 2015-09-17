/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.sirs.importer.v2;

import com.healthmarketscience.jackcess.Row;
import fr.sirs.core.InjectorCore;
import fr.sirs.core.model.Element;
import fr.sirs.importer.v2.ImportContext;
import java.util.HashSet;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author Alexis Manin (Geomatys)
 */
public abstract class DocumentModifier<T extends Element> {

    @Autowired
    protected ImportContext context;

    protected DocumentModifier() {
        InjectorCore.injectDependencies(this);
    }

    @PostConstruct
    private void register() {
        HashSet<DocumentModifier> modifiers = context.modifiers.get(getDocumentClass());
        if (modifiers == null) {
            modifiers = new HashSet<>();
            context.modifiers.put(getDocumentClass(), modifiers);
        }
        modifiers.add(this);
    }

    /**
     * @return type for the object to create at import.
     */
    protected abstract Class<T> getDocumentClass();

    /**
     *
     * @param originalData
     * @param outputData
     */
    protected abstract void modifyDocument(final Row originalData, final T outputData);
}
