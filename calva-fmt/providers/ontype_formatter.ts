import * as vscode from 'vscode';
import * as config from '../config';
import * as formatter from '../format';
const { formatTextAtIdx, cljify, jsify } = require('../../lib/calva_fmt');

export class FormaOnTypeEditProvider {
    provideOnTypeFormattingEdits(document: vscode.TextDocument, position: vscode.Position, _ch, _options) {
        formatter.formatPosition(vscode.window.activeTextEditor, true);
        return null;
    }
}