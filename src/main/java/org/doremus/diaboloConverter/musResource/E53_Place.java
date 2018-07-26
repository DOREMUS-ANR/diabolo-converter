package org.doremus.diaboloConverter.musResource;

import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.GeoNames;
import org.doremus.diaboloConverter.files.Place;
import org.doremus.ontology.CIDOC;

import java.net.URI;

public class E53_Place extends DoremusResource {
  public E53_Place(Place record) {
    super(record);

    String label = record.getLabel();
    String comment = record.getComment();

    this.resource.addProperty(RDF.type, CIDOC.E53_Place)
      .addProperty(RDFS.label, label)
      .addProperty(model.createProperty(GeoNames.NAME), label)
      .addProperty(CIDOC.P1_is_identified_by, record.getLabel())
      .addProperty(CIDOC.P1_is_identified_by, record.getOriginalLabel());

    if (comment != null)
      this.resource.addProperty(CIDOC.P3_has_note, comment);
  }

  public E53_Place(String identifier) {
    super(identifier);

    int match = GeoNames.get(identifier);
    if (match != -1) {
      this.uri = URI.create(GeoNames.toURI(match));
      this.resource = model.createResource(this.uri.toString());
    }
  }
}
