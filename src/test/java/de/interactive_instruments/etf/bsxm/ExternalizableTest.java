/**
 * Copyright 2010-2018 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.bsxm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;

import de.interactive_instruments.IFile;

/**
 * @author Christoph Spalek ( spalek aT interactive-instruments doT de )
 */
public class ExternalizableTest {

	private final static String DB_BASE_NAME = "testDB-000";
	private final static String THEME_NAME = "testTheme";
	private final static IFile OUTPUT_FILE = new IFile("build/tmp/externalizable_file");
	private final static IFile OUTPUT_DIR = new IFile("build/tmp");
	private FeatureParsingStore parsingStore = new FeatureParsingStore(DB_BASE_NAME);

	@Test
	public void testSerializeObject() throws IOException, ClassNotFoundException {

		TopoX t = new TopoX();
		t.initDb(DB_BASE_NAME, (short) 1);
		int id = t.newTopologyBuilder(THEME_NAME, 1995000, OUTPUT_DIR.getAbsolutePath());

		parsingStore.parseSimpleFeature(t, id);

		// Serialize object
		FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		try {
			t.writeExternal(objectOutputStream);
		} catch (final IOException e) {
			fail("Could not serialize TopoX object.", e);
		}
	}

	@Test
	public void testDeserializeObject() throws IOException, ClassNotFoundException {
		TopoX t = new TopoX();
		t.initDb(DB_BASE_NAME, (short) 1);
		int id = t.newTopologyBuilder(THEME_NAME, 1995000, OUTPUT_DIR.getAbsolutePath());

		parsingStore.parseSimpleFeature(t, id);

		// Serialize object
		FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		t.writeExternal(objectOutputStream);

		objectOutputStream.flush();
		objectOutputStream.close();
		fileOutputStream.close();
		t = null;

		// Deserialize object
		FileInputStream fileInputStream = new FileInputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		TopoX t2 = new TopoX();
		try {
			t2.readExternal(objectInputStream);
		} catch (final IOException e) {
			fail("Could not deserialize TopoX object.", e);
		}
	}

	@Test
	public void testDeserializedDbName() throws IOException, ClassNotFoundException {
		TopoX t = new TopoX();
		t.initDb(DB_BASE_NAME, (short) 1);
		int id = t.newTopologyBuilder(THEME_NAME, 1995000, OUTPUT_DIR.getAbsolutePath());

		parsingStore.parseSimpleFeature(t, id);

		final String expectedDbName = t.dbname(id);

		// Serialize object
		FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		t.writeExternal(objectOutputStream);

		objectOutputStream.flush();
		objectOutputStream.close();
		fileOutputStream.close();
		t = null;

		// Deserialize object
		FileInputStream fileInputStream = new FileInputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		TopoX t2 = new TopoX();
		t2.readExternal(objectInputStream);

		final String deserializedDbName = t2.dbname(0);

		assertEquals(expectedDbName, deserializedDbName);
	}

	@Test
	public void testDeserializedFreeStandingSurfaceFailOutput() throws IOException, ClassNotFoundException {

		TopoX t = new TopoX();
		t.initDb(DB_BASE_NAME, (short) 1);
		int id = t.newTopologyBuilder(THEME_NAME, 1995000, OUTPUT_DIR.getAbsolutePath());

		parsingStore.parseFreeStandingSurfaceFail(t, id);

		// Serialize object
		FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		t.writeExternal(objectOutputStream);

		t.detectFreeStandingSurfaces(id);
		String expectedErrorFilePath = t.errorFile(0);

		objectOutputStream.flush();
		objectOutputStream.close();
		fileOutputStream.close();
		t = null;

		// Save initial error file
		IFile expectedErrorFile = new IFile(expectedErrorFilePath);
		String expectedErrorFileContent = null;
		try {
			expectedErrorFileContent = expectedErrorFile.readContent("UTF-8").toString();
		} catch (final IOException e) {
			fail("Could not read " + expectedErrorFile.getAbsolutePath(), e);
		}

		// Deserialize object
		FileInputStream fileInputStream = new FileInputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		TopoX t2 = new TopoX();
		t2.readExternal(objectInputStream);

		t2.detectFreeStandingSurfaces(0);
		String deserializedErrorFilePath = t2.errorFile(0);

		// Save deserialized error file
		IFile deserializedErrorFile = new IFile(deserializedErrorFilePath);
		String deserializedErrorFileContent = null;
		try {
			deserializedErrorFileContent = deserializedErrorFile.readContent("UTF-8").toString();
		} catch (final IOException e) {
			fail("Could not read " + deserializedErrorFile.getAbsolutePath(), e);
		}
		objectInputStream.close();
		fileInputStream.close();

		assertEquals(expectedErrorFileContent, deserializedErrorFileContent);
	}

