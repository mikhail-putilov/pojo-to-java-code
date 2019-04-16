package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SerializerTest {

    @Autowired
    private Serializer serializer;

    @Test
    public void writePojoToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojo());
        String expectedJavaCode = "public SimplePojo createSimplePojo() {\n" +
            "    SimplePojo simplePojo = new SimplePojo();\n" +
            "    simplePojo.setAge(12);\n" +
            "    simplePojo.setName(\"name1\");\n" +
            "    simplePojo.setIndex(13025);\n" +
            "    return simplePojo;\n" +
            "}";
        log.info(actualJavaCode);
        log.info(expectedJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
    }

    @Test
    public void writePojo2ToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojo2());
        String expectedJavaCode = "public SimplePojo2 createSimplePojo2() {\n" +
            "    SimplePojo2 simplePojo2 = new SimplePojo2();\n" +
            "    simplePojo2.setAges(new int[]{12, 13, 14});\n" +
            "    simplePojo2.setJustDate(LocalDate.parse(\"2012-03-17\"));\n" +
            "    simplePojo2.setName(\"Joe \\\"Captain Crunch\\\" Doe\");\n" +
            "    return simplePojo2;\n" +
            "}";
        log.info(expectedJavaCode);
        log.info(actualJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
    }

    @Test
    public void writePojo3ToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojo3());
        log.info(actualJavaCode);
    }
}

@Data
class SimplePojo {
    String name = "name1";
    int age = 12;
    long index = 13025;
}

@Data
class SimplePojo2 {
    String name = "Joe \"Captain Crunch\" Doe";
    int[] ages = {12, 13, 14};
    LocalDate justDate = LocalDate.of(2012, 3, 17);
}
@Data
class SimplePojo3 {
    String name = "okay okay";
    int[] ages = {12, 13, 14};
    SimplePojo2 refToPojo = new SimplePojo2();
}
