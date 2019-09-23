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
package de.interactive_instruments.etf.bsxm.topox;

import java.io.Externalizable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import de.interactive_instruments.SUtils;
import de.interactive_instruments.exceptions.ExcUtils;
import gnu.trove.TLongIntHashMap;

/**
 * Object for persisting topological errors in a XML file
 *
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public class TopologyErrorXmlWriter implements TopologyErrorCollector, Externalizable {

	private static final String TOPOX_ERROR_NS = "http://www.interactive-instruments.de/etf/topology-error/1.0";
	private static final String TOPOX_ERROR_NS_PREFIX = "ete";
	private String themeName;
	private File errorOutputFile;
	private XMLStreamWriter writer;
	private int counter = 0;
	private TLongIntHashMap coordinateHashToPreviousErrorId;

	public TopologyErrorXmlWriter() throws FileNotFoundException, XMLStreamException {}

	public TopologyErrorXmlWriter(final String themeName, final XMLStreamWriter writer) {
		this.themeName = themeName;
		this.writer = writer;
		this.coordinateHashToPreviousErrorId = new TLongIntHashMap(128);
	}

	public TopologyErrorXmlWriter(final String themeName, final XMLStreamWriter writer, final File errorOutputFile) {
		this.themeName = themeName;
		this.writer = writer;
		this.errorOutputFile = errorOutputFile;
		this.coordinateHashToPreviousErrorId = new TLongIntHashMap(128);
	}

	@Override
	public void init() {
		try {
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement(TOPOX_ERROR_NS_PREFIX, "TopologicalErrors", TOPOX_ERROR_NS);
			writer.setPrefix(TOPOX_ERROR_NS_PREFIX, TOPOX_ERROR_NS);
			writer.writeNamespace(TOPOX_ERROR_NS_PREFIX, TOPOX_ERROR_NS);
			writer.writeAttribute("name", themeName);
		} catch (final XMLStreamException e) {
			throw new IllegalStateException("Initialization failed: ", e);
		}
	}

	private XMLStreamWriter createWriter(String themeName, File errorOutputFile)
			throws FileNotFoundException, XMLStreamException {
		final XMLOutputFactory xof = XMLOutputFactory.newInstance();
		final XMLStreamWriter streamWriter = xof.createXMLStreamWriter(new FileOutputStream(errorOutputFile), "UTF-8");
		streamWriter.writeStartDocument("UTF-8", "1.0");
		streamWriter.writeStartElement(TOPOX_ERROR_NS_PREFIX, "TopologicalErrors", TOPOX_ERROR_NS);
		streamWriter.setPrefix(TOPOX_ERROR_NS_PREFIX, TOPOX_ERROR_NS);
		streamWriter.writeNamespace(TOPOX_ERROR_NS_PREFIX, TOPOX_ERROR_NS);
		streamWriter.writeAttribute("name", themeName);
		return streamWriter;
	}

	@Override
	public void collectError(final TopologyErrorType topologyErrorType, final String... parameter) {
		try {
			// error element
			writer.writeStartElement("e");
			// id
			writer.writeAttribute("i", Integer.toString(++counter));
			// errorType
			writer.writeAttribute("t", topologyErrorType.toString());

			collectParameters(parameter);

			writer.writeEndElement();
		} catch (final XMLStreamException e) {
			final String message = themeName + " " +
					topologyErrorType.toString() + " : " + SUtils.concatStr(" ", parameter);
			throw new IllegalStateException("Error writing topological error: " + message, e);
		}
	}

	@Override
	public void collectError(final TopologyErrorType topologyErrorType, final double x, final double y,
			final String... parameter) {
		try {
			// error element
			writer.writeStartElement("e");
			// id
			writer.writeAttribute("i", Integer.toString(++counter));
			// errorType
			writer.writeAttribute("t", topologyErrorType.toString());

			final long coordinateHash = TopologyBuilder.calcCoordHashCode(x, y);
			final int previousErrorI = coordinateHashToPreviousErrorId.get(coordinateHash);
			if (previousErrorI != 0) {
				// previous error
				writer.writeAttribute("p", Integer.toString(previousErrorI));
			} else {
				coordinateHashToPreviousErrorId.put(coordinateHash, counter);
			}
			writer.writeStartElement("X");
			writer.writeCharacters(Double.toString(x));
			writer.writeEndElement();

			writer.writeStartElement("Y");
			writer.writeCharacters(Double.toString(y));
			writer.writeEndElement();

			collectParameters(parameter);

			writer.writeEndElement();
		} catch (final XMLStreamException e) {
			final String message = themeName + " " +
					topologyErrorType.toString() + " : " + SUtils.concatStr(" ", parameter);
			throw new IllegalStateException("Error writing topological error: " + message, e);
		}
	}

	private void collectParameters(final String... parameter) throws XMLStreamException {
		if (parameter != null) {
			for (int i = 0; i < parameter.length; i++) {
				writer.writeStartElement(parameter[i]);
				writer.writeCharacters(parameter[++i]);
				writer.writeEndElement();
			}
		}
	}

	@Override
	public void release() {
		try {
			// TopologicalErrors
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();
		} catch (final XMLStreamException e) {
			ExcUtils.suppress(e);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(themeName);
		out.writeObject(errorOutputFile);
		out.writeInt(counter);
		out.writeObject(coordinateHashToPreviousErrorId);
	}

	@Override
	public void readExternal(final ObjectInput in) throws IOException, ClassNotFoundException {
		this.themeName = in.readUTF();
		this.errorOutputFile = (File) in.readObject();
		this.counter = in.readInt();
		this.coordinateHashToPreviousErrorId = (TLongIntHashMap) in.readObject();
		try {
			this.writer = (XMLStreamWriter) createWriter(this.themeName, this.errorOutputFile);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
}
