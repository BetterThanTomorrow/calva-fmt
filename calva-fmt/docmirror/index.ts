import * as vscode from "vscode";
import { Scanner, Token, ScannerState } from "./clojure-lexer";

const scanner = new Scanner();

class ClojureSourceLine {
    tokens: Token[];
    endState: ScannerState;
    constructor(public text: string, public startState: ScannerState, lineNo: number) {
        this.tokens = scanner.processLine(text, lineNo, startState)
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

    dirtyLines: number[] = [];

    constructor(public doc: vscode.TextDocument) {
        scanner.state = { inString: false, paren: null }
        for(let i=0; i<doc.lineCount; i++) {
            let line = doc.lineAt(i);
            this.lines.push(new ClojureSourceLine(line.text, scanner.state, i));
        }
    }

    private markDirty(idx: number) {
        if(this.dirtyLines.indexOf(idx) == -1)
            this.dirtyLines.push(idx);
    }

    private removeDirty(start: number, end: number, inserted: number) {
        let delta = end-start + inserted;
        this.dirtyLines = this.dirtyLines.filter(x => x < start || x > end)
                                          .map(x => x > start ? x - delta : x);
    }
    
    private getStateForLine(line: number) {
        return line == 0 ? { inString: false, paren: null } : this.lines[line-1].endState;
    }

    private changeRange(e: vscode.TextDocumentContentChangeEvent) {
        // extract the lines we will replace
        let replaceLines = e.text.split(this.doc.eol == vscode.EndOfLine.LF ? /\n/ : /\r\n/);

        // the left side of the line unaffected by the edit.
        let left = this.lines[e.range.start.line].text.substr(0, e.range.start.character);

        // the right side of the line unaffected by the edit.
        let right = this.lines[e.range.end.line].text.substr(e.range.end.character);

        // we've nuked this lines, so. yay.
        this.removeDirty(e.range.start.line, e.range.end.line, replaceLines.length-1);

        let items: ClojureSourceLine[] = [];
        
        // initialize the lexer state - the first line is definitely not in a string, otherwise copy the
        // end state of the previous line before the edit
        let state = this.getStateForLine(e.range.start.line)

        let lastLine: ClojureSourceLine;
        if(replaceLines.length == 1) {
            // trivial single line edit
            items.push(lastLine = new ClojureSourceLine(left + replaceLines[0] + right, state, e.range.start.line));
        } else {
            // multi line edit.
            items.push(new ClojureSourceLine(left + replaceLines[0], state, e.range.start.line));
            for(let i=1; i<replaceLines.length-1; i++)
                items.push(new ClojureSourceLine(replaceLines[i], scanner.state, e.range.start.line+i));
            items.push(lastLine = new ClojureSourceLine(replaceLines[replaceLines.length-1] + right, scanner.state, e.range.start.line+replaceLines.length))
        }

        // now splice in our edited lines
        this.lines.splice(e.range.start.line, e.range.end.line-e.range.start.line+1, ...items);
        let nextIdx = e.range.start.line+replaceLines.length
        let nextLine = this.lines[nextIdx];
        
        if(nextLine && !equal(nextLine.startState, lastLine.endState))
            this.markDirty(nextIdx); // everything is desynced now, so mark this line.
        console.log(this.dirtyLines);
    }

    flushChanges() {
        let seen = new Set<number>();
        this.dirtyLines.sort();
        while(this.dirtyLines.length) {
            let nextIdx = this.dirtyLines.shift();
            if(seen.has(nextIdx))
                continue; // already processed.

            seen.add(nextIdx);
            let prevState = this.getStateForLine(nextIdx);
            let newLine: ClojureSourceLine;
            do {
                newLine = new ClojureSourceLine(this.lines[nextIdx].text, prevState, nextIdx+1);
                prevState = newLine.endState;
                this.lines[nextIdx++] = newLine;    
            } while(this.lines[nextIdx] && !(equal(this.lines[nextIdx].startState, newLine.endState)))
        }
    }

    processChanges(e: vscode.TextDocumentContentChangeEvent[]) {
        for(let change of e)
            this.changeRange(change);
        this.flushChanges();
        
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