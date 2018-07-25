import * as vscode from 'vscode';
import { OnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider } from './providers/range_formatter';
import * as formatter from './format';

const ClojureLanguageConfiguration = {
    wordPattern: /[^\s()[\]{};"\\]+/,
}

function activate(context: vscode.ExtensionContext) {
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new OnTypeEditProvider, "\n"));
    context.subscriptions.push(vscode.languages.registerDocumentRangeFormattingEditProvider("clojure", new RangeEditProvider));

    const api = {
        formatPosition: formatter.formatPosition,
        formatRange: formatter.formatRange,
    };

    return api;
}

module.exports = {
    activate
}
