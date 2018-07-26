package org.doremus.diaboloConverter.files;

import org.doremus.diaboloConverter.Converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@XmlRootElement(name = "main")
@XmlAccessorType(XmlAccessType.FIELD)
public class Geo {
  private static Geo singleton = null;
  private static Map<String, List<String>> map = null;

  @XmlElement(name = "DATA_RECORD")
  private List<Place> places;


  public static List<Place> list() {
    return singleton.places;
  }

  public static Place get(String id) {
    if (singleton == null) init();
    return singleton.places.stream()
      .filter(x -> id.equals(x.getId()))
      .findFirst().orElse(null);
  }

  public static List<String> getPlacesOf(String workId) {
    if (map == null) loadMap();
    return map.getOrDefault(workId, new ArrayList<>());
  }


  public static void init() {
    File geoFile = new File(Paths.get(Converter.inputFolderPath, "RÇfÇrentiels", "GEO.xml").toString());
    singleton = Geo.fromFile(geoFile);
  }

  private static void loadMap() {
    File input = new File(Paths.get(Converter.inputFolderPath, "Notices DIABOLO", "N33TCD_DESC_GEO.xml").toString());
    try {
      map = DiaboloRecord.toStringMap(input, "DORIS_KEY", "NOM_DORIS_KEY");
    } catch (FileNotFoundException | XMLStreamException e) {
      e.printStackTrace();
    }
  }

  public static Geo fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(Geo.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (Geo) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }


}
