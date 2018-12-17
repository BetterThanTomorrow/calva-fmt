import { LexicalGrammar, Token } from "./lexer";
export type Token = Token;

let toplevel = new LexicalGrammar();
toplevel.terminal("\\s+", (l, m) => ({ type: "ws" }))
toplevel.terminal(";.*", (l, m) => ({ type: "comment" }))
toplevel.terminal("\\(|\\)|\\[|\\]|\\{|\\}|#\\{|,|~@|~|'|#'|#:|#_|^|#\\(|`|#?\\(", (l, m) => ({ type: "punc" }))
toplevel.terminal("[^()[\\]\\{\\}#,~@'`^\"\\s]+", (l, m) => ({ type: "id" }))
toplevel.terminal('"(\\\\.|[^"]+)*"', (l, m) => ({ type: "str"}))
toplevel.terminal('"([^"]+|\\\\.)*', (l, m) => ({ type: "str-start"}))

let multstring = new LexicalGrammar()
multstring.terminal('([^"]|\\\\.)*"', (l, m) => ({ type: "str-end" }))
multstring.terminal('([^"]|\\\\.)*', (l, m) => ({ type: "str" }))

export type ScannerState = { inString: boolean }

export class Scanner {
    state: ScannerState = { inString: false };
    processLine(line: string, state: ScannerState = this.state) {
        let tks: Token[] = [];
        this.state = state;
        let lex = (this.state.inString ? multstring : toplevel).lex(line);
        let tk: Token;
        do {
            tk = lex.scan();
            if(tk) {
                let oldpos = lex.position;
                if(tk.type == "str-end") {
                    this.state = { ...this.state, inString: false};
                    lex = toplevel.lex(line);
                    lex.position = oldpos;
                } else if (tk.type == "str-start") {
                    this.state = { ...this.state, inString: true};
                    lex = multstring.lex(line);
                    lex.position = oldpos;
                }
                tks.push(tk);
            }
        } while(tk);
        return tks;
    }
}