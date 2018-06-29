import * as vscode from 'vscode';
import { OnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider } from './providers/range_formatter';

const ClojureLanguageConfiguration = {
    wordPattern: /[^\s()[\]{};"\\]+/,
}

function activate(context: vscode.ExtensionContext) {
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new OnTypeEditProvider, "\n"));
    context.subscriptions.push(vscode.languages.registerDocumentRangeFormattingEditProvider("clojure", new RangeEditProvider));
}

module.exports = {
    activate
}
