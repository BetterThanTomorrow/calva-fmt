import * as vscode from "vscode";
import { Scanner, Token, ScannerState } from "./clojure-lexer";

const scanner = new Scanner();

class ClojureSourceLine {
    tokens: Token[];
    endState: ScannerState;
    constructor(public text: string, prevState: ScannerState) {
        this.tokens = scanner.processLine(text, prevState)
        this.endState = scanner.state;
        console.log(this.tokens);
    }

    get length() {
        return this.text.length;
    }
}

let debugValidation = false

class DocumentMirror {
    lines: ClojureSourceLine[] = [];
    scanner = new Scanner();

    constructor(public doc: vscode.TextDocument) {
        scanner.state = { inString: false }
        for(let i=0; i<doc.lineCount; i++) {
            let line = doc.lineAt(i);
            this.lines.push(new ClojureSourceLine(line.text, scanner.state));
        }
    }

    changeRange(e: vscode.TextDocumentContentChangeEvent) {
        let replaceLines = e.text.split(this.doc.eol == vscode.EndOfLine.LF ? /\n/ : /\r\n/);
        let left = this.lines[e.range.start.line].text.substr(0, e.range.start.character);
        let right = this.lines[e.range.end.line].text.substr(e.range.end.character);

        let items: ClojureSourceLine[] = [];
        
        let state = e.range.start.line == 0 ? { inString: false } : this.lines[e.range.start.line-1].endState;

        if(replaceLines.length == 1) {
            items.push(new ClojureSourceLine(left + replaceLines[0] + right, state));
        } else {
            items.push(new ClojureSourceLine(left + replaceLines[0], state));
            for(let i=1; i<replaceLines.length-1; i++) {
                items.push(new ClojureSourceLine(replaceLines[i], scanner.state));
            }
            items.push(new ClojureSourceLine(replaceLines[replaceLines.length-1] + right, scanner.state))
        }

        this.lines.splice(e.range.start.line, e.range.end.line-e.range.start.line+1, ...items);
    }

    processChanges(e: vscode.TextDocumentContentChangeEvent[]) {
        for(let change of e) {
            this.changeRange(change);
        }
        
        if(debugValidation && this.doc.getText() != this.text) {
            vscode.window.showErrorMessage("DocumentMirror failed");
        }
    }

    get text() {
        return this.lines.map(x => x.text).join(this.doc.eol == vscode.EndOfLine.LF ? "\n" : "\r\n");
    }
}

let documents = new Map<vscode.TextDocument, DocumentMirror>();

let registered = false;
export function activate() {
    // the last thing we want is to register twice and receive double events...
    if(registered)
        return;
    registered = true;

    vscode.workspace.onDidCloseTextDocument(e => {
        if(e.languageId == "clojure") {
            documents.delete(e);
        }
    })

    vscode.workspace.onDidChangeTextDocument(e => {
        if(e.document.languageId == "clojure") {
            if(!documents.get(e.document))
                documents.set(e.document, new DocumentMirror(e.document));
            else
                documents.get(e.document).processChanges(e.contentChanges)
        }
    })
}