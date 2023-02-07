package cse232b.visitor;
import java.util.ArrayList;
//java package
import java.util.List;
//antlr4 package
import cse232b.parsers.XQueryBaseVisitor;
import cse232b.parsers.XQueryParser;

//xml package
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XqueryExpressionBuilder extends XQueryBaseVisitor<List<Node>> {
	//use a nodelist to store the current visit node
	private List<Node> curNodes = new ArrayList<Node>();
	@Override public List<Node> visitApChildren(XQueryParser.ApChildrenContext ctx){
		return visit(ctx.rp());

	}

	@Override public List<Node> visitApDescendants(XQueryParser.ApDescendantsContext ctx) { 
		return visitChildren(ctx); 
	}
	
	@Override public List<Node> visitXmlDoc(XQueryParser.XmlDocContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFileName(XQueryParser.FileNameContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitTagName(XQueryParser.TagNameContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitParent(XQueryParser.ParentContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitAttribute(XQueryParser.AttributeContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitRpChildren(XQueryParser.RpChildrenContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitRpParentheses(XQueryParser.RpParenthesesContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitText(XQueryParser.TextContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitChildren(XQueryParser.ChildrenContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitRpConcat(XQueryParser.RpConcatContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitCurrent(XQueryParser.CurrentContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitRpDescendants(XQueryParser.RpDescendantsContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitRpFilter(XQueryParser.RpFilterContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterEqual(XQueryParser.FilterEqualContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterNot(XQueryParser.FilterNotContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterOr(XQueryParser.FilterOrContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterAnd(XQueryParser.FilterAndContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterRp(XQueryParser.FilterRpContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterParentheses(XQueryParser.FilterParenthesesContext ctx) { 
		return visitChildren(ctx); 
	}

	@Override public List<Node> visitFilterIs(XQueryParser.FilterIsContext ctx) { 
		return visitChildren(ctx); 
	}
}
