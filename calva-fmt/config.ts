import * as vscode from 'vscode';

function readConfiguration() {
    let workspaceConfig = vscode.workspace.getConfiguration("calva.fmt")
    return {
        "adjust-cursor-position?": workspaceConfig.get("autoAdjustIndentOnNewLines"),
        "indentation?": workspaceConfig.get("indentation"),
        "remove-surrounding-whitespace?": workspaceConfig.get("removeSurroundingWhitespace"),
        "remove-trailing-whitespace?": workspaceConfig.get("removeTrailingWhitespace"),
        "insert-missing-whitespace?": workspaceConfig.get("insertMissingWhitespace"),
        "remove-consecutive-blank-lines?": workspaceConfig.get("removeConsecutiveBlankLines")
    };
}

export function getConfig() {
    let config = readConfiguration();
    return config;
}
