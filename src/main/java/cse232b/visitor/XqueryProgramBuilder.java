package cse232b.visitor;

import java.io.FileInputStream;
import java.util.ArrayList;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
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
	}

}
