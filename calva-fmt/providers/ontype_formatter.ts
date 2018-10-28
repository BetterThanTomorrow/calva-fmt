import * as vscode from 'vscode';
import * as config from '../config';
import * as formatter from '../format';
const { formatTextAtIdx, cljify, jsify } = require('../../lib/calva_fmt');


function calculateIndent(document: vscode.TextDocument, pos: vscode.Position): number {
    let allText: string = document.getText(),
        idx: number = document.offsetAt(pos),
        data: object = { "all-text": allText, "idx": idx, "config": config.getConfig() },
        result: object = jsify(formatTextAtIdx(cljify(data)));
    if (!result["error"]) {
        return result["new-index"];
    } else {
        console.log(result["error"]);
        return pos.character;
    }
}


export class FormaOnTypeEditProvider {
    provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        formatter.formatPositionCommand(vscode.window.activeTextEditor);
        return null;
    }
    //     if (config.getConfig()["adjust-cursor-position?"]) {
    //         let indent = calculateIndent(document, position),
    //             startPosition = position.with(position.line, 0);
    //         if (position.character !== undefined) {
    //             if (position.character > indent) {
    //                 return [vscode.TextEdit.delete(new vscode.Range(position.with(position.line, indent), position))];
    //             } else {
    //                 return [vscode.TextEdit.insert(startPosition, ' '.repeat(indent - position.character))];
    //             }
    //         } else {
    //             return null;
    //         }
    //     } else {
    //         return null;
    //     }
    // }
}


export class OnlyAdjustIndentOnTypeEditProvider {
    provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        if (config.getConfig()["adjust-cursor-position?"]) {
            let indent = calculateIndent(document, position),
                startPosition = position.with(position.line, 0);
            if (position.character !== undefined) {
                if (position.character > indent) {
                    return [vscode.TextEdit.delete(new vscode.Range(position.with(position.line, indent), position))];
                } else {
                    return [vscode.TextEdit.insert(startPosition, ' '.repeat(indent - position.character))];
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}