import * as vscode from 'vscode';
import { OnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider, formatRange } from './providers/range_formatter';

const ClojureLanguageConfiguration = {
    wordPattern: /[^\s()[\]{};"\\]+/,
}

function activate(context: vscode.ExtensionContext) {
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new OnTypeEditProvider, "\n"));
    context.subscriptions.push(vscode.languages.registerDocumentRangeFormattingEditProvider("clojure", new RangeEditProvider));

    const api = {
        formatRange: formatRange
    };

    return api;
}

module.exports = {
    activate
}
