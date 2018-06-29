import * as vscode from 'vscode';
import * as formatter from '../format';

export class RangeEditProvider implements vscode.DocumentRangeFormattingEditProvider {
    provideDocumentRangeFormattingEdits(document: vscode.TextDocument, range: vscode.Range, _options, _token) {
        let text: string = document.getText(range),
            newText: string = formatter.format(text);
        return [vscode.TextEdit.replace(range, newText)];
    }
}