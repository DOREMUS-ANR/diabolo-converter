package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.Utils;
import org.doremus.diaboloConverter.files.Person;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.FRBROO;

import java.net.URI;
import java.net.URISyntaxException;

public class F11_Corporate_Body extends DoremusResource {
  private String name, comment;
  private Literal birthDate;

  public F11_Corporate_Body(Person record) throws NullPointerException {
    super(record.getId());
    this.record = record;
    this.name = Utils.fixCase(record.getLabel());
    if (this.name == null) throw new NullPointerException("The name of a Corporate Body cannot be null");

    Resource r = getFromDoremus(this.name);
    if (r != null) {
      this.resource = r;
      setUri(r.getURI());
    }

    this.birthDate = record.getFormationDate();
    comment = record.getComment();

    // TODO what about the big group of TRADIT <country_name>?

    initResource();
    E21_Person.addToCache(record.getId(), this.uri.toString());
  }

  public static Resource getFromDoremus(String name) {
    String sparql = "PREFIX rdfs: <" + RDFS.getURI() + ">\n" +
      "PREFIX efrbroo: <" + FRBROO.getURI() + ">\n" +
      "SELECT DISTINCT ?s " +
      "WHERE { " +
      "?s a efrbroo:F11_Corporate_Body; rdfs:label \"" + name + "\".}";

    return (Resource) Utils.queryDoremus(sparql, "s");
  }

  public F11_Corporate_Body(String id) {
    super(id);
  }

  public String getName() {
    return name;
  }


  private Resource initResource() {
    resource.addProperty(RDF.type, FRBROO.F11_Corporate_Body)
      .addProperty(RDFS.label, this.name)
      .addProperty(CIDOC.P131_is_identified_by, this.name);

    if (birthDate != null) {
      URI tUri = null;
      try {
        tUri = new URI(this.uri + "/formation/interval");
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      E52_TimeSpan ts = new E52_TimeSpan(tUri, birthDate, birthDate);
      this.resource.addProperty(CIDOC.P95i_was_formed_by,
        model.createResource(this.uri + "/formation")
          .addProperty(RDF.type, CIDOC.E66_Formation)
          .addProperty(CIDOC.P4_has_time_span, ts.asResource())
      );
    }


    if (comment != null && !comment.equals(this.name))
      addNote(comment);


    return resource;
  }

}
