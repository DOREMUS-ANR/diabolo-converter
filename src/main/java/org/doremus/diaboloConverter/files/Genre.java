package org.doremus.diaboloConverter.files;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.customconverter.ConvertGermanToBoolean;
import org.doremus.diaboloConverter.Converter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Genre {
  public static final String VOICE_KEY = "518";
  public static final String RECIT_KEY = "415";
  private static final String GENRE_NAMESPACE = "http://data.doremus.org/vocabulary/diabolo/genre/";
  private static List<Genre> list = null;
  private static Map<String, List<String>> map = null;

  @CsvBindByName(column = "DORIS_KEY")
  private String id;
  @CsvBindByName(column = "DE")
  private String label;
  @CsvCustomBindByName(column = "CATEGORIZATION", converter = ConvertGermanToBoolean.class)
  private boolean categorization;
  @CsvCustomBindByName(column = "SKIP", converter = ConvertGermanToBoolean.class)
  private boolean skip;
  @CsvCustomBindByName(column = "RELIGION", converter = ConvertGermanToBoolean.class)
  private boolean religion;
  @CsvBindByName(column = "AUTRES")
  private String otherType;

  public Genre() {
  }

  public Genre(String id) {
    this.id = id;
    this.label = null;
  }

  public String getType() {
    if (categorization)
      return "categorization";
    if (religion)
      return "religion";
    if (skip)
      return "skip";
    if (otherType != null && !otherType.isEmpty())
      return "other";
    return "genre";
  }

  public static Genre get(String id) {
    if (list == null) init();

    return list.stream()
      .filter(x -> x.id.equals(id))
      .findFirst().orElse(new Genre(id));
  }

  public String getId() {
    return id;
  }

  public boolean isReligion() {
    return religion;
  }

  public boolean shouldBeSkipped() {
    return skip;
  }

  public boolean is(String id) {
    return id.equals(this.id);

  }

  public boolean isVoice() {
    return this.is(VOICE_KEY);
  }

  public boolean isRecit() {
    return this.is(RECIT_KEY);
  }


  public String getLabel() {
    return label.trim();
  }


  private static void init() {
    ClassLoader cl = ClassLoader.getSystemClassLoader();
    File csv = new File(cl.getResource("genres.csv").getFile());
    File input = new File(Paths.get(Converter.inputFolderPath, "Notices DIABOLO", "N33TCD_GENRE.xml").toString());

    try {
      list = new CsvToBeanBuilder(new FileReader(csv)).withType(Genre.class).build().parse();
      map = DiaboloRecord.toStringMap(input, "DORIS_KEY", "GEN_DORIS_KEY");
    } catch (FileNotFoundException | XMLStreamException e) {
      e.printStackTrace();
    }
  }

  public static List<Genre> getGenresOf(String workId) {
    if (list == null) init();

    return map.getOrDefault(workId, new ArrayList<>()).stream()
      .map(Genre::get)
      .collect(Collectors.toList());
  }

  public String getGenreUri() {
    return GENRE_NAMESPACE + this.id.toLowerCase().replaceAll(" ", "_");
  }
}
