package cse232b.visitor;
//java package
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

//antlr4 package
import cse232b.parsers.XQueryBaseVisitor;
import cse232b.parsers.XQueryParser;

//xml package
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XqueryExpressionBuilder extends XQueryBaseVisitor<ArrayList<Node>> {
	//use a nodelist to store the current visit node
	private ArrayList<Node> curNodes = new ArrayList<Node>();
	//util function
	//remove duplicate node
	private ArrayList<Node> removeDuplicates(ArrayList<Node> list){
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
	//visit all descendants node of input node list
	private ArrayList<Node> visitAllDescendants(ArrayList<Node> list){
		ArrayList<Node> result = new ArrayList<>();
		for (Node ele : list) {
			NodeList children = ele.getChildNodes();
			
			
		}
		return result;
	}
	//doc then rp
	@Override public ArrayList<Node> visitApChildren(XQueryParser.ApChildrenContext ctx){
		visit(ctx.doc());
		return visit(ctx.rp());

	}
	//doc then iterate all descendants then ap
	@Override public ArrayList<Node> visitApAllDescendants(XQueryParser.ApAllDescendantsContext ctx) { 
		visit(ctx.doc());
		return visitChildren(ctx); 
	}
	
	@Override public ArrayList<Node> visitDocFile(XQueryParser.DocFileContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public ArrayList<Node> visitFileName(XQueryParser.FileNameContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitAttrName(XQueryParser.AttrNameContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitTxt(XQueryParser.TxtContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitTagName(XQueryParser.TagNameContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitParent(XQueryParser.ParentContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitRpChildren(XQueryParser.RpChildrenContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitRpAllDescendants(XQueryParser.RpAllDescendantsContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitChildren(XQueryParser.ChildrenContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitSelf(XQueryParser.SelfContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitRpConcat(XQueryParser.RpConcatContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitRpFilter(XQueryParser.RpFilterContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitRpBrackets(XQueryParser.RpBracketsContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterEqual(XQueryParser.FilterEqualContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterBrackets(XQueryParser.FilterBracketsContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterOr(XQueryParser.FilterOrContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterAnd(XQueryParser.FilterAndContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterRp(XQueryParser.FilterRpContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterIs(XQueryParser.FilterIsContext ctx) { return visitChildren(ctx); }
	/**
	 * {@inheritDoc}
	 *
	 * <p>The default implementation returns the result of calling
	 * {@link #visitChildren} on {@code ctx}.</p>
	 */
	@Override public ArrayList<Node> visitFilterString(XQueryParser.FilterStringContext ctx) { return visitChildren(ctx); }
	
}
