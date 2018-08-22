package org.doremus.diaboloConverter.musResource;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.rdf.model.*;
import org.apache.jena.util.ResourceUtils;
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.ConstructURI;
import org.doremus.diaboloConverter.files.DiaboloRecord;
import org.doremus.ontology.CIDOC;

import java.net.URI;
import java.net.URISyntaxException;


public abstract class DoremusResource {
  String className;
  final String sourceDb = "rfd"; // radio france diabolo
  protected DiaboloRecord record;

  protected Model model;
  protected URI uri;
  protected Resource resource;
  protected String identifier;

  public DoremusResource() {
    // do nothing, enables customisation for child class
    this.model = ModelFactory.createDefaultModel();
    this.className = this.getClass().getSimpleName();
  }

  public DoremusResource(URI uri) {
    this();
    this.uri = uri;
    this.resource = model.createResource(this.uri.toString());
  }

  public DoremusResource(String identifier) {
    this();
    this.identifier = identifier;


    this.resource = null;
    /* create RDF resource */
    try {
      this.uri = ConstructURI.build(this.sourceDb, this.className, this.identifier);
      this.resource = model.createResource(this.uri.toString())
        .addProperty(DC.identifier, this.identifier);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  protected void setUri(String uri) {
    if (this.uri != null && uri.equals(this.uri.toString())) return;

    try {
      this.uri = new URI(uri);
      if (this.resource != null)
        this.resource = ResourceUtils.renameResource(this.resource, uri);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public DoremusResource(DiaboloRecord record) {
    this(record.getId());
    this.record = record;
  }

  public DoremusResource(DiaboloRecord record, String identifier) {
    this(identifier);
    this.record = record;
  }

  public Resource asResource() {
    return this.resource;
  }

  public Model getModel() {
    return this.model;
  }

  public String getIdentifier() {
    return this.identifier;
  }

  protected void addNote(String text) {
    if (text == null) return;
    text = text.trim();
    if (text.isEmpty()) return;

    this.resource
      .addProperty(RDFS.comment, text, "fr")
      .addProperty(CIDOC.P3_has_note, text, "fr");
  }

  public URI getUri() {
    return uri;
  }

  protected void setClass(OntClass _class) {
    this.resource.addProperty(RDF.type, _class);
  }

  public DoremusResource addProperty(Property property, DoremusResource resource) {
    if (resource != null) {
      this.addProperty(property, resource.asResource());
      this.model.add(resource.getModel());
    }
    return this;
  }

  public DoremusResource addProperty(Property property, Resource resource) {
    if (resource != null) this.resource.addProperty(property, resource);
    return this;
  }

  public DoremusResource addProperty(Property property, String literal) {
    if (literal != null && !literal.isEmpty()) this.resource.addProperty(property, literal.trim());
    return this;
  }

  public DoremusResource addProperty(Property property, String literal, String lang) {
    if (literal != null && !literal.isEmpty()) this.resource.addProperty(property, literal.trim(), lang);
    return this;
  }

  protected DoremusResource addProperty(Property property, Literal literal) {
    if (literal != null) this.resource.addProperty(property, literal);
    return this;
  }

  protected DoremusResource addProperty(Property property, String literal, XSDDatatype datatype) {
    if (literal != null && !literal.isEmpty()) this.resource.addProperty(property, literal.trim(), datatype);
    return this;
  }

  public void addTimeSpan(E52_TimeSpan timeSpan) {
    if(timeSpan!=null&& timeSpan.asResource()!= null)
      this.resource.addProperty(CIDOC.P4_has_time_span, timeSpan.asResource());
    this.model.add(timeSpan.getModel());
  }

}
