import * as vscode from 'vscode';
import * as formatter from '../format';

export class RangeEditProvider {
    provideDocumentRangeFormattingEdits(document, range, options, token) {
        let text = document.getText(range),
            newText = formatter.format(text);
        return [vscode.TextEdit.replace(range, newText)];
    }
}