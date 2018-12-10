import * as vscode from 'vscode';
import * as formatter from '../format';

export class FormaOnTypeEditProvider {
    provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        if (vscode.workspace.getConfiguration("calva.fmt").get("formatAsYouType")) {
            formatter.formatPosition(vscode.window.activeTextEditor, true);
        }
        return null;
    }
}