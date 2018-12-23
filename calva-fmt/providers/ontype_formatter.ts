import * as vscode from 'vscode';
import * as formatter from '../format';
import { getDocument, getIndent } from "../docmirror";
export class FormaOnTypeEditProvider {
    async provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        if (vscode.workspace.getConfiguration("calva.fmt").get("formatAsYouType")) {
            let pos = new vscode.Position(position.line, 0);
            let indent = getIndent(document, pos)
            
            let delta = document.lineAt(position.line).firstNonWhitespaceCharacterIndex-indent;
            if(delta > 0) {
                return [vscode.TextEdit.delete(new vscode.Range(pos, new vscode.Position(pos.line, delta)))];
            } else if(delta < 0) {
                let str = "";
                while(delta++ < 0)
                    str += " "
                return [vscode.TextEdit.insert(pos, str)];
            }
        }
        return null;
    }
}