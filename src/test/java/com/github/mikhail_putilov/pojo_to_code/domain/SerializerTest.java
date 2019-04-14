package com.github.mikhail_putilov.pojo_to_code.domain;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SerializerTest {

    @Autowired
    private Serializer serializer;

    @Test
    public void writePojoToCode() {
        final String javaCode = serializer.writePojoToCode(new SimplePojo());
        assertThat(javaCode, is("final SimplePojo simplePojo = new SimplePojo();\n" +
                "simplePojo.setName(\"name1\");\n" +
                "simplePojo.setAge(12);\n" +
                "simplePojo.setIndex(13025);"));
    }
}

@Getter
@Setter
class SimplePojo {
    String name = "name1";
    int age = 12;
    long index = 13025;
}
