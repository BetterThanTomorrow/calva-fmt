import * as vscode from 'vscode';
import * as config from './config';
const { formatTextAtRange, formatTextAtIdx, formatTextAtIdxOnType, cljify, jsify } = require('../lib/calva_fmt');


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

export function formatPosition(editor: vscode.TextEditor, onType: boolean = false): void {
    const doc: vscode.TextDocument = editor.document,
        pos: vscode.Position = editor.selection.active,
        index = doc.offsetAt(pos),
        formatted: { "range-text": string, "range": number[], "new-index": number } = _formatIndex(doc.getText(), index, onType),
        range: vscode.Range = new vscode.Range(doc.positionAt(formatted.range[0]), doc.positionAt(formatted.range[1])),
        newIndex: number = doc.offsetAt(range.start) + formatted["new-index"],
        previousText: string = doc.getText(range);
    if (previousText != formatted["range-text"]) {
        editor.edit(textEditorEdit => {
            textEditorEdit.replace(range, formatted["range-text"]);
        }, { undoStopAfter: false, undoStopBefore: false }).then((_onFulfilled: boolean) => {
            editor.selection = new vscode.Selection(doc.positionAt(newIndex), doc.positionAt(newIndex));
        });
    } else {
        if (newIndex != index) {
            editor.selection = new vscode.Selection(doc.positionAt(newIndex), doc.positionAt(newIndex));
        }
    }
}

export function formatPositionCommand(editor: vscode.TextEditor) {
    formatPosition(editor);
}

function _formatIndex(allText: string, index: number, onType: boolean = false): { "range-text": string, "range": number[], "new-index": number } {
    const d = cljify({
        "all-text": allText,
        "idx": index,
        "config": config.getConfig(),
        "eol": "\n"
    }),
        result = jsify(onType ? formatTextAtIdxOnType(d) : formatTextAtIdx(d));
    if (!result["error"]) {
        return result;
    }
    else {
        console.log(result["error"]);
        return null;
    }
}


function _formatRange(rangeText: string, allText: string, range: number[]): string {
    const d = cljify({
        "range-text": rangeText,
        "all-text": allText,
        "range": range,
        "config": config.getConfig()
    }),
        result = jsify(formatTextAtRange(d));
    if (!result["error"]) {
        return result["range-text"];
    }
    else {
        console.log(result["error"]);
        return rangeText;
    }
}
