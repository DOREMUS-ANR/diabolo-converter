package org.doremus.diaboloConverter.musResource;

import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.doremus.diaboloConverter.ConstructURI;
import org.doremus.diaboloConverter.ISNIWrapper;
import org.doremus.diaboloConverter.Utils;
import org.doremus.diaboloConverter.files.Person;
import org.doremus.isnimatcher.ISNIRecord;
import org.doremus.ontology.CIDOC;
import org.doremus.ontology.PROV;
import org.doremus.ontology.Schema;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

public class E21_Person extends DoremusResource {
  private static HashMap<String, String> cache;

  private String firstName;
  private String lastName;
  private String fullName;
  private List<String> pseudo, altNames;
  private Literal birthDate;
  private Literal deathDate;
  private String birthYear, deathYear;
  private String comment;


  public E21_Person(String id) {
    String uriCache = cache.get(id);
    if (uriCache == null || uriCache.isEmpty())
      throw new RuntimeException("Unknow person: " + id);

    try {
      uri = new URI(uriCache);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    this.resource = model.createResource(uri.toString());
  }

  public E21_Person(Person record) throws URISyntaxException, RuntimeException {
    this.record = record;
    this.firstName = record.getName();
    this.fullName = record.getFullName();
    this.lastName = record.getSurname();
    this.pseudo = record.getPseudonym();
    this.altNames = record.getAltName();
    this.birthDate = record.getBirthDate();
    this.deathDate = record.getDeathDate();
    this.birthYear = toYear(record.getBirthDate());
    this.deathYear = toYear(record.getDeathDate());
    this.comment = record.getComment();

    String ln = lastName;
    if (lastName == null || lastName.isEmpty()) ln = fullName;
    if (ln.isEmpty()) throw new RuntimeException("Person without surname neither pseudo :" + record.getId());

    this.uri = ConstructURI.build("E21_Person", firstName, ln, birthYear);
    initResource();
    interlink();

    addToCache(record.getId(), this.uri.toString());
  }

  private String toYear(Literal d) {
    if (d == null) return null;
    return d.getLexicalForm().substring(0, 4);
  }

  public String getIdentification() {
    if (lastName == null) return null;
    String identification = lastName;
    if (firstName != null) identification += ", " + firstName;
    if (birthYear != null) {
      identification += " (" + birthYear;
      if (deathYear != null) identification += "-" + deathYear;
      identification += ")";
    }
    return identification;
  }

  private Resource initResource() {
    this.resource = model.createResource(this.uri.toString());
    resource.addProperty(RDF.type, CIDOC.E21_Person);

    addProperty(FOAF.firstName, firstName);
    addProperty(FOAF.surname, lastName);
    addProperty(FOAF.name, fullName);
    addProperty(RDFS.label, fullName);
    for (String ps : pseudo)
      addProperty(FOAF.name, ps);
    for (String ps : altNames)
      addProperty(FOAF.name, ps);

    addProperty(CIDOC.P131_is_identified_by, this.getIdentification());
    addProperty(DCTerms.identifier, record.getId());

    try {
      addDate(birthDate, false);
      addDate(deathDate, true);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }

    addNote(this.comment);
    return resource;
  }

  private void addDate(Literal date, boolean isDeath) throws URISyntaxException {
    if (date == null) return;

    String url = this.uri + (isDeath ? "/death" : "/birth");

    E52_TimeSpan ts = new E52_TimeSpan(new URI(url + "/interval"), date, date);
    Property schemaProp = isDeath ? Schema.deathDate : Schema.birthDate;

    this.resource.addProperty(isDeath ? CIDOC.P100i_died_in : CIDOC.P98i_was_born,
      model.createResource(url)
        .addProperty(RDF.type, isDeath ? CIDOC.E69_Death : CIDOC.E67_Birth)
        .addProperty(CIDOC.P4_has_time_span, ts.asResource())
    ).addProperty(schemaProp, ts.getStart());
    model.add(ts.getModel());
  }


  public void addProperty(Property property, Literal object) {
    if (property == null || object == null) return;
    resource.addProperty(property, object);
  }

  private void addProperty(Property property, String object, String lang) {
    if (property == null || object == null || object.isEmpty()) return;

    if (lang != null)
      resource.addProperty(property, model.createLiteral(object, lang));
    else
      resource.addProperty(property, object);
  }

  private void addProperty(Property property, String object) {
    addProperty(property, object, null);
  }

  private void addPropertyResource(Property property, String uri) {
    if (property == null || uri == null) return;
    resource.addProperty(property, model.createResource(uri));
  }

  public static void loadCache() {
    cache = new HashMap<>();
    try {
      FileInputStream fis = new FileInputStream("person.properties");
      Properties properties = new Properties();
      properties.load(fis);

      for (String key : properties.stringPropertyNames()) {
        cache.put(key, properties.get(key).toString());
      }
    } catch (IOException e) {
      System.out.println("No 'person.properties' file found. I will create it.");
    }

  }

  public static void addToCache(String key, String value) {
    cache.put(key, value);
    saveCache();
  }

  private static void saveCache() {
    Properties properties = new Properties();

    for (Map.Entry<String, String> entry : cache.entrySet()) {
      properties.put(entry.getKey(), entry.getValue() + "");
    }

    try {
      properties.store(new FileOutputStream("person.properties"), null);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private boolean interlink() {
    // 1. search in doremus by name/date
    Resource match = getPersonFromDoremus();
    if (match != null) {
      this.setUri(match.getURI());
      return true;
    }

    // 2. search in isni by name/date
    String s = Utils.mergeNameSurname(this.firstName, this.lastName);
    List<String> labelsToCheck = new ArrayList<>();
    labelsToCheck.add(fullName);
    if (s != null && !fullName.equalsIgnoreCase(s)) labelsToCheck.add(s);

    ISNIRecord isniMatch = null;
    while (isniMatch == null && labelsToCheck.size() > 0) {
      String l = labelsToCheck.remove(0);
      System.out.println("- " + l);
      try {
        isniMatch = ISNIWrapper.search(l, this.birthYear);
      } catch (IOException e) {
        return false;
      }
    }
    if (isniMatch == null) return false;

    // 3. search in doremus by isni
    match = getPersonFromDoremus(isniMatch.uri);
    if (match != null) {
      this.setUri(match.getURI());
      return true;
    }

    // 4. add isni info
    this.isniEnrich(isniMatch);
    return false;
  }

  private Resource getPersonFromDoremus() {
    String sparql =
      "PREFIX ecrm: <" + CIDOC.getURI() + ">\n" +
        "PREFIX foaf: <" + FOAF.getURI() + ">\n" +
        "PREFIX prov: <" + PROV.getURI() + ">\n" +
        "PREFIX schema: <" + Schema.getURI() + ">\n" +
        "SELECT DISTINCT ?s " +
        "FROM <http://data.doremus.org/bnf> " +
        "WHERE { " +
        "?s a ecrm:E21_Person; foaf:name \"" + this.fullName + "\"." +
        (this.birthYear != null ? "?s schema:birthDate ?date. FILTER regex(str(?date), \"" + this.birthYear +
          "\")\n" : "") +
        "}";

    return (Resource) Utils.queryDoremus(sparql, "s");
  }

  private Resource getPersonFromDoremus(String isni) {
    String sparql =
      "PREFIX owl: <" + OWL.getURI() + ">\n" +
        "SELECT DISTINCT * WHERE {" +
        " ?s owl:sameAs <" + isni + "> }";

    return (Resource) Utils.queryDoremus(sparql, "s");
  }


  public void isniEnrich(ISNIRecord isni) {
    this.addPropertyResource(OWL.sameAs, isni.uri);
    this.addPropertyResource(OWL.sameAs, isni.getViafURI());
    this.addPropertyResource(OWL.sameAs, isni.getMusicBrainzUri());
    this.addPropertyResource(OWL.sameAs, isni.getMuziekwebURI());
    this.addPropertyResource(OWL.sameAs, isni.getWikidataURI());

    String wp = isni.getWikipediaUri();
    String dp = isni.getDBpediaUri();

    if (wp == null) {
      wp = isni.getWikipediaUri("fr");
      dp = isni.getDBpediaUri("fr");
    }
    this.addPropertyResource(OWL.sameAs, dp);
    this.addPropertyResource(FOAF.isPrimaryTopicOf, wp);
  }

}
