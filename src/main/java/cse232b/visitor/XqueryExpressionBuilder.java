package cse232b.visitor;

//java package
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//antlr4 package
import cse232b.parsers.XQueryBaseVisitor;
import cse232b.parsers.XQueryParser;

//xml package
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XqueryExpressionBuilder extends XQueryBaseVisitor<ArrayList<Node>> {
	// use a nodelist to store the current visit node
	private ArrayList<Node> curNodes = new ArrayList<Node>();

	// util function
	// remove duplicate node
	private ArrayList<Node> removeDuplicates(ArrayList<Node> list) {
		ArrayList<Node> result = new ArrayList<>();
		HashSet<Node> unique = new HashSet<>();
		for (Node ele : list) {
			if (!unique.contains(ele)) {
				unique.add(ele);
				result.add(ele);
			}
		}
		return result;
	}

	// return descendants of a single node
	private ArrayList<Node> visitNodeDescendants(Node node) {
		ArrayList<Node> result = new ArrayList<>();
		result.add(node);
		NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			// DFS
			result.addAll(visitNodeDescendants(children.item(i)));
		}
		return result;
	}

	// return all descendants of input node list
	private ArrayList<Node> visitNodeListDescendants(ArrayList<Node> list) {
		ArrayList<Node> result = new ArrayList<>();
		for (Node ele : list) {
			result.addAll(visitNodeDescendants(ele));
		}
		return removeDuplicates(result);
	}

	// doc then rp
	@Override
	public ArrayList<Node> visitApChildren(XQueryParser.ApChildrenContext ctx) {
		visit(ctx.doc());
		System.out.println(curNodes);
		return visit(ctx.rp());

	}

	// doc then iterate all descendants then rp
	@Override
	public ArrayList<Node> visitApAllDescendants(XQueryParser.ApAllDescendantsContext ctx) {
		visit(ctx.doc());
		curNodes = visitNodeListDescendants(curNodes);
//		System.out.println(curNodes);
		return visit(ctx.rp());
	}

	// open doc file, first function of each query, todo
	@Override
	public ArrayList<Node> visitDocFile(XQueryParser.DocFileContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		String fileName = ctx.fname().getText();
		// read file from resource folder
		File fileDOM = new File(fileName);
//		ClassLoader classLoader = getClass().getClassLoader();
//		try {
//			URL targetURL = classLoader.getResource(fileName);
//			// file not exist
//			if (targetURL == null) {
//				throw new IllegalArgumentException(fileName + "not found!");
//			}
//			fileDOM = new File(targetURL.toURI());
//		} catch (Exception e) {
//			System.out.println("file not found!");
//			System.exit(1);
//		}
		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			Document document = documentBuilder.parse(fileDOM);
			// add doc node
			result.add(document);
			curNodes = result;
		} catch (Exception e) {
			System.out.println(e);
			System.exit(1);
		}
		// System.out.println("file open!");
		return result;
	}

	// nothing to do
	@Override
	public ArrayList<Node> visitFileName(XQueryParser.FileNameContext ctx) {
		return super.visitFileName(ctx);
	}

	// get element node with required attributes
	@Override
	public ArrayList<Node> visitAttrName(XQueryParser.AttrNameContext ctx) {
		String require = ctx.WORD().getText();
		ArrayList<Node> result = new ArrayList<>();
		for (Node node : curNodes) {
			NamedNodeMap nodeAttrMap = node.getAttributes();
			// element node && require attr
			if (nodeAttrMap != null && nodeAttrMap.getNamedItem(require) != null) {
				result.add(node);
			}
		}
		curNodes = result;
		return result;
	}

	// get all text node
	@Override
	public ArrayList<Node> visitTxt(XQueryParser.TxtContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		for (Node node : curNodes) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.TEXT_NODE) {
					result.add(children.item(i));
				}
			}
		}
		curNodes = result;
		return result;
	}

	// get element node with tag name
	@Override
	public ArrayList<Node> visitTagName(XQueryParser.TagNameContext ctx) {
		String require = ctx.WORD().getText();
		ArrayList<Node> result = new ArrayList<>();
//		System.out.println("1"+require);
//		System.out.println(curNodes);
		for (Node node : curNodes) {
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE
						&& children.item(i).getNodeName().equals(require)) {
					result.add(children.item(i));
				}
			}
		}
		curNodes = result;
