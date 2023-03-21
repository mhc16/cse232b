package cse232b.visitor;

//java package

import cse232b.parsers.XQueryBaseVisitor;
import cse232b.parsers.XQueryParser;
import javafx.util.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

class SubQuery {
    public Integer idx;
    public ArrayList<String> vars;
    public HashMap<String,String> var2xqs;
    public ArrayList<String> conds;

    public SubQuery(Integer id){
        idx = id;
        vars = new ArrayList<>();
        var2xqs = new HashMap<>();
        conds = new ArrayList<>();
    }

    public void addVar(String var,String xq){
        vars.add(var);
        var2xqs.put(var,xq);
    }

    public String Sequentialize(){
        StringBuilder res = new StringBuilder("for ");
        for (int i=0;i<vars.size();i++){
            String var = vars.get(i);
            String xq = var2xqs.get(var);
            res.append(var+" in "+xq+"\n\t");
            if (i<vars.size()-1){
                res.append(',');
            }
        }
        if (!conds.isEmpty()) {
            res.append("where "+conds.get(0));
            for (int i = 1; i < conds.size(); i++) {
                res.append(",\n"+conds.get(i));
            }
            res.append("\n");
        }
        res.append("return <tuple>{\n");
        for (int i=0; i<vars.size();i++) {
            String varName = vars.get(i);
            String plainName = varName.substring(1);
            res.append("<" + plainName + ">{" + varName + "}</" + plainName + ">");
            if (i<vars.size()-1) {
                res.append(",");
            }
            res.append("\n");
        }
        res.append("}</tuple>,\n");
        return res.toString();
    }
}

public class XqueryRewriteBuilder extends XQueryBaseVisitor<String> {
    HashMap<String,Integer> vars2id = new HashMap<>();
    ArrayList<SubQuery> subQueries = new ArrayList<>();
    ArrayList<Pair<String,String>> crossConds = new ArrayList<>();

    @Override
    public String visitXqFLWR(XQueryParser.XqFLWRContext ctx) {
        visit(ctx.forClause());
        if (ctx.whereClause() != null) {
            visit(ctx.whereClause());
        }
        return visit(ctx.returnClause());
    }

    @Override
    public String visitForClause(XQueryParser.ForClauseContext ctx) {
        Integer size = ctx.var().size();
        for (int i =0;i<size;i++){
            String varName = ctx.var(i).getText();
            String varXq = ctx.xq(i).getText();
            if (varXq.startsWith("$")){
                String rootVar = varXq.split("/")[0];
                Integer id = vars2id.get(rootVar);
                SubQuery temp = subQueries.get(id);
                temp.addVar(varName,varXq);
                vars2id.put(varName,id);
            } else if (varXq.startsWith("doc")){
                Integer id = subQueries.size();
                SubQuery newSub = new SubQuery(id);
                newSub.addVar(varName,varXq);
                subQueries.add(newSub);
                vars2id.put(varName,id);
            } else {
                System.out.println("error when processing for clause");
            }
        }
        return "";
    }

    @Override
    public String visitCondEqual(XQueryParser.CondEqualContext ctx) {
        String leq = ctx.xq(0).getText();
        String req = ctx.xq(1).getText();
        if (leq.startsWith("$") && req.startsWith("$") && !vars2id.get(leq).equals(vars2id.get(req))) {
            crossConds.add(new Pair<>(leq, req));
        } else if (leq.startsWith("$")) {
            Integer lid = vars2id.get(leq);
            SubQuery temp = subQueries.get(lid);
            temp.conds.add(leq+" eq "+req);
        } else if (req.startsWith("$")) {
            Integer rid = vars2id.get(req);
            SubQuery temp = subQueries.get(rid);
            temp.conds.add(req+" eq "+leq);
        }
        return "";
    }

    // where cond, visit condition
    @Override
    public String visitWhereClause(XQueryParser.WhereClauseContext ctx) {
        visit(ctx.cond());
        return "";
    }

    // return xq
    @Override
    public String visitReturnClause(XQueryParser.ReturnClauseContext ctx) {
        StringBuilder result = new StringBuilder();
        HashSet<String> curVars = new HashSet<>();
        Integer start = vars2id.get(crossConds.get(0).getKey());
        result.append(subQueries.get(start).Sequentialize());
        curVars.addAll(subQueries.get(start).vars);
        HashSet<Integer> curIds = new HashSet<>();
        curIds.add(start);
        while (curIds.size()<subQueries.size()){
//            System.out.println(curIds);
            boolean Matched = false;
            for (int i = 0;i<subQueries.size();i++) {
                if (curIds.contains(i)) {
                    continue;
                }
                ArrayList<String> left = new ArrayList<>();
                ArrayList<String> right = new ArrayList<>();
                ArrayList<Pair<String, String>> crossCondscopy = new ArrayList<>(crossConds);
                ArrayList<String> temp = subQueries.get(i).vars;
                for (Pair<String, String> cond : crossCondscopy) {
                    String lv = cond.getKey(), rv = cond.getValue();
                    if ((curVars.contains(lv) && temp.contains(rv)) || (curVars.contains(rv) && temp.contains(lv))) {
                        String lvn = lv.substring(1), rvn = rv.substring(1);
                        Matched = true;
                        if (curVars.contains(lv)) {
                            left.add(lvn);
                            right.add(rvn);
                            crossConds.remove(cond);
                        } else {
                            left.add(rvn);
                            right.add(lvn);
                            crossConds.remove(cond);
                        }
                    }
                }
                if (Matched) {
                    curVars.addAll(subQueries.get(i).vars);
                    curIds.add(i);
                    result.insert(0, "join (");
                    result.append(subQueries.get(i).Sequentialize());
                    result.append("[");
                    for (String item : left) {
                        result.append(item);
                        result.append(",");
                    }
                    result.deleteCharAt(result.length() - 1);
                    result.append("], [");
                    for (String item : right) {
                        result.append(item);
                        result.append(",");
                    }
                    result.deleteCharAt(result.length() - 1);
                    result.append("]\n),\n");
                    break;
                }
            }
            if (!Matched){
                for (int i = 0;i<subQueries.size();i++){
                    if (curIds.contains(i)){
                        continue;
                    }
                    curIds.add(i);
                    curVars.addAll(subQueries.get(i).vars);
                    result.insert(0, "join (");
                    result.append(subQueries.get(i).Sequentialize());
                    result.append("[], []\n),\n");
                }
            }
        }

        result.deleteCharAt(result.length()-2);
        result.insert(0, "for $tuple in ");
        String returnText = ctx.getText();
        returnText = returnText.replace("return", "return\n");
        returnText = returnText.replaceAll("\\$([A-Za-z0-9_]+)", "\\$tuple/$1/*");
        returnText = returnText.replaceAll(",", ",\n");
        result.append(returnText);
        return result.toString();

    }


}