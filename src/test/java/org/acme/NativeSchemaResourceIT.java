package org.acme;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeSchemaResourceIT extends SchemaResourceTest {
    // Execute the same tests but in native mode.
}