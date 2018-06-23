import * as vscode from 'vscode';
import * as config from '../config';
const { indentForPosition, cljify, jsify } = require('../../cljc_out/calva_fmt');

let paredit = require('paredit.js');

function minimalRangeForIndenting(document: vscode.TextDocument, pos: vscode.Position): vscode.Range {
    let allText: string = document.getText(),
        ast: object = paredit.parse(allText),
        peRange: object = paredit.navigator.rangeForDefun(ast, document.offsetAt(pos));
    if (peRange) {
        return new vscode.Range(document.positionAt(peRange[0]), document.positionAt(peRange[1]));
    } else {
        return new vscode.Range(pos, pos);
    }
}

function calculateIndent(document: vscode.TextDocument, pos: vscode.Position): number {
    let range = minimalRangeForIndenting(document, pos),
        text: string = document.getText(range),
        cljRange: object = {
            "start": { "line": range.start.line, "character": range.start.character },
            "end": { "line": range.end.line, "character": range.end.character }
        },
        cljPos: object = { "line": pos.line, "character": pos.character },
        data: object = { "text": text, "range": cljRange, "pos": cljPos, "config": config.getConfig() },
        result: object = jsify(indentForPosition(cljify(data)));
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