package cse232b.visitor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.stream.Stream;

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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import cse232b.parsers.XQueryLexer;
import cse232b.parsers.XQueryParser;

public class XqueryProgramBuilder {

	public static void main(String[] args) {
		// build engine
		System.out.println("test");
		// Parse Input Query
		String inputFile = "test.txt";
		FileInputStream fileInputStream = null;
		ANTLRInputStream antlrInputStream= null;
		try {
			fileInputStream = new FileInputStream(inputFile);
			antlrInputStream = new ANTLRInputStream(fileInputStream);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("input file error");
			System.exit(1);
		}
		System.out.println("input file opened");
		XQueryLexer lexer = new XQueryLexer(antlrInputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		XQueryParser xQueryParser = new XQueryParser(tokenStream);
		ParseTree tree = xQueryParser.ap();
		XqueryExpressionBuilder builder = new XqueryExpressionBuilder();
		ArrayList<Node> result = builder.visit(tree);
		// Generate output xml object
        System.out.println("Query result: " + result);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Document document = documentBuilder.newDocument();
        // Add query result to xml object
        Node xmlResult = document.createElement("Result");
        for (Node node : result) {
        	Node nodeCopy = document.importNode(node, true);
        	xmlResult.appendChild(nodeCopy);
        }
        document.appendChild(xmlResult);
        // Generate output xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource source = new DOMSource(document);
        FileOutputStream xmlFileOutputStream = null;
        try {
        	xmlFileOutputStream = new FileOutputStream("result.xml");
		} catch (Exception e) {
			// TODO: handle exception
		}
        StreamResult streamResult = new StreamResult(xmlFileOutputStream);
        try {
			transformer.transform(source, streamResult);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
