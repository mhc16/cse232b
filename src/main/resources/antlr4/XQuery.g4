grammar XQuery;

// parser rule
// absolute path
ap          : doc '/' rp                        # ApChildren
            | doc '//' rp                       # ApDescendants
            ;
//document
doc         : 'doc' '("' fname '")'             # XmlDoc
            ;
//file name
fname       : WORD ('.' WORD)*                  # FileName
            ;
//relative path
rp          : WORD                              # TagName
            | '*'                               # Children
            | '.'                               # Current
            | '..'                              # Parent
            | 'text()'                          # Text
            | '@' WORD                          # Attribute
            | '(' rp ')'                        # RpParentheses
            | rp '/' rp                         # RpChildren
            | rp '//' rp                        # RpDescendants
            | rp '[' filter ']'                 # RpFilter
            | rp ',' rp                         # RpConcat
            ;
//filter
filter      : rp                                # FilterRp
            | rp '=' rp                         # FilterEqual
            | rp 'eq' rp                        # FilterEqual
            | rp '==' rp                        # FilterIs
            | rp 'is' rp                        # FilterIs
            | rp '=' StringConstant				# FilterEqual
            | '(' filter ')'                    # FilterParentheses
            | filter 'and' filter               # FilterAnd
            | filter 'or' filter                # FilterOr
            | 'not' filter                      # FilterNot
            ;
//lexer rule
WORD                : [a-zA-Z0-9_-]+;
WS                  : [ \t\r\n]+ -> skip;
StringConstant      : '"'[a-zA-Z0-9_ ,.!?;'"-]+'"';