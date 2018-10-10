import * as vscode from 'vscode';
import * as config from './config';
const { formatTextAtRange, formatTextAtIdx, cljify, jsify } = require('../lib/calva_fmt');


export function formatRangeEdits(document: vscode.TextDocument, range: vscode.Range): vscode.TextEdit[] {
    const text: string = document.getText(range),
        rangeTuple: number[] = [document.offsetAt(range.start), document.offsetAt(range.end)],
        newText: string = _formatRange(text, document.getText(), rangeTuple);
    return [vscode.TextEdit.replace(range, newText)];
}

export function formatRange(document: vscode.TextDocument, range: vscode.Range) {
    let wsEdit: vscode.WorkspaceEdit = new vscode.WorkspaceEdit();
    wsEdit.set(document.uri, formatRangeEdits(document, range));
    return vscode.workspace.applyEdit(wsEdit);
}

export function formatPosition(document: vscode.TextDocument, pos: vscode.Position): [Thenable<boolean>, number] {
    const index = document.offsetAt(pos),
        formatted: { "text": string, "range": number[], "new-index": number } = _formatIndex(document.getText(), index),
        range: vscode.Range = new vscode.Range(document.positionAt(formatted.range[0]), document.positionAt(formatted.range[1])),
        previousText: string = document.getText(range);
    if (previousText != formatted.text) {
        let wsEdit: vscode.WorkspaceEdit = new vscode.WorkspaceEdit();
        wsEdit.set(document.uri, [vscode.TextEdit.replace(range, formatted.text)]);
        return [vscode.workspace.applyEdit(wsEdit), document.offsetAt(range.start) + formatted["new-index"]];
    } else {
        return [new Promise(() => { return false }), index];
    }
}

export function formatPositionCommand(editor: vscode.TextEditor) {
    const doc: vscode.TextDocument = editor.document;
    const pos: vscode.Position = editor.selection.active;
    const [promise, index] = formatPosition(doc, pos);
    promise.then((editsWherePerfomed) => {
        if (editsWherePerfomed) {
            editor.selection = new vscode.Selection(doc.positionAt(index), doc.positionAt(index));
        }
    });
}

function _formatIndex(allText: string, index: number): { "text": string, "range": number[], "new-index": number } {
    const d = cljify({
        "all-text": allText,
        "idx": index,
        "config": config.getConfig()
    }),
        result = jsify(formatTextAtIdx(d));
    if (!result["error"]) {
        return result;
    }
    else {
        console.log(result["error"]);
        return null;
    }
}


function _formatRange(text: string, allText: string, range: number[]): string {
    const d = cljify({
        "text": text,
        "all-text": allText,
        "range": range,
        "config": config.getConfig()
    }),
        result = jsify(formatTextAtRange(d));
    if (!result["error"]) {
        return result["text"];
    }
    else {
        console.log(result["error"]);
        return text;
    }
}
