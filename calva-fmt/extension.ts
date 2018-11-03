import * as vscode from 'vscode';
import { FormaOnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider } from './providers/range_formatter';
import * as formatter from './format';

const ClojureLanguageConfiguration = {
    wordPattern: /[^\s()[\]{};"\\]+/,
}

function activate(context: vscode.ExtensionContext) {
    vscode.workspace.getConfiguration().update('files.trimTrailingWhitespace', false);
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.formatCurrentForm', formatter.formatPositionCommand));
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new FormaOnTypeEditProvider,
        "\n", " "));
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
