import * as vscode from 'vscode';

function readConfiguration() {
    let workspaceConfig = vscode.workspace.getConfiguration("calva.fmt")
    return {
        "indentation?": workspaceConfig.get("indentation"),
        "remove-surrounding-whitespace?": workspaceConfig.get("removeSurroundingWhitespace"),
        "remove-trailing-whitespace?": workspaceConfig.get("removeTrailingWhitespace"),
        "insert-missin-whitespace?": workspaceConfig.get("insertMissingWhitespace")
    };
}

export function getConfig() {
    let config = readConfiguration();
    config["remove-consecutive-blank-lines?"] = false; // Until we understand its behaviour better
    return config;
}