//		System.out.println("2");
//		System.out.println(curNodes);
		return result;
	}

	// get parent node for each current node
	@Override
	public ArrayList<Node> visitParent(XQueryParser.ParentContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		for (Node node : curNodes) {
			result.add(node.getParentNode());
		}
		result = removeDuplicates(result);
		curNodes = result;
		return result;
	}

	// rp1 / rp2
	@Override
	public ArrayList<Node> visitRpChildren(XQueryParser.RpChildrenContext ctx) {
		ArrayList<Node> result = null;
		visit(ctx.rp(0));
		result = visit(ctx.rp(1));
		curNodes = result;
		return result;
	}

	// rp1 // rp2
	@Override
	public ArrayList<Node> visitRpAllDescendants(XQueryParser.RpAllDescendantsContext ctx) {
		ArrayList<Node> result = null;
		visit(ctx.rp(0));
		curNodes = visitNodeListDescendants(curNodes);
		result = visit(ctx.rp(1));
		curNodes = result;
		return result;
	}

	// all children node of element node in document order
	@Override
	public ArrayList<Node> visitChildren(XQueryParser.ChildrenContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		for (Node node : curNodes) {
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				NodeList children = node.getChildNodes();
				for (int i = 0; i < children.getLength(); i++) {
					result.add(children.item(i));
				}
			}
		}
		curNodes = result;
		return result;
	}

	// visit current node
	@Override
	public ArrayList<Node> visitSelf(XQueryParser.SelfContext ctx) {
		return new ArrayList<>(curNodes);
	}

	// rp1 , rp2
	@Override
	public ArrayList<Node> visitRpConcat(XQueryParser.RpConcatContext ctx) {
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> result = visit(ctx.rp(0));
		curNodes = curNodesCopy;
		result.addAll(visit(ctx.rp(1)));
		result = removeDuplicates(result);
		curNodes = result;
		return result;
	}

	// rp [ filter ]
	@Override
	public ArrayList<Node> visitRpFilter(XQueryParser.RpFilterContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = visit(ctx.rp());
		for (Node node : curNodesCopy) {
			ArrayList<Node> temp = new ArrayList<>();
			temp.add(node);
			curNodes = temp;
			// filter condition
			if (!visit(ctx.filter()).isEmpty()) {
				result.add(node);
			}
		}
		curNodes = result;
		return result;
	}

	// ( rp )
	@Override
	public ArrayList<Node> visitRpBrackets(XQueryParser.RpBracketsContext ctx) {
		return visit(ctx.rp());
	}

	// rp1 = rp2 , rp1 eq rp2, use isEqualNode
	@Override
	public ArrayList<Node> visitFilterEqual(XQueryParser.FilterEqualContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> rp1NodeList = visit(ctx.rp(0));
		curNodes = curNodesCopy;
		ArrayList<Node> rp2NodeList = visit(ctx.rp(1));
		for (Node node1 : rp1NodeList) {
			for (Node node2 : rp2NodeList) {
				if (node1.isEqualNode(node2)) {
					result.add(node1);
				}
			}
		}
		curNodes = result;
		return result;
	}

	// not filter, use removeAll
	@Override
	public ArrayList<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = curNodes;
		ArrayList<Node> fil1NodeList = visit(ctx.filter());
		if (fil1NodeList.isEmpty()) {
			result = curNodesCopy;
			curNodes = curNodesCopy;
		}
		return result;
	}

	// ( filter )
	@Override
	public ArrayList<Node> visitFilterBrackets(XQueryParser.FilterBracketsContext ctx) {
		return visit(ctx.filter());
	}

	// filter1 or filter2
	@Override
	public ArrayList<Node> visitFilterOr(XQueryParser.FilterOrContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> fil1NodeList = visit(ctx.filter(0));
		curNodes = curNodesCopy;
		ArrayList<Node> fil2NodeList = visit(ctx.filter(1));
		if (!fil1NodeList.isEmpty() || !fil2NodeList.isEmpty()) {
			result.addAll(fil1NodeList);
			result.addAll(fil2NodeList);
		}
		result = removeDuplicates(result);
		curNodes = result;
		return result;
	}

	// filter1 and filter2
	@Override
	public ArrayList<Node> visitFilterAnd(XQueryParser.FilterAndContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> fil1NodeList = visit(ctx.filter(0));
		curNodes = curNodesCopy;
		ArrayList<Node> fil2NodeList = visit(ctx.filter(1));
		if (!fil1NodeList.isEmpty() && !fil2NodeList.isEmpty()) {
			result.addAll(fil1NodeList);
			result.addAll(fil2NodeList);
		}
		result = removeDuplicates(result);
		curNodes = result;
		return result;
	}

	// exist node under Rp
	@Override
	public ArrayList<Node> visitFilterRp(XQueryParser.FilterRpContext ctx) {
		return visit(ctx.rp());
	}

	// rp1 == rp2 , rp1 is rp2, use isSameNode
	@Override
	public ArrayList<Node> visitFilterIs(XQueryParser.FilterIsContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> rp1NodeList = visit(ctx.rp(0));
		curNodes = curNodesCopy;
		ArrayList<Node> rp2NodeList = visit(ctx.rp(1));
		for (Node node1 : rp1NodeList) {
			for (Node node2 : rp2NodeList) {
				if (node1.isSameNode(node2)) {
					result.add(node1);
				}
			}
		}
		curNodes = result;
		return result;
	}

	// rp = StringConstant
	@Override
	public ArrayList<Node> visitFilterString(XQueryParser.FilterStringContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		String require = ctx.StringConstant().getText();
		require = require.substring(1, (require.length()-1));
		curNodes = visit(ctx.rp());
		for (Node node : curNodes) {
			if ((node.getNodeType() == Node.TEXT_NODE || node.getNodeType() == Node.ATTRIBUTE_NODE)
					&& node.getTextContent().equals(require)) {
				result.add(node);
			}
		}
		curNodes = result;
		return result;
	}

}