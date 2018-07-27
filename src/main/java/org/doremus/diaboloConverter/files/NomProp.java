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

@XmlRootElement(name = "main")
@XmlAccessorType(XmlAccessType.FIELD)
public class NomProp {
  private static NomProp singleton;

  @XmlElement(name = "DATA_RECORD")
  private List<Person> persons;

  public List<Person> getPersons() {
    return persons;
  }

  public static Person getPerson(String id) {
    if (singleton == null) init();
    return singleton.persons.stream()
      .filter(x -> id.equals(x.getId()))
      .findFirst().orElse(null);
  }


  public static NomProp fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(NomProp.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (NomProp) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

  public static void init() {
    File input = new File(Paths.get(Converter.inputFolderPath, "RÇfÇrentiels", "NOMPROP.xml").toString());
    singleton = NomProp.fromFile(input);
  }

}
