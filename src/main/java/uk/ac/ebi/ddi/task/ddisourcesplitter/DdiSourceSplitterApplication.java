package uk.ac.ebi.ddi.task.ddisourcesplitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.ebi.ddi.ddifileservice.services.IFileSystem;
import uk.ac.ebi.ddi.ddifileservice.services.S3FileSystem;
import uk.ac.ebi.ddi.task.ddisourcesplitter.configuration.SourceSplitterTaskProperties;
import uk.ac.ebi.ddi.task.ddisourcesplitter.services.SourceSplitterService;
import uk.ac.ebi.ddi.task.ddisourcesplitter.utils.XmlUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class DdiSourceSplitterApplication implements CommandLineRunner {

	@Autowired
	private IFileSystem fileSystem;

	@Autowired
	private SourceSplitterService sourceSplitterService;

	@Autowired
	private SourceSplitterTaskProperties taskProperties;

	private List<String> entries = new ArrayList<>();

	private TransformerFactory transformerFactory = TransformerFactory.newInstance();

	private final XMLInputFactory factory = XMLInputFactory.newInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(DdiSourceSplitterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DdiSourceSplitterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fileSystem.cleanDirectory(taskProperties.getOutputDirectory());
		AtomicInteger outIndex = new AtomicInteger(0);

		List<String> files = fileSystem.listFilesFromFolder(taskProperties.getInputDirectory());
		for (String filePath : files) {
			LOGGER.info("Processing file {}", filePath);
			process(filePath, outIndex);
		}
	}

	public void process(String filePath, AtomicInteger outIndex) throws Exception {
		if (!filePath.contains(taskProperties.getOriginalPrefix())) {
			return;
		}
		File file = fileSystem.getFile(filePath);
		LOGGER.info("Reading database info...");
		String database = "";
		if (!taskProperties.getDatabaseElement().isEmpty()) {
			database = sourceSplitterService.readDatabaseInfo(file);
		}

		StringWriter sw = new StringWriter();
		XMLOutputFactory of = XMLOutputFactory.newInstance();
		XMLEventWriter xw = null;
		LOGGER.info("Parsing entries...");
		try (final InputStream stream = new FileInputStream(file)) {
			final XMLEventReader reader = factory.createXMLEventReader(stream);
			while (reader.hasNext()) {
				final XMLEvent event = reader.nextEvent();
				if (event.isStartElement()
						&& event.asStartElement().getName().getLocalPart().equals(taskProperties.getEntryElement())) {
					xw = of.createXMLEventWriter(sw);
				}
				if (event.isEndElement()
						&& event.asEndElement().getName().getLocalPart().equals(taskProperties.getEntryElement())) {
					Objects.requireNonNull(xw).close();
					if (taskProperties.getFilters().isEmpty()) {
						entries.add(sw.toString());
					} else {
						String entry = sw.toString();
						for (String filter : taskProperties.getFilters()) {
							if (entry.contains(filter)) {
								entries.add(entry);
								break;
							}
						}
					}
					sw = new StringWriter();
					xw = null;
				}
				if (xw != null) {
					xw.add(event);
				}
				if (entries.size() > 0 && entries.size() % taskProperties.getNumberEntries() == 0) {
					writeXml(database, entries, outIndex.getAndIncrement());
				}
			}
		}
		writeXml(database, entries, outIndex.getAndIncrement());
		if (fileSystem instanceof S3FileSystem) {
			file.delete();
		}
	}

	private void writeXml(String database, List<String> entries, int index) throws Exception {
		if (entries.size() < 1) {
			return;
		}
		Document document = XmlUtils.createDocument();
		if (!database.isEmpty()) {
			document = XmlUtils.convertStringToDocument(database);
		}
		Node node = document.createElement(taskProperties.getEntriesElement());
		if (!taskProperties.getDatabaseElement().isEmpty()) {
			NodeList nodes = document.getElementsByTagName(taskProperties.getDatabaseElement());
			Element element = (Element) nodes.item(0);
			element.appendChild(node);
		} else {
			document.appendChild(node);
		}

		for (String item : entries) {
			Document itemDoc = XmlUtils.convertStringToDocument(item);
			Node nodeToImport = document.importNode(itemDoc.getFirstChild(), true);
			node.appendChild(nodeToImport);
		}
		String prefixFile = taskProperties.getFilePrefix();
		Transformer transformer = transformerFactory.newTransformer();
		File tmpFile = File.createTempFile("ddi", "tmp.xml");
		try (FileWriter writer = new FileWriter(tmpFile)) {
			StreamResult result = new StreamResult(writer);
			transformer.transform(new DOMSource(document), result);
		}

		String outputFileName = taskProperties.getOutputDirectory() + "/" + prefixFile + "_" + index + ".xml";
		LOGGER.info("Attempting to write data to {}", outputFileName);
		fileSystem.copyFile(tmpFile, outputFileName);
		tmpFile.delete();
		entries.clear();
	}
}
