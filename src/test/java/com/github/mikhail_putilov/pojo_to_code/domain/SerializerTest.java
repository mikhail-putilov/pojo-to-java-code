package com.github.mikhail_putilov.pojo_to_code.domain;

import com.github.mikhail_putilov.pojo_to_code.DummyEnum;
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
        String expectedJavaCode = "package com.github.mikhail_putilov.pojo_to_code.domain;\n" +
            "\n" +
            "public class CreateSimplePojo {\n" +
            "\n" +
            "    public SimplePojo createSimplePojo() {\n" +
            "        SimplePojo bean = new SimplePojo();\n" +
            "        bean.setAge(12);\n" +
            "        bean.setIndex(13025);\n" +
            "        bean.setName(\"name1\");\n" +
            "        return bean;\n" +
            "    }\n" +
            "\n" +
            "}\n";
        log.info("actual: {}", actualJavaCode);
        log.info("expected: {}", expectedJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
    }

    @Test
    public void writePojo2ToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojo2());
        String expectedJavaCode = "package com.github.mikhail_putilov.pojo_to_code.domain;\n" +
            "\n" +
            "import java.time.LocalDate;\n" +
            "\n" +
            "public class CreateSimplePojo2 {\n" +
            "\n" +
            "    public SimplePojo2 createSimplePojo2() {\n" +
            "        SimplePojo2 bean = new SimplePojo2();\n" +
            "        bean.setAges(new int[]{12, 13, 14});\n" +
            "        bean.setJustDate(LocalDate.parse(\"2012-03-17\"));\n" +
            "        bean.setName(\"Joe \\\"Captain Crunch\\\" Doe\");\n" +
            "        return bean;\n" +
            "    }\n" +
            "\n" +
            "}\n";
        log.info("actual: {}", actualJavaCode);
        log.info("expected: {}", expectedJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
    }

    @Test
    public void writePojo3ToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojo3());
        String expectedJavaCode = "package com.github.mikhail_putilov.pojo_to_code.domain;\n" +
            "\n" +
            "import java.time.LocalDate;\n" +
            "\n" +
            "public class CreateSimplePojo3 {\n" +
            "\n" +
            "    public SimplePojo2 createSimplePojo2() {\n" +
            "        SimplePojo2 bean = new SimplePojo2();\n" +
            "        bean.setAges(new int[]{12, 13, 14});\n" +
            "        bean.setJustDate(LocalDate.parse(\"2012-03-17\"));\n" +
            "        bean.setName(\"Joe \\\"Captain Crunch\\\" Doe\");\n" +
            "        return bean;\n" +
            "    }\n" +
            "    public SimplePojo3 createSimplePojo3() {\n" +
            "        SimplePojo3 bean = new SimplePojo3();\n" +
            "        bean.setAges(new int[]{12, 13, 14});\n" +
            "        bean.setName(\"okay okay\");\n" +
            "        bean.setRefToPojo(createSimplePojo2());\n" +
            "        return bean;\n" +
            "    }\n" +
            "\n" +
            "}\n";
        log.info("actual: {}", actualJavaCode);
        log.info("expected: {}", expectedJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
    }

    @Test
    public void writePojoWithEnumToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojoWithEnum());
        String expectedJavaCode = "package com.github.mikhail_putilov.pojo_to_code.domain;\n" +
            "\n" +
            "public class CreateSimplePojoWithEnum {\n" +
            "\n" +
            "    public SimplePojoWithEnum createSimplePojoWithEnum() {\n" +
            "        SimplePojoWithEnum bean = new SimplePojoWithEnum();\n" +
            "        bean.setEnumProp(AbcEnum.ABC);\n" +
            "        bean.setIndex(13025);\n" +
            "        return bean;\n" +
            "    }\n" +
            "\n" +
            "}\n";
        log.info("actual: {}", actualJavaCode);
        log.info("expected: {}", expectedJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
    }

    @Test
    public void writePojoWithEnumFromDifferentFileToCode() {
        final String actualJavaCode = serializer.writePojoToCode(new SimplePojoWithEnum2());
        String expectedJavaCode = "package com.github.mikhail_putilov.pojo_to_code.domain;\n" +
            "\n" +
            "import com.github.mikhail_putilov.pojo_to_code.DummyEnum;\n" +
            "\n" +
            "public class CreateSimplePojoWithEnum2 {\n" +
            "\n" +
            "    public SimplePojoWithEnum2 createSimplePojoWithEnum2() {\n" +
            "        SimplePojoWithEnum2 bean = new SimplePojoWithEnum2();\n" +
            "        bean.setEnumProp(DummyEnum.A);\n" +
            "        bean.setIndex(13025);\n" +
            "        return bean;\n" +
            "    }\n" +
            "\n" +
            "}\n";
        log.info("actual: {}", actualJavaCode);
        log.info("expected: {}", expectedJavaCode);
        assertThat(actualJavaCode, is(expectedJavaCode));
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

enum AbcEnum {
    ABC, BCA
}

@Data
class SimplePojoWithEnum {
    AbcEnum enumProp = AbcEnum.ABC;
    long index = 13025;
}

@Data
class SimplePojoWithEnum2 {
    DummyEnum enumProp = DummyEnum.A;
    long index = 13025;
}
