package cse232b.visitor;

//java package
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
	private HashMap<String, ArrayList<Node>> VarsMap = new HashMap<>();
	private Document outFile;

	XqueryExpressionBuilder() {
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			outFile = docBuilder.newDocument();
		} catch (Exception e) {
			System.out.println("Initialize docBuilder failed!");
			System.exit(1);
		}
	}

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

	// help function for XqFLWR function
	private void subVisitFLWR(int k, XQueryParser.XqFLWRContext ctx, ArrayList<Node> res) {
		// for clause part
		if (k < ctx.forClause().var().size()) {
			String varname = ctx.forClause().var(k).getText();
			ArrayList<Node> values = visit(ctx.forClause().xq(k));
			ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);

			for (Node item : values) {
				ArrayList<Node> temp = new ArrayList<>();
				// backup
				HashMap<String, ArrayList<Node>> mapCopy = new HashMap<>(VarsMap);
				temp.add(item);
				curNodes = temp;
				VarsMap.put(varname, temp);
				subVisitFLWR(k + 1, ctx, res);
				// restore
				VarsMap = mapCopy;
			}
			curNodes = curNodesCopy;
		} else {
			// let clause part if exist
			if (ctx.letClause() != null) {
				visit(ctx.letClause());
			}
			// where clause part if exist
			if (ctx.whereClause() != null) {
				if (visit(ctx.whereClause()).isEmpty()) {
					return;
				}
			}
			// add all results
			res.addAll(visit(ctx.returnClause()));
		}
	}

	// help function for cond some function
	private boolean condSomeHelper(XQueryParser.CondSomeContext ctx, int index) {
		// end of var, judge condition, if not empty then true
		if (index == ctx.var().size()) {
			return !visit(ctx.cond()).isEmpty();
		}
		// not the end of var
		else {
			String var = ctx.var(index).getText();
			ArrayList<Node> xqList = visit(ctx.xq(index));
			// iterate variable in Xq value (for)
			// need to restore context for each level of variable
			HashMap<String, ArrayList<Node>> contextMapBackup = new HashMap<>(VarsMap);
			for (Node node : xqList) {
				ArrayList<Node> newValue = new ArrayList<>();
				newValue.add(node);
				VarsMap.put(var, newValue);
				// if one satisfy condition, then return true
				if (condSomeHelper(ctx, index + 1)) {
					VarsMap = contextMapBackup;
					return true;
				}
				VarsMap = contextMapBackup;
			}
		}
		return false;
	}

	// xq methods
	// return context var value, empty if not exist
	@Override
	public ArrayList<Node> visitXqVar(XQueryParser.XqVarContext ctx) {
		ArrayList<Node> res = new ArrayList<>();
		// key not exist
		if (!VarsMap.containsKey(ctx.var().getText())) {
			return res;
		}
		res.addAll(VarsMap.get(ctx.var().getText()));
		curNodes = res;
		return res;
	}

	// StringConstant, implement makeText() function
	@Override
	public ArrayList<Node> visitXqString(XQueryParser.XqStringContext ctx) {
		String temp = ctx.getText();
		temp = temp.substring(1, temp.length() - 1);
		Node tempTxtNode = outFile.createTextNode(temp);
		ArrayList<Node> res = new ArrayList<>();
		res.add(tempTxtNode);
		return res;
	}

	// xq / rp
	@Override
	public ArrayList<Node> visitXqChildren(XQueryParser.XqChildrenContext ctx) {
		curNodes = visit(ctx.xq());
		ArrayList<Node> res = visit(ctx.rp());
		curNodes = res;
		return res;
	}

	// ap
	@Override
	public ArrayList<Node> visitXqAp(XQueryParser.XqApContext ctx) {
		return visit(ctx.ap());
	}

	// <tag> xq </tag>, implement makeElem() function
	@Override
	public ArrayList<Node> visitXqInTag(XQueryParser.XqInTagContext ctx) {
		ArrayList<Node> temps = visit(ctx.xq());
		ArrayList<Node> res = new ArrayList<>();
		Node tempNode = outFile.createElement(ctx.WORD(0).getText());
		for (Node item : temps) {
			if (item != null) {
				Node tempChild = outFile.importNode(item, true);
				tempNode.appendChild(tempChild);
			}
		}
		res.add(tempNode);
		return res;
	}

	// xq // rp
	@Override
	public ArrayList<Node> visitXqAllDescendants(XQueryParser.XqAllDescendantsContext ctx) {
		curNodes = visit(ctx.xq());
		curNodes = visitNodeListDescendants(curNodes);
		ArrayList<Node> res = visit(ctx.rp());
		res = removeDuplicates(res);
		curNodes = res;
		return res;
	}

	// letClause xq, make adjustment to context
	@Override
	public ArrayList<Node> visitXqLetClause(XQueryParser.XqLetClauseContext ctx) {
		// backup
		HashMap<String, ArrayList<Node>> mapCopy = new HashMap<>(VarsMap);
		visit(ctx.letClause());
		ArrayList<Node> res = visit(ctx.xq());
		// restore
		VarsMap = mapCopy;
		return res;
	}

	// xq1 , xq2
	@Override
	public ArrayList<Node> visitXqConcat(XQueryParser.XqConcatContext ctx) {
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> result = visit(ctx.xq(0));
		curNodes = curNodesCopy;
		result.addAll(visit(ctx.xq(1)));
		result = removeDuplicates(result);
		curNodes = result;
		return result;
	}

	// ( xq )
	@Override
	public ArrayList<Node> visitXqBrackets(XQueryParser.XqBracketsContext ctx) {
		return visit(ctx.xq());
	}

	// forClause letClause? whereClause? returnClause
	@Override
	public ArrayList<Node> visitXqFLWR(XQueryParser.XqFLWRContext ctx) {
		// backup context
		HashMap<String, ArrayList<Node>> mapCopy = new HashMap<>(VarsMap);
		ArrayList<Node> res = new ArrayList<>();
		// use recursion function
		subVisitFLWR(0, ctx, res);
		// restore
		VarsMap = mapCopy;
		return res;
	}

	// nothing to do
	@Override
	public ArrayList<Node> visitVar(XQueryParser.VarContext ctx) {
		return new ArrayList<>();
	}

	// let var := xq ... , mainly make adjustments to the context
	@Override
	public ArrayList<Node> visitLetClause(XQueryParser.LetClauseContext ctx) {
		int k = ctx.var().size();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		for (int i = 0; i < k; i++) {
			String varname = ctx.var(i).getText();
			VarsMap.put(varname, visit(ctx.xq(i)));
			curNodes = new ArrayList<>(curNodesCopy);
		}
		curNodes = curNodesCopy;
		return new ArrayList<>();
	}

	// where cond, visit condition
	@Override
	public ArrayList<Node> visitWhereClause(XQueryParser.WhereClauseContext ctx) {
		return visit(ctx.cond());
	}

	// return xq
	@Override
	public ArrayList<Node> visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
		return visit(ctx.xq());
	}

	// cond1 or cond2
	@Override
	public ArrayList<Node> visitCondOr(XQueryParser.CondOrContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> cond1Res = visit(ctx.cond(0));
		curNodes = curNodesCopy;
		ArrayList<Node> cond2Res = visit(ctx.cond(1));
		// if not empty
		if (!cond1Res.isEmpty() || !cond2Res.isEmpty()) {
			result.addAll(cond1Res);
			result.addAll(cond2Res);
		}
		result = removeDuplicates(result);
		curNodes = curNodesCopy;

		return result;
	}

	// cond1 and cond2
	@Override
	public ArrayList<Node> visitCondAnd(XQueryParser.CondAndContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> cond1Res = visit(ctx.cond(0));
		curNodes = curNodesCopy;
		ArrayList<Node> cond2Res = visit(ctx.cond(1));
		// if not empty
		if (!cond1Res.isEmpty() && !cond2Res.isEmpty()) {
			result.addAll(cond1Res);
			result.addAll(cond2Res);
		}
		result = removeDuplicates(result);
		curNodes = curNodesCopy;
		return result;
	}

	// empty ( xq )
	@Override
	public ArrayList<Node> visitCondEmpty(XQueryParser.CondEmptyContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> xqRes = visit(ctx.xq());
		if (xqRes.isEmpty()) {
			result.add(outFile.createElement("Empty"));
		}
		curNodes = curNodesCopy;
		return result;
	}

	// some var in xq (, var in xq) satisfies cond
	@Override
	public ArrayList<Node> visitCondSome(XQueryParser.CondSomeContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		boolean satisfy = condSomeHelper(ctx, 0);
		curNodes = curNodesCopy;
		if (satisfy) {
			result.add(outFile.createElement("Satisfy"));
		}
		return result;
	}

	// xq1 == xq2 ,xq1 is xq2, use isSameNode
	@Override
	public ArrayList<Node> visitCondIs(XQueryParser.CondIsContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> xq1Res = visit(ctx.xq(0));
		curNodes = curNodesCopy;
		ArrayList<Node> xq2Res = visit(ctx.xq(1));
		for (Node node1 : xq1Res) {
			for (Node node2 : xq2Res) {
				if (node1.isSameNode(node2)) {
					result.add(node1);
				}
			}
		}
		curNodes = curNodesCopy;
		return result;
	}

	// not cond
	@Override
	public ArrayList<Node> visitCondNot(XQueryParser.CondNotContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> condRes = visit(ctx.cond());
		if (condRes.isEmpty()) {
			result.add(outFile.createElement("Empty"));
		}
		curNodes = curNodesCopy;
		return result;
	}

	// ( cond )
	@Override
	public ArrayList<Node> visitCondBrackets(XQueryParser.CondBracketsContext ctx) {
		return visit(ctx.cond());
	}

	// xq1 == xq2, xq1 eq xq2, use isEqualNode
	@Override
	public ArrayList<Node> visitCondEqual(XQueryParser.CondEqualContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
		ArrayList<Node> xq1Res = visit(ctx.xq(0));
		curNodes = curNodesCopy;
		ArrayList<Node> xq2Res = visit(ctx.xq(1));
		for (Node node1 : xq1Res) {
			for (Node node2 : xq2Res) {
				if (node1.isEqualNode(node2)) {
					result.add(node1);
				}
			}
		}
		curNodes = curNodesCopy;
		return result;
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
		result = removeDuplicates(result);
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
		result = removeDuplicates(result);
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
		result = removeDuplicates(result);
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
			ArrayList<Node> each = new ArrayList<>();
			each.add(node);
			curNodes = each;
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

	// not filter, True if empty else false
	@Override
	public ArrayList<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) {
		ArrayList<Node> result = new ArrayList<>();
		ArrayList<Node> curNodesCopy = new ArrayList<>(curNodes);
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
		require = require.substring(1, (require.length() - 1));
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