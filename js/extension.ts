import * as vscode from 'vscode';
const onTypeFormatter = require('./providers/ontype_formatter');
const rangeFormatter = require('./providers/range_formatter');

//import { StatusBar } from './status_bar';

function activate(context) {
    context.subscriptions.push(vscode.commands.registerCommand("calva.fmt.toggleAutoAdjustIndent", onTypeFormatter.toggleAutoAdjustIndentCommand));
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new onTypeFormatter.OnTypeEditProvider, "\n"));
    context.subscriptions.push(vscode.languages.registerDocumentRangeFormattingEditProvider("clojure", new rangeFormatter.RangeEditProvider));
}

module.exports = {
    activate
}
// (defn activate[^ js context]
// ;; Languae configuration
//     (vscode / languages.setLanguageConfiguration "clojure", (language - config / ClojureLanguageConfiguration.))

//     ;; Commands
//         (.push(.-subscriptions context)(vscode / commands.registerCommand "calva.fmt.toggleAutoAdjustIndent" ontype - formatting / toggleAutoAdjustIndentCommand))

//     ;; Providers
//         (.push(.-subscriptions context)(vscode / languages.registerDocumentRangeFormattingEditProvider "clojure"(range - formatting / RangeEditProvider.)))
//         (.push(.-subscriptions context)(vscode / languages.registerOnTypeFormattingEditProvider "clojure"(ontype - formatting / OnTypeEditProvider.) "\n"))

//     ;; Inititalization
//         (ontype - formatting / init))