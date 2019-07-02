package uk.ac.ebi.ddi.task.ddisourcesplitter.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("splitter")
public class SourceSplitterTaskProperties {

    private String inputDirectory;

    private String outputDirectory;

    private String filePrefix;

    private int numberEntries;

    private String originalPrefix;

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

    @Override
    public String toString() {
        return "SourceSplitterTaskProperties{" +
                "inputDirectory='" + inputDirectory + '\'' +
                ", outputDirectory='" + outputDirectory + '\'' +
                ", filePrefix='" + filePrefix + '\'' +
                ", numberEntries=" + numberEntries +
                ", originalPrefix='" + originalPrefix + '\'' +
                '}';
    }
}
