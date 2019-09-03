package uk.ac.ebi.ddi.task.ddisourcesplitter.utils;

import javax.xml.stream.EventFilter;
import javax.xml.stream.events.XMLEvent;

public class DatabaseFilter implements EventFilter {
    private boolean deleteSection = false;

    private String entriesName;

    public DatabaseFilter(String entriesName) {
        this.entriesName = entriesName;
    }

    @Override
    public boolean accept(XMLEvent event) {
        if (event.isStartElement())
            if (event.asStartElement().getName().getLocalPart().equals(entriesName)) {
                deleteSection = true;
                return false;
            }
        if (event.isEndElement())
            if (event.asEndElement().getName().getLocalPart().equals(entriesName)) {
                deleteSection = false;
                return false;
            }
        return !deleteSection;
    }
}