	@Test
	public void testDeserializedDetectHolesPassOutput() throws IOException, ClassNotFoundException {

		TopoX t = new TopoX();
		t.initDb(DB_BASE_NAME, (short) 1);
		int id = t.newTopologyBuilder(THEME_NAME, 1995000, OUTPUT_DIR.getAbsolutePath());

		parsingStore.parseDetectHolesPass(t, id);

		// Serialize object
		FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		t.writeExternal(objectOutputStream);

		t.detectHoles(id);
		String expectedErrorFilePath = t.errorFile(0);

		objectOutputStream.flush();
		objectOutputStream.close();
		fileOutputStream.close();
		t = null;

		// Save initial error file
		IFile expectedErrorFile = new IFile(expectedErrorFilePath);
		String expectedErrorFileContent = null;
		try {
			expectedErrorFileContent = expectedErrorFile.readContent("UTF-8").toString();
		} catch (final IOException e) {
			fail("Could not read " + expectedErrorFile.getAbsolutePath(), e);
		}

		// Deserialize object
		FileInputStream fileInputStream = new FileInputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		TopoX t2 = new TopoX();
		t2.readExternal(objectInputStream);

		t2.detectHoles(0);
		String deserializedErrorFilePath = t2.errorFile(0);

		// Save deserialized error file
		IFile deserializedErrorFile = new IFile(deserializedErrorFilePath);
		String deserializedErrorFileContent = null;
		try {
			deserializedErrorFileContent = deserializedErrorFile.readContent("UTF-8").toString();
		} catch (final IOException e) {
			fail("Could not read " + deserializedErrorFile.getAbsolutePath(), e);
		}
		objectInputStream.close();
		fileInputStream.close();

		assertEquals(expectedErrorFileContent, deserializedErrorFileContent);
	}

	@Test
	public void testDeserializedDetectHolesFailOutput() throws IOException, ClassNotFoundException {

		TopoX t = new TopoX();
		t.initDb(DB_BASE_NAME, (short) 1);
		int id = t.newTopologyBuilder(THEME_NAME, 1995000, OUTPUT_DIR.getAbsolutePath());

		parsingStore.parseDetectHolesFail(t, id);

		// Serialize object
		FileOutputStream fileOutputStream = new FileOutputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
		t.writeExternal(objectOutputStream);

		t.detectHoles(id);
		String expectedErrorFilePath = t.errorFile(0);

		objectOutputStream.flush();
		objectOutputStream.close();
		fileOutputStream.close();
		t = null;

		// Save initial error file
		IFile expectedErrorFile = new IFile(expectedErrorFilePath);
		String expectedErrorFileContent = null;
		try {
			expectedErrorFileContent = expectedErrorFile.readContent("UTF-8").toString();
		} catch (final IOException e) {
			fail("Could not read " + expectedErrorFile.getAbsolutePath(), e);
		}

		// Deserialize object
		FileInputStream fileInputStream = new FileInputStream(OUTPUT_FILE.getAbsoluteFile());
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		TopoX t2 = new TopoX();
		t2.readExternal(objectInputStream);

		t2.detectHoles(0);
		String deserializedErrorFilePath = t2.errorFile(0);

		// Save deserialized error file
		IFile deserializedErrorFile = new IFile(deserializedErrorFilePath);
		String deserializedErrorFileContent = null;
		try {
			deserializedErrorFileContent = deserializedErrorFile.readContent("UTF-8").toString();
		} catch (final IOException e) {
			fail("Could not read " + deserializedErrorFile.getAbsolutePath(), e);
		}
		objectInputStream.close();
		fileInputStream.close();

		assertEquals(expectedErrorFileContent, deserializedErrorFileContent);
	}
}
