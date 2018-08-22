package org.doremus.diaboloConverter.files;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.Comparator;
import java.util.List;

@XmlRootElement(name = "main")
@XmlAccessorType(XmlAccessType.FIELD)
public class OeuvresNotMod {
  @XmlElement(name = "DATA_RECORD")
  private List<Oeuvre> works;

  public OeuvresNotMod() {
  }


  public List<Oeuvre> getWorks() {
    works.sort(Comparator.comparing(Oeuvre::getId));
    return works;
  }


  public static OeuvresNotMod fromFile(File file) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(OeuvresNotMod.class);
      Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
      return (OeuvresNotMod) jaxbUnmarshaller.unmarshal(file);
    } catch (JAXBException e) {
      e.printStackTrace();
      return null;
    }
  }

}
