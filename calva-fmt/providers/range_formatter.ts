import * as vscode from 'vscode';
import * as formatter from '../format';


function formatRangeEdits(document: vscode.TextDocument, range: vscode.Range): vscode.TextEdit[] {
    const text: string = document.getText(range),
          newText: string = formatter.format(text);
    return [vscode.TextEdit.replace(range, newText)];
}

export function formatRange(document: vscode.TextDocument, range: vscode.Range) {
    let wsEdit: vscode.WorkspaceEdit = new vscode.WorkspaceEdit();
    wsEdit.set(document.uri, formatRangeEdits(document, range));
    vscode.workspace.applyEdit(wsEdit);
}

export class RangeEditProvider implements vscode.DocumentRangeFormattingEditProvider {
    provideDocumentRangeFormattingEdits(document: vscode.TextDocument, range: vscode.Range, _options, _token) {
        return formatRangeEdits(document, range);
    }
}
