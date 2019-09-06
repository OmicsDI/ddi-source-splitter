package uk.ac.ebi.ddi.task.ddisourcesplitter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("splitter")
public class SourceSplitterTaskProperties {

    private String inputDirectory;

    private String outputDirectory;

    private String filePrefix;

    private int numberEntries = 10;

    private String originalPrefix = ".xml";

    private String databaseElement = "database";

    private String entriesElement = "entries";

    private String entryElement = "entry";

    private List<String> filters = new ArrayList<>();

    public String getDatabaseElement() {
        return databaseElement;
    }

    public void setDatabaseElement(String databaseElement) {
        this.databaseElement = databaseElement;
    }

    public String getEntriesElement() {
        return entriesElement;
    }

    public void setEntriesElement(String entriesElement) {
        this.entriesElement = entriesElement;
    }

    public String getEntryElement() {
        return entryElement;
    }

    public void setEntryElement(String entryElement) {
        this.entryElement = entryElement;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getFilePrefix() {
        return filePrefix;
    }

    public void setFilePrefix(String filePrefix) {
        this.filePrefix = filePrefix;
    }

    public int getNumberEntries() {
        return numberEntries;
    }

    public void setNumberEntries(int numberEntries) {
        this.numberEntries = numberEntries;
    }

    public String getOriginalPrefix() {
        return originalPrefix;
    }

    public void setOriginalPrefix(String originalPrefix) {
        this.originalPrefix = originalPrefix;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    @Override
    public String toString() {
        return "SourceSplitterTaskProperties{"
                + "inputDirectory='" + inputDirectory + '\''
                + ", outputDirectory='" + outputDirectory + '\''
                + ", filePrefix='" + filePrefix + '\''
                + ", numberEntries=" + numberEntries
                + ", originalPrefix='" + originalPrefix + '\''
                + ", databaseElement='" + databaseElement + '\''
                + ", entriesElement='" + entriesElement + '\''
                + ", entryElement='" + entryElement + '\''
                + '}';
    }
}
