package org.doremus.diaboloConverter.files;

import org.doremus.diaboloConverter.Converter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

@XmlRootElement(name = "main")
@XmlAccessorType(XmlAccessType.FIELD)
public class SousOeuvres {
  private static SousOeuvres singleton = null;

  @XmlElement(name = "DATA_RECORD")
  private List<SousOeuvre> works;


  public static List<Oeuvre> subWorksOf(String workId) {
    if (singleton == null) init();

    return singleton.works.stream()
      .filter(x -> workId.equals(x.getWorkId()))
      .collect(Collectors.toList());
  }


  public static SousOeuvres fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(SousOeuvres.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (SousOeuvres) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void init() {
    File geoFile = new File(Paths.get(Converter.inputFolderPath, "Notices DIABOLO", "N33TCD_SOUSOEUVRE.xml").toString());
    singleton = fromFile(geoFile);
  }


}
