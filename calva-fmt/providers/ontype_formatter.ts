import * as vscode from 'vscode';
import * as formatter from '../format';
import { getDocument, getIndent } from "../docmirror";
export class FormaOnTypeEditProvider {
    provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        if (vscode.workspace.getConfiguration("calva.fmt").get("formatAsYouType")) {
            let editor = vscode.window.activeTextEditor;
            let pos = new vscode.Position(position.line, 0);
            let indent = getIndent(document, pos)
            
            /*
            if(editor.document == document)
                setTimeout(() => {
                    let endPos = new vscode.Position(pos.line, indent)
                    editor.selection = new vscode.Selection(endPos, endPos);
                },1)
            */  
            let delta = document.lineAt(position.line).firstNonWhitespaceCharacterIndex-indent;
            if(delta > 0) {
                editor.edit(edits => edits.delete(new vscode.Range(pos, new vscode.Position(pos.line, delta))));
            } else if(delta < 0) {
                let str = "";
                while(delta++ < 0)
                    str += " "
                editor.edit(edits => edits.insert(pos, str));
            }
        }
        return null;
    }
}