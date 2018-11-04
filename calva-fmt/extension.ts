import * as vscode from 'vscode';
import { FormaOnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider } from './providers/range_formatter';
import * as formatter from './format';

const ClojureLanguageConfiguration: vscode.LanguageConfiguration = {
    wordPattern: /[^\s()[\]{};"\\]+/,
    indentationRules: {
        //increaseIndentPattern: /(\((?!.*\))|\[(?!.*\])|\{(?!.*\}))/,
        increaseIndentPattern: undefined,
        decreaseIndentPattern: undefined
    },
    onEnterRules: [
        {
            beforeText: /\((?!.*\))/,
            action: { indentAction: vscode.IndentAction.Indent }
        },
        {
            beforeText: /\[(?!.*\])/,
            action: { indentAction: vscode.IndentAction.Indent }
        },
        {
            beforeText: /\{(?!.*\})/,
            action: { indentAction: vscode.IndentAction.Indent }
        },
    ]

}

function activate(context: vscode.ExtensionContext) {
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.formatCurrentForm', formatter.formatPositionCommand));
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new FormaOnTypeEditProvider,
        "\n"));
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
