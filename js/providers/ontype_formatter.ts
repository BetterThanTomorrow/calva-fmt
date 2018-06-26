import * as vscode from 'vscode';
import * as config from '../config';
const { indentForIndex, cljify, jsify } = require('../../cljc_out/calva_fmt');


function calculateIndent(document: vscode.TextDocument, pos: vscode.Position): number {
    let allText: string = document.getText(),
        idx: number = document.offsetAt(pos),
        data: object = { "all-text": allText, "idx": idx, "config": config.getConfig() },
        result: object = jsify(indentForIndex(cljify(data)));
    if (!result["error"]) {
        return result["indent"];
    } else {
        console.log(result["error"]);
        return pos.character;
    }
}


export class OnTypeEditProvider {
    provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        let indent = calculateIndent(document, position),
            startPosition = position.with(position.line, 0);
        if (position.character != indent) {
            if (position.character > indent) {
                return [vscode.TextEdit.delete(new vscode.Range(position.with(position.line, indent), position))];
            } else {
                return [vscode.TextEdit.insert(startPosition, ' '.repeat(indent - position.character))];
            }
        } else {
            return null;
        }

    }
}