package uk.ac.ebi.ddi.task.ddisourcesplitter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.ddi.ddifileservice.services.IFileSystem;
import uk.ac.ebi.ddi.task.ddisourcesplitter.configuration.SourceSplitterTaskProperties;
import uk.ac.ebi.ddi.xml.validator.parser.OmicsXMLFile;
import uk.ac.ebi.ddi.xml.validator.utils.Tuple;

import java.io.File;
import java.util.List;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DdiSourceSplitterApplication.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@TestPropertySource(properties = {
        "file.provider=local",
        "splitter.input_directory=/tmp/splitter/input",
        "splitter.output_directory=/tmp/splitter/output",
        "splitter.file_prefix=output",
        "splitter.number_entries=1",
        "splitter.original_prefix=arrayexpress",
})
public class ITLocalSourceSplitterApplicationTest {

    @Autowired
    private IFileSystem fileSystem;

    @Autowired
    private SourceSplitterTaskProperties taskProperties;

    @Autowired
    private DdiSourceSplitterApplication application;

    @Before
    public void setUp() throws Exception {
        new File(taskProperties.getInputDirectory()).mkdirs();
        new File(taskProperties.getOutputDirectory()).mkdirs();

        File testFile = new File(getClass().getClassLoader().getResource("generated_file.xml").getFile());
        fileSystem.copyFile(testFile, taskProperties.getInputDirectory() + "/arrayexpress-test.xml");
    }

    @Test
	public void contextLoads() throws Exception {
        application.run();
        List<String> outFiles = fileSystem.listFilesFromFolder(taskProperties.getOutputDirectory());
        Assert.assertEquals(3, outFiles.size());
        for (String filePath : outFiles) {
            File file = fileSystem.getFile(filePath);
            Assert.assertTrue(OmicsXMLFile.hasFileHeader(file));
            List<Tuple> errors = OmicsXMLFile.validateSchema(file);
            Assert.assertEquals(1, errors.size());
            Assert.assertEquals("cvc-complex-type.4: Attribute 'acc' must appear on element 'entry'.",
                    errors.get(0).getValue());
        }
	}

    @After
    public void tearDown() throws Exception {
        fileSystem.cleanDirectory(taskProperties.getInputDirectory());
        fileSystem.cleanDirectory(taskProperties.getOutputDirectory());
    }
}
