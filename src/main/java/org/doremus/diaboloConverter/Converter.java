package org.doremus.diaboloConverter;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.*;
import org.doremus.diaboloConverter.files.*;
import org.doremus.diaboloConverter.musResource.DoremusResource;
import org.doremus.diaboloConverter.musResource.E21_Person;
import org.doremus.diaboloConverter.musResource.E53_Place;
import org.doremus.diaboloConverter.musResource.F11_Corporate_Body;
import org.doremus.ontology.*;
import org.doremus.string2vocabulary.VocabularyManager;
import org.geonames.Toponym;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Converter {
  private static final String UTF8_BOM = "\uFEFF";
  public static Resource RADIO_FRANCE = ResourceFactory.createResource("http://data.doremus.org/organization/Radio_France");
  public static Resource DOREMUS = ResourceFactory.createResource("http://data.doremus.org/organization/DOREMUS");
  private static List<String> SKIP_NOTICE = Arrays.asList("824228", "519004", "627510", "715539", "451869",
    "621325", "357546");

  static Properties properties;
  static String dataFolderPath;
  public static String inputFolderPath;
  private static String outputFolderPath;
  private static boolean modifiedOut = false;

  public static void main(String[] args) throws IOException {
    // INIT
    System.out.println("\n\n******* Running Diabolo Converter *********");
    loadProperties();
    System.out.println(properties);

    GeoNames.loadCache();
    E21_Person.loadCache();

    ClassLoader classLoader = Converter.class.getClassLoader();
    VocabularyManager.setVocabularyFolder(classLoader.getResource("vocabulary").getPath());
    VocabularyManager.init(classLoader.getResource("property2family.csv"));

    System.out.println("\n\n");
    // end INIT

    inputFolderPath = properties.getProperty("src");
    outputFolderPath = properties.getProperty("out");


    if (properties.getProperty("persons").equals("true")) {
      System.out.println("...Converting persons");

      File input = new File(Paths.get(inputFolderPath, "RÇfÇrentiels", "NOMPROP.xml").toString());
      NomProp source = NomProp.fromFile(input);

      Path personPath = Paths.get(outputFolderPath, "person");
      if (Files.notExists(personPath)) Files.createDirectories(personPath);
      assert source != null;
      for (Person p : source.getPersons()) {
//        if(!p.getId().equals("68400")) continue;
        if (p.isAPerson())
          parsePerson(p, personPath.toString());
        else if (p.isAGroup())
          parseOrganization(p, personPath.toString());
      }
    }


    if (properties.getProperty("places").equals("true")) {
      System.out.println("...Converting places");
      GeoNames.setUser(properties.getProperty("geonames_user"));
      String geonamesFolder = Paths.get(properties.getProperty("placeFolder"), "geonames").toString();
      System.out.println(geonamesFolder);
      new File(geonamesFolder).mkdirs();
      GeoNames.setDestFolder(geonamesFolder);

      Geo.init();

      new File(outputFolderPath + "/place/p").mkdirs();
      for (Place p : Geo.list())
        parsePlace(p, outputFolderPath + "/place/p");
    }

    System.out.println("...CONVERTING WORKS");
    File input = new File(Paths.get(inputFolderPath, "RÇfÇrentiels", "OEUVRES_NOT_MOD.xml").toString());
    OeuvresNotMod source = OeuvresNotMod.fromFile(input);


    Path workPath = Paths.get(outputFolderPath, "work");
    if (Files.notExists(workPath)) Files.createDirectories(workPath);
    assert source != null;
    for (Oeuvre work : source.getWorks()) {
      if (SKIP_NOTICE.contains(work.getId())) continue;
//      System.out.println(work.getId());
      parseRecord(work, workPath.toString());
    }

//    new File(outputFolderPath + "/item").mkdirs();
//

//
//    if (properties.getProperty("organizations").equals("true")) {
//      File organizFolder = new File(Paths.get(dataFolderPath, "MORALE").toString());
//      new File(outputFolderPath + "/organization").mkdirs();
//      for (File p : organizFolder.listFiles())
//        parseOrganization(p, outputFolderPath + "/organization");
//    }
//
//    // MAG_CONTENU is the first folder to parse
//    File mcFolder = new File(Paths.get(dataFolderPath, "MAG_CONTENU").toString());
//    for (File mc : mcFolder.listFiles()) {
//      parseRecord(mc, outputFolderPath + "/item");
//    }
  }

  public static void parsePerson(String id) {
    Path personPath = Paths.get(outputFolderPath, "person");

    Person p = NomProp.getPerson(id);
    if (p == null) return;
    parsePerson(p, personPath.toString());
  }

  private static void parsePerson(Person p, String outputFolder) {
    p.init();
//    System.out.println(p.getId());
    try {
      DoremusResource r = new E21_Person(p);
      writeTtl(r.getModel(), Paths.get(outputFolder, p.getId() + ".ttl"));
    } catch (URISyntaxException | IOException e) {
      e.printStackTrace();
    }
  }

  private static void parseOrganization(Person mr, String outputFolder) {
    System.out.println(mr.getId());

    try {
      F11_Corporate_Body cb = new F11_Corporate_Body(mr);
      writeTtl(cb.getModel(), Paths.get(outputFolder, mr.getId() + ".ttl"));
    } catch (NullPointerException e) {
      System.out.println("Corporate without name: " + mr.getId());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void parsePlace(Place place, String outputFolder) {
    if (place.isPeople()) return; // TODO people?

    Model m = ModelFactory.createDefaultModel();

    System.out.println("Place: " + place.getId() + " : " + place.getLabel());

    Toponym tp = GeoNames.query(place.getId(), place.getLabel(), place.getType(),
      place.getCountry(), place.getContinent());
    if (tp != null) {
      // simply download the file
      String uri = GeoNames.toURI(tp.getGeoNameId());
      System.out.println("> " + uri + " : " + tp.getName());
      GeoNames.downloadRdf(tp.getGeoNameId());

      // add some additional info
      Resource r = m.createResource(uri).addProperty(RDF.type, CIDOC.E53_Place);
      if (tp.getName() != null) r.addProperty(RDFS.label, tp.getName());
    } else {
      // model it as a Place
      E53_Place pl = new E53_Place(place);
      m.add(pl.getModel());
    }
    try {
      writeTtl(m, Paths.get(outputFolder, place.getId() + ".ttl"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void parseRecord(Oeuvre source, String outputFolder) {
//    if(!Objects.equals(source.getId(), "149106"))return;
    try {
      RecordConverter r = new RecordConverter(source);
      Model m = r.getModel();

      if (m == null) return;

      VocabularyManager.string2uri(m);
      if (!modifiedOut) modifiedOut = addModified(m);

      writeTtl(m, Paths.get(outputFolder, source.getId() + ".ttl"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  private static boolean addModified(Model model) {
    model.createResource("http://data.doremus.org/diabolo")
      .addProperty(DCTerms.modified, Instant.now().toString(), XSDDatatype.XSDdateTime);
    return true;
  }

  private static void writeTtl(Model m, Path path) throws IOException {
    writeTtl(m, path.toString());
  }

  private static void writeTtl(Model m, String filename) throws IOException {
    m.setNsPrefix("mus", MUS.getURI());
    m.setNsPrefix("ecrm", CIDOC.getURI());
    m.setNsPrefix("efrbroo", FRBROO.getURI());
    m.setNsPrefix("xsd", XSD.getURI());
    m.setNsPrefix("dcterms", DCTerms.getURI());
    m.setNsPrefix("owl", OWL.getURI());
    m.setNsPrefix("foaf", FOAF.getURI());
    m.setNsPrefix("rdfs", RDFS.getURI());
    m.setNsPrefix("prov", PROV.getURI());
    m.setNsPrefix("time", Time.getURI());
    m.setNsPrefix("schema", Schema.getURI());


    // Write the output file
    FileWriter out = new FileWriter(filename);
    // m.write(System.out, "TURTLE");
    m.write(out, "TURTLE");
    out.close();
  }

  private static void loadProperties() {
    properties = new Properties();
    String filename = "config.properties";

    try {
      InputStream input = new FileInputStream(filename);
      properties.load(input);
      input.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }

}


