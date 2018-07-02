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
    vscode.workspace.applyEdit(wsEdit);
}

export function formatPosition(document: vscode.TextDocument, index: vscode.Position) {
    const formatted: { "text": string, "range": number[] } = _formatIndex(document.getText(), document.offsetAt(index)),
        range: vscode.Range = new vscode.Range(document.positionAt(formatted.range[0]), document.positionAt(formatted.range[1]));
    formatRange(document, range);
}


function _formatIndex(allText: string, index: number): { "text": string, "range": number[] } {
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
