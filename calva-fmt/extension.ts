import * as vscode from 'vscode';
import { FormaOnTypeEditProvider } from './providers/ontype_formatter';
import { RangeEditProvider } from './providers/range_formatter';
import * as formatter from './format';
import * as inferer from './infer';

const ClojureLanguageConfiguration: vscode.LanguageConfiguration = {
    wordPattern: /[^\s#()[\]{};"\\]+/,
    onEnterRules: [
        // This is madness, but the only way to stop vscode from indenting new lines
        {
            beforeText: /.*/,
            action: {
                indentAction: vscode.IndentAction.Outdent,
                removeText: Number.MAX_VALUE
            }
        },
    ]
}



function activate(context: vscode.ExtensionContext) {
    vscode.languages.setLanguageConfiguration("clojure", ClojureLanguageConfiguration);
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.formatCurrentForm', formatter.formatPositionCommand));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.indentCurrentForm', formatter.indentPositionCommand));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.alignCurrentForm', formatter.alignPositionCommand));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.inferParens', inferer.inferParensCommand));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.tabIndent', (e) => { inferer.indentCommand(e, " ", true) }));
    context.subscriptions.push(vscode.commands.registerTextEditorCommand('calva-fmt.tabDedent', (e) => { inferer.indentCommand(e, " ", false) }));
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new FormaOnTypeEditProvider, "\r", "\n"));
    context.subscriptions.push(vscode.languages.registerDocumentRangeFormattingEditProvider("clojure", new RangeEditProvider));
    vscode.window.onDidChangeActiveTextEditor(inferer.updateState);
    // vscode.workspace.onDidChangeTextDocument((e: vscode.TextDocumentChangeEvent) => {
    //     console.log(e);
    // });
    const api = {
        formatPosition: formatter.formatPosition,
        formatRange: formatter.formatRange,
    };

    return api;
}

module.exports = {
    activate
}
