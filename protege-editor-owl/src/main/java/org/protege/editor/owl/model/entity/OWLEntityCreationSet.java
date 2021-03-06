package org.protege.editor.owl.model.entity;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import java.util.ArrayList;
import java.util.List;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 23-Sep-2006<br><br>

 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class OWLEntityCreationSet<E extends OWLEntity> {

    private E owlEntity;

    private List<OWLOntologyChange> changes;


    public OWLEntityCreationSet(E owlEntity, List<? extends OWLOntologyChange> changes) {
        this.owlEntity = owlEntity;
        this.changes = new ArrayList<>(changes);
    }


    public OWLEntityCreationSet(E owlEntity) {
        this.owlEntity = owlEntity;
        changes = new ArrayList<>();
    }


    public E getOWLEntity() {
        return owlEntity;
    }


    public List<? extends OWLOntologyChange> getOntologyChanges() {
        return changes;
    }
}
