import * as vscode from 'vscode';
import { FormaOnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider } from './providers/range_formatter';
import * as formatter from './format';
import * as inferer from './infer';

const ClojureLanguageConfiguration: vscode.LanguageConfiguration = {
    wordPattern: /[^\s#()[\]{};"\\]+/,
    onEnterRules: [
        // In a desperate attempt to stop VS Code from indenting top level lines, the gloves are off!
        {
            beforeText: /.*/,
            action: { indentAction: vscode.IndentAction.Outdent }
        },
    ]
}



function activate(context: vscode.ExtensionContext) {
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.formatCurrentForm', formatter.formatPositionCommand));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.inferParens', inferer.inferParensCommand));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.tabIndent', inferer.tabIndentCommand));
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
