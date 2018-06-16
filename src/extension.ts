'use strict';
//import { StatusBar } from './status_bar';
import * as vscode from 'vscode';
import * as ontype from './providers/ontype_formatter';


export function activate(context: vscode.ExtensionContext) {
    context.subscriptions.push(vscode.commands.registerCommand("calva.fmt.toggleAutoAdjustIndent", ontype.toggleAutoAdjustIndentCommand));
    context.subscriptions.push(vscode.languages.registerOnTypeFormattingEditProvider("clojure", new ontype.ClojureOnTypeFormattingEditProvider, "\n"));
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