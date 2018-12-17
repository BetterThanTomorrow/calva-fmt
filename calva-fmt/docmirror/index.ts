import * as vscode from "vscode";
import { Scanner, Token, ScannerState } from "./clojure-lexer";

const scanner = new Scanner();

class ClojureSourceLine {
    tokens: Token[];
    endState: ScannerState;
    constructor(public text: string, public startState: ScannerState) {
        this.tokens = scanner.processLine(text, startState)
        this.endState = scanner.state;
    }

    get length() {
        return this.text.length;
    }
}

let debugValidation = false

function equal(x: any, y: any): boolean {
    if(x==y) return true;
    if(x instanceof Array && y instanceof Array) {
        if(x.length == y.length) {
            for(let i = 0; i<x.length; i++)
                if(!equal(x[i], y[i]))
                    return false;
            return true;
        } else
            return false;
    } else if (!(x instanceof Array) && !(y instanceof Array) && x instanceof Object && y instanceof Object) {
        for(let f in x)
            if(!equal(x[f], y[f]))
                return false;
        for(let f in y)
            if(!x.hasOwnProperty(f))
                return false
        return true;
    }
    return false;
}

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
        // extract the lines we will replace
        let replaceLines = e.text.split(this.doc.eol == vscode.EndOfLine.LF ? /\n/ : /\r\n/);

        // the left side of the line unaffected by the edit.
        let left = this.lines[e.range.start.line].text.substr(0, e.range.start.character);

        // the right side of the line unaffected by the edit.
        let right = this.lines[e.range.end.line].text.substr(e.range.end.character);

        let items: ClojureSourceLine[] = [];
        
        // initialize the lexer state - the first line is definitely not in a string, otherwise copy the
        // end state of the previous line before the edit
        let state = e.range.start.line == 0 ? { inString: false } : this.lines[e.range.start.line-1].endState;

        let lastLine: ClojureSourceLine;
        if(replaceLines.length == 1) {
            // trivial single line edit
            items.push(lastLine = new ClojureSourceLine(left + replaceLines[0] + right, state));
        } else {
            // multi line edit.
            items.push(new ClojureSourceLine(left + replaceLines[0], state));
            for(let i=1; i<replaceLines.length-1; i++)
                items.push(new ClojureSourceLine(replaceLines[i], scanner.state));
            items.push(lastLine = new ClojureSourceLine(replaceLines[replaceLines.length-1] + right, scanner.state))
        }

        // now splice in our edited lines
        this.lines.splice(e.range.start.line, e.range.end.line-e.range.start.line+1, ...items);
        let nextIdx = e.range.start.line+replaceLines.length
        let nextLine = this.lines[nextIdx];
        let prevState = lastLine.endState;
        
        while(nextLine && !equal(nextLine.startState, prevState)) {
            // everything is desynced now, so scan forward until it is resolved.

            // TODO: this should only really happen after all coalesced events have occured, and even possibly only
            //       when we need to force computation because we need the parenthesis info. For example, we do not
            //       need up to date information for the purposes of formatting for the lines below the cursor *ever*.
            //       because " can toggle the entire file to be in/out of a string, it is important that we don't immediately touch
            //       the whole file again. Waiting to perform this scan only when the cursor is moved, or on return would be enough.
            //
            //       This is a TODO because I need to track 'dirty' line #s, and update the indices as edits move them about.
            
            let newLine = new ClojureSourceLine(this.lines[nextIdx].text, prevState);
            prevState = newLine.endState;
            this.lines[nextIdx++] = newLine;
            nextLine = this.lines[nextIdx];
        }
    }

    processChanges(e: vscode.TextDocumentContentChangeEvent[]) {
        for(let change of e)
            this.changeRange(change);
        
        if(debugValidation && this.doc.getText() != this.text)
            vscode.window.showErrorMessage("DocumentMirror failed");
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