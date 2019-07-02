package uk.ac.ebi.ddi.task.ddisourcesplitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.ac.ebi.ddi.ddifileservice.DdiFileServiceApplication;
import uk.ac.ebi.ddi.ddifileservice.services.IFileSystem;
import uk.ac.ebi.ddi.ddifileservice.type.ConvertibleOutputStream;
import uk.ac.ebi.ddi.task.ddisourcesplitter.configuration.SourceSplitterTaskProperties;
import uk.ac.ebi.ddi.task.ddisourcesplitter.configuration.TaskConfiguration;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.parser.marshaller.OmicsDataMarshaller;
import uk.ac.ebi.ddi.xml.validator.parser.model.Database;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entries;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication(scanBasePackageClasses = {DdiFileServiceApplication.class, TaskConfiguration.class})
public class DdiSourceSplitterApplication implements CommandLineRunner {

	@Autowired
	private IFileSystem fileSystem;

	@Autowired
	private SourceSplitterTaskProperties taskProperties;

	private List<Entry> entries = new ArrayList<>();

	private static final Logger LOGGER = LoggerFactory.getLogger(DdiSourceSplitterApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(DdiSourceSplitterApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fileSystem.cleanDirectory(taskProperties.getOutputDirectory());
		AtomicInteger index = new AtomicInteger(0);
		AtomicInteger outIndex = new AtomicInteger(1);

		List<String> files = fileSystem.listFilesFromFolder(taskProperties.getInputDirectory());
		for (String filePath : files) {
			LOGGER.info("Processing file {}", filePath);
			process(filePath, index, files.size(), outIndex);
		}
	}

	public void process(String filePath, AtomicInteger index, int total, AtomicInteger outIndex) throws Exception {
		index.getAndIncrement();
		if (!filePath.contains(taskProperties.getOriginalPrefix())) {
			return;
		}
		File file = fileSystem.getFile(filePath);
		OmicsXMLFile reader = new OmicsXMLFile(file);
		for (String id: reader.getEntryIds()) {

			LOGGER.info("The ID: {} will be enriched!!", id);
			Entry dataset = reader.getEntryById(id);

			entries.add(dataset);

			if (entries.size() == taskProperties.getNumberEntries()) {
				writeData(reader, entries, outIndex.get());
				entries.clear();
				outIndex.getAndIncrement();
			}
		}
		if (index.get() == total && !entries.isEmpty()) {
			writeData(reader, entries, outIndex.get());
		}
	}

	private void writeData(OmicsXMLFile reader, List<Entry> data, int postfix) throws IOException {
		String prefixFile = taskProperties.getFilePrefix();
		String outputFileName = taskProperties.getOutputDirectory() + "/" + prefixFile + "_" + postfix + ".xml";
		try (ConvertibleOutputStream outputStream = new ConvertibleOutputStream()) {
			OmicsDataMarshaller outputXMLFile = new OmicsDataMarshaller();
			Database database = new Database();
			database.setDescription(reader.getDescription());
			database.setName(reader.getName());
			database.setRelease(reader.getRelease());
			database.setReleaseDate(reader.getReleaseDate());
			database.setEntryCount(data.size());
			database.setEntries(new Entries(data));
			outputXMLFile.marshall(database, outputStream);
			fileSystem.saveFile(outputStream, outputFileName);
		}
	}
}
