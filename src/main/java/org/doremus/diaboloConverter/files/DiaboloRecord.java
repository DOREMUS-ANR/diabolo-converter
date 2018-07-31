package org.doremus.diaboloConverter.files;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class DiaboloRecord {

  public abstract String getId();

  static Map<String, List<String>> toStringMap(File source, String fieldKey, String fieldValue) throws FileNotFoundException, XMLStreamException {
    Map<String, List<String>> map = new HashMap<>();

    XMLInputFactory xif = XMLInputFactory.newFactory();
    XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(source));

    String currentKey = null;
    while (xsr.hasNext()) {
      int i = xsr.next();
      if (i != XMLStreamConstants.START_ELEMENT) continue;
      if (xsr.getLocalName().equals(fieldKey)) {
        currentKey = xsr.getElementText();
        if (!map.containsKey(currentKey))
          map.put(currentKey, new ArrayList<>());
      } else if (xsr.getLocalName().equals(fieldValue))
        map.get(currentKey).add(xsr.getElementText());
    }

    return map;
  }

  static Map<String, String> toDictionary(File source, String fieldKey, String fieldValue) throws XMLStreamException, FileNotFoundException {
    Map<String, String> map = new HashMap<>();

    XMLInputFactory xif = XMLInputFactory.newFactory();
    XMLStreamReader xsr = xif.createXMLStreamReader(new FileReader(source));

    String currentKey = null;
    while (xsr.hasNext()) {
      int i = xsr.next();
      if (i != XMLStreamConstants.START_ELEMENT) continue;

      if (xsr.getLocalName().equals(fieldKey)) {
        currentKey = xsr.getElementText();
      } else if (xsr.getLocalName().equals(fieldValue)) {
        map.put(currentKey, xsr.getElementText());
      }
    }
    return map;
  }

}
