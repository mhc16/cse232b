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

import cse232b.parsers.XQueryBaseVisitor;
import cse232b.parsers.XQueryLexer;
import cse232b.parsers.XQueryParser;

public class XqueryProgramBuilder {
	// parse input XPath query, build tree, retrieve results
	private static ArrayList<Node> retrieveXQueryResult(String inputFile, String pattern) {
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
		// retrieve result
		// base pattern
		if (pattern.equals("Op")) {
			XQueryLexer lexer = new XQueryLexer(antlrInputStream);
			CommonTokenStream tokenStream = new CommonTokenStream(lexer);
			XQueryParser xQueryParser = new XQueryParser(tokenStream);
			// change from rp to xq
			ParseTree tree = xQueryParser.xq();
			XqueryRewriteBuilder rewriteBuilder = new XqueryRewriteBuilder();
			String rewrite = rewriteBuilder.visit(tree);
			System.out.println("===\n" + rewrite + "\n===");
			antlrInputStream = new ANTLRInputStream(rewrite);

		}
		XqueryExpressionBuilder builder = new XqueryExpressionBuilder();
		return retrieveExpressionBuilderResult(antlrInputStream, builder);
	}

	// retrieve results from different expression builder
	private static <T> T retrieveExpressionBuilderResult(ANTLRInputStream antlrInputStream,
			XQueryBaseVisitor<T> builder) {
		// build Antlr parse tree
		XQueryLexer lexer = new XQueryLexer(antlrInputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		XQueryParser xQueryParser = new XQueryParser(tokenStream);
		// change from rp to xq
		ParseTree tree = xQueryParser.xq();

		return builder.visit(tree);
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
		document.setXmlStandalone(true);
		// Add query result to xml object
		// single node should not surround by "RESULT"
		if (result.size() == 1) {
			Node nodeCopy = document.importNode(result.get(0), true);
			document.appendChild(nodeCopy);
		} else {
			// more nodes should surround by "result"
			Node xmlResult = document.createElement("RESULT");
			for (Node node : result) {
				Node nodeCopy = document.importNode(node, true);
				xmlResult.appendChild(nodeCopy);
			}
			document.appendChild(xmlResult);
		}

		// document.appendChild(xmlResult);
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
		transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		return transformer;
	}

	public static void main(String[] args) {
		// build engine
		// System.out.println("test");
		// input arguments' length should be 3
		if (args.length != 2 && args.length != 3) {
			System.out.println("Input argument number should be 3!");
			System.out.println("Usage: java -jar cse232b_milestone3.jar [input file] [output file] [0,1]");
			System.out.println("0 represents basic join pattern; 1 represents optimized pattern; 0 is default.");
			System.exit(1);
		}
		// get input file; output file; pattern
		String inputFile = args[0];
		String outputFile = args[1];
		String pattern = "";
		if (args.length == 3) {
			pattern = args[2];
		}
//		String inputFile = "test1.txt";
//        String outputFile = "output3.xml";
//		String pattern = "Op";
		long stime = System.currentTimeMillis();

		// Parse Input Query, retrieve results
		ArrayList<Node> result = retrieveXQueryResult(inputFile, pattern);
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
		long etime = System.currentTimeMillis();
		System.out.printf("execution time in ms %d", (etime - stime));
	}

}
