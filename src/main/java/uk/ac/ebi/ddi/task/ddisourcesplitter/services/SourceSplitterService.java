package uk.ac.ebi.ddi.task.ddisourcesplitter.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.ac.ebi.ddi.task.ddisourcesplitter.configuration.SourceSplitterTaskProperties;
import uk.ac.ebi.ddi.task.ddisourcesplitter.utils.DatabaseFilter;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;

@Service
public class SourceSplitterService {

    @Autowired
    private SourceSplitterTaskProperties taskProperties;

    public String readDatabaseInfo(File xmlFile) throws XMLStreamException, TransformerException {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);
        XMLEventReader reader = xmlInputFactory.createXMLEventReader(new StreamSource(xmlFile));
        reader = xmlInputFactory.createFilteredReader(reader, new DatabaseFilter(taskProperties.getEntriesElement()));
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        transformer.transform(new StAXSource(reader), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}
