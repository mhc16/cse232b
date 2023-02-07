grammar XQuery;

@header {
package cse232b.parsers;
}

//lexer rule
WORD                : [a-zA-Z0-9_-]+;
WS                  : [ \t\r\n]+ -> skip;
StringConstant      : '"'[a-zA-Z0-9_ ,.!?;'"-]+'"';

// parser rule
// absolute path
ap          : doc '/' rp                        # ApChildren
            | doc '//' rp                       # ApAllDescendants
            ;
//document
doc         : 'doc' '("' fname '")'             # DocFile
            ;
//file name
fname       : WORD ('.' WORD)*                  # FileName
            ;
//relative path
rp          : WORD                              # TagName
            | '*'                               # Children
            | '.'                               # Self
            | '..'                              # Parent
            | 'text()'                          # Txt
            | '@' WORD                          # AttrName
            | '(' rp ')'                        # RpBrackets
            | rp '/' rp                         # RpChildren
            | rp '//' rp                        # RpAllDescendants
            | rp '[' filter ']'                 # RpFilter
            | rp ',' rp                         # RpConcat
            ;
//filter
filter      : rp                                # FilterRp
            | rp '=' rp                         # FilterEqual
            | rp 'eq' rp                        # FilterEqual
            | rp '==' rp                        # FilterIs
            | rp 'is' rp                        # FilterIs
            | rp '=' StringConstant				# FilterString
            | '(' filter ')'                    # FilterBrackets
            | filter 'and' filter               # FilterAnd
            | filter 'or' filter                # FilterOr
            | 'not' filter                      # FilterNot
            ;
