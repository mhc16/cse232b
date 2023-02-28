package cse232b.visitor;
// java io
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
// xml
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
// anltr4
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import cse232b.parsers.XQueryLexer;
import cse232b.parsers.XQueryParser;

public class XqueryProgramBuilder {
	// parse input XPath query, build tree, retrieve results
	private static ArrayList<Node> retrieveXQueryResult(String inputFile) {
		// build antlr input file stream
		FileInputStream fileInputStream = null;
		ANTLRInputStream antlrInputStream = null;
		try {
			fileInputStream = new FileInputStream(inputFile);
			antlrInputStream = new ANTLRInputStream(fileInputStream);
		} catch (Exception e) {
			System.out.println("Input file error!");
			System.exit(1);
		}
		// build Antlr parse tree
		XQueryLexer lexer = new XQueryLexer(antlrInputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		XQueryParser xQueryParser = new XQueryParser(tokenStream);
		ParseTree tree = xQueryParser.xq();
		XqueryExpressionBuilder builder = new XqueryExpressionBuilder();
		// retrieve results
		ArrayList<Node> result = builder.visit(tree);
		return result;
	}

	// generate XML document
	private static Document xmlDocumentGenerator(ArrayList<Node> result) {
		// implement document builder
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			System.out.println("Document builder failed!");
			System.exit(1);
		}
		Document document = documentBuilder.newDocument();
		// Add query result to xml object
		Node xmlResult = document.createElement("RESULT");
		for (Node node : result) {
			Node nodeCopy = document.importNode(node, true);
			xmlResult.appendChild(nodeCopy);
		}
		document.appendChild(xmlResult);
		return document;
	}

	// generate XML output file
	private static Transformer transformerGenerator() {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			System.out.println("Transformer builder failed!");
			System.exit(1);
		}
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		return transformer;
	}

	public static void main(String[] args) {
		// build engine
		// System.out.println("test");
		// input arguments' length should be 2
//		if (args.length != 2) {
//			System.out.println("Input argument number should be 2!");
//			System.exit(1);
//		}
		String inputFile = "text.txt";
		String outputFile = "outputxq.xml";
		// Parse Input Query, retrieve results
		ArrayList<Node> result = retrieveXQueryResult(inputFile);
		// System.out.println(result);
		// Generate output xml object
		Document document = xmlDocumentGenerator(result);
		// Generate output xml file
		Transformer transformer = transformerGenerator();
		DOMSource source = new DOMSource(document);
		// Build output stream
		FileOutputStream xmlFileOutputStream = null;
		try {
			xmlFileOutputStream = new FileOutputStream(outputFile);
		} catch (Exception e) {
			System.out.println("Output stream failed!");
			System.exit(1);
		}
		StreamResult streamResult = new StreamResult(xmlFileOutputStream);
		// Transform process
		try {
			transformer.transform(source, streamResult);
		} catch (TransformerException e) {
			System.out.println("Transform process failed!");
			System.exit(1);
		}
	}

}
